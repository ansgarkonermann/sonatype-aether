package org.sonatype.aether.impl.internal;

/*
 * Copyright (c) 2010 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0, 
 * and you may not use this file except in compliance with the Apache License Version 2.0. 
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the Apache License Version 2.0 is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */

import java.io.File;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.impl.UpdateCheck;
import org.sonatype.aether.impl.UpdateCheckManager;
import org.sonatype.aether.metadata.Metadata;
import org.sonatype.aether.repository.Authentication;
import org.sonatype.aether.repository.Proxy;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.repository.RepositoryPolicy;
import org.sonatype.aether.spi.locator.Service;
import org.sonatype.aether.spi.locator.ServiceLocator;
import org.sonatype.aether.spi.log.Logger;
import org.sonatype.aether.spi.log.NullLogger;
import org.sonatype.aether.transfer.ArtifactNotFoundException;
import org.sonatype.aether.transfer.ArtifactTransferException;
import org.sonatype.aether.transfer.MetadataNotFoundException;
import org.sonatype.aether.transfer.MetadataTransferException;

/**
 * @author Benjamin Bentmann
 */
@Component( role = UpdateCheckManager.class )
public class DefaultUpdateCheckManager
    implements UpdateCheckManager, Service
{

    @Requirement
    private Logger logger = NullLogger.INSTANCE;

    private static final String UPDATED_KEY_SUFFIX = ".lastUpdated";

    private static final String ERROR_KEY_SUFFIX = ".error";

    private static final String NOT_FOUND = "";

    public DefaultUpdateCheckManager()
    {
        // enables default constructor
    }

    public DefaultUpdateCheckManager( Logger logger )
    {
        setLogger( logger );
    }

    public void initService( ServiceLocator locator )
    {
        setLogger( locator.getService( Logger.class ) );
    }

    public DefaultUpdateCheckManager setLogger( Logger logger )
    {
        this.logger = ( logger != null ) ? logger : NullLogger.INSTANCE;
        return this;
    }

    public String getEffectiveUpdatePolicy( RepositorySystemSession session, String policy1, String policy2 )
    {
        return ordinalOfUpdatePolicy( policy1 ) < ordinalOfUpdatePolicy( policy2 ) ? policy1 : policy2;
    }

    private int ordinalOfUpdatePolicy( String policy )
    {
        if ( RepositoryPolicy.UPDATE_POLICY_DAILY.equals( policy ) )
        {
            return 1440;
        }
        else if ( RepositoryPolicy.UPDATE_POLICY_ALWAYS.equals( policy ) )
        {
            return 0;
        }
        else if ( policy != null && policy.startsWith( RepositoryPolicy.UPDATE_POLICY_INTERVAL ) )
        {
            String s = policy.substring( RepositoryPolicy.UPDATE_POLICY_INTERVAL.length() + 1 );
            return Integer.valueOf( s );
        }
        else
        {
            // assume "never"
            return Integer.MAX_VALUE;
        }
    }

    public void checkArtifact( RepositorySystemSession session, UpdateCheck<Artifact, ArtifactTransferException> check )
    {
        if ( check.getLocalLastUpdated() != 0
            && !isUpdatedRequired( session, check.getLocalLastUpdated(), check.getPolicy() ) )
        {
            if ( logger.isDebugEnabled() )
            {
                logger.debug( "Skipped remote update check for " + check.getItem()
                    + ", locally built artifact up-to-date." );
            }

            check.setRequired( false );
            return;
        }

        Artifact artifact = check.getItem();

        File artifactFile = check.getFile();
        if ( artifactFile == null )
        {
            throw new IllegalArgumentException( String.format( "The artifact '%s' has no file attached", artifact ) );
        }

        boolean fileExists = artifactFile.exists();

        File touchFile = getTouchFile( artifact, artifactFile );
        Properties props = read( touchFile );

        String dataKey = getDataKey( artifact, artifactFile, check.getRepository() );

        String error = getError( props, dataKey );

        long lastUpdated;
        if ( fileExists )
        {
            lastUpdated = artifactFile.lastModified();
        }
        else if ( error == null )
        {
            // this is the first attempt ever
            lastUpdated = 0;
        }
        else if ( error.length() <= 0 )
        {
            // artifact did not exist
            lastUpdated = getLastUpdated( props, dataKey );
        }
        else
        {
            // artifact could not be transferred
            String transferKey = getTransferKey( artifact, artifactFile, check.getRepository() );
            lastUpdated = getLastUpdated( props, transferKey );
        }

        if ( lastUpdated == 0 )
        {
            check.setRequired( true );
        }
        else if ( isUpdatedRequired( session, lastUpdated, check.getPolicy() ) )
        {
            check.setRequired( true );
        }
        else if ( fileExists )
        {
            if ( logger.isDebugEnabled() )
            {
                logger.debug( "Skipped remote update check for " + check.getItem()
                    + ", locally cached artifact up-to-date." );
            }

            check.setRequired( false );
        }
        else
        {
            RemoteRepository repository = check.getRepository();

            if ( error == null || error.length() <= 0 )
            {
                if ( session.isNotFoundCachingEnabled() )
                {
                    check.setRequired( false );
                    check.setException( new ArtifactNotFoundException( artifact, repository, "Failure to find "
                        + artifact + " in " + repository.getUrl() + " was cached in the local repository, "
                        + "resolution will not be reattempted until the update interval of " + repository.getId()
                        + " has elapsed or updates are forced" ) );
                }
                else
                {
                    check.setRequired( true );
                }
            }
            else
            {
                if ( session.isTransferErrorCachingEnabled() )
                {
                    check.setRequired( false );
                    check.setException( new ArtifactTransferException( artifact, repository, "Failure to transfer "
                        + artifact + " from " + repository.getUrl() + " was cached in the local repository, "
                        + "resolution will not be reattempted until the update interval of " + repository.getId()
                        + " has elapsed or updates are forced. Original error: " + error ) );
                }
                else
                {
                    check.setRequired( true );
                }
            }
        }
    }

    public void checkMetadata( RepositorySystemSession session, UpdateCheck<Metadata, MetadataTransferException> check )
    {
        if ( check.getLocalLastUpdated() != 0
            && !isUpdatedRequired( session, check.getLocalLastUpdated(), check.getPolicy() ) )
        {
            if ( logger.isDebugEnabled() )
            {
                logger.debug( "Skipped remote update check for " + check.getItem()
                    + ", locally built metadata up-to-date." );
            }

            check.setRequired( false );
            return;
        }

        Metadata metadata = check.getItem();

        File metadataFile = check.getFile();
        if ( metadataFile == null )
        {
            throw new IllegalArgumentException( String.format( "The metadata '%s' has no file attached", metadata ) );
        }

        boolean fileExists = metadataFile.exists();

        File touchFile = getTouchFile( metadata, metadataFile );
        Properties props = read( touchFile );

        String dataKey = getDataKey( metadata, metadataFile, check.getAuthoritativeRepository() );

        String error = getError( props, dataKey );

        long lastUpdated;
        if ( error == null )
        {
            if ( fileExists )
            {
                // last update was successful
                lastUpdated = getLastUpdated( props, dataKey );
            }
            else
            {
                // this is the first attempt ever
                lastUpdated = 0;
            }
        }
        else if ( error.length() <= 0 )
        {
            // metadata did not exist
            lastUpdated = getLastUpdated( props, dataKey );
        }
        else
        {
            // metadata could not be transferred
            String transferKey = getTransferKey( metadata, metadataFile, check.getRepository() );
            lastUpdated = getLastUpdated( props, transferKey );
        }

        if ( lastUpdated == 0 )
        {
            check.setRequired( true );
        }
        else if ( isUpdatedRequired( session, lastUpdated, check.getPolicy() ) )
        {
            check.setRequired( true );
        }
        else if ( fileExists )
        {
            if ( logger.isDebugEnabled() )
            {
                logger.debug( "Skipped remote update check for " + check.getItem()
                    + ", locally cached metadata up-to-date." );
            }

            check.setRequired( false );
        }
        else
        {
            RemoteRepository repository = check.getRepository();

            if ( error == null || error.length() <= 0 )
            {
                check.setRequired( false );
                check.setException( new MetadataNotFoundException( metadata, repository, "Failure to find " + metadata
                    + " in " + repository.getUrl() + " was cached in the local repository, "
                    + "resolution will not be reattempted until the update interval of " + repository.getId()
                    + " has elapsed or updates are forced" ) );
            }
            else
            {
                if ( session.isTransferErrorCachingEnabled() )
                {
                    check.setRequired( false );
                    check.setException( new MetadataTransferException( metadata, repository, "Failure to transfer "
                        + metadata + " from " + repository.getUrl() + " was cached in the local repository, "
                        + "resolution will not be reattempted until the update interval of " + repository.getId()
                        + " has elapsed or updates are forced. Original error: " + error ) );
                }
                else
                {
                    check.setRequired( true );
                }
            }
        }
    }

    private long getLastUpdated( Properties props, String key )
    {
        String value = props.getProperty( key + UPDATED_KEY_SUFFIX, "" );
        try
        {
            return ( value.length() > 0 ) ? Long.parseLong( value ) : 0;
        }
        catch ( NumberFormatException e )
        {
            logger.debug( "Cannot parse lastUpdated date: \'" + value + "\'. Ignoring.", e );
            return 0;
        }
    }

    private String getError( Properties props, String key )
    {
        return props.getProperty( key + ERROR_KEY_SUFFIX );
    }

    private File getTouchFile( Artifact artifact, File artifactFile )
    {
        return new File( artifactFile.getPath() + ".lastUpdated" );
    }

    private File getTouchFile( Metadata metadata, File metadataFile )
    {
        return new File( metadataFile.getParent(), "resolver-status.properties" );
    }

    private String getDataKey( Artifact artifact, File artifactFile, RemoteRepository repository )
    {
        Set<String> mirroredUrls = Collections.emptySet();
        if ( repository.isRepositoryManager() )
        {
            mirroredUrls = new TreeSet<String>();
            for ( RemoteRepository mirroredRepository : repository.getMirroredRepositories() )
            {
                mirroredUrls.add( normalizeRepoUrl( mirroredRepository.getUrl() ) );
            }
        }

        StringBuilder buffer = new StringBuilder( 1024 );

        buffer.append( normalizeRepoUrl( repository.getUrl() ) );
        for ( String mirroredUrl : mirroredUrls )
        {
            buffer.append( '+' ).append( mirroredUrl );
        }

        return buffer.toString();
    }

    private String getTransferKey( Artifact artifact, File artifactFile, RemoteRepository repository )
    {
        return getRepoKey( repository );
    }

    private String getDataKey( Metadata metadata, File metadataFile, RemoteRepository repository )
    {
        return metadataFile.getName();
    }

    private String getTransferKey( Metadata metadata, File metadataFile, RemoteRepository repository )
    {
        return metadataFile.getName() + '/' + getRepoKey( repository );
    }

    private String getRepoKey( RemoteRepository repository )
    {
        StringBuilder buffer = new StringBuilder( 128 );

        Proxy proxy = repository.getProxy();
        if ( proxy != null )
        {
            appendAuth( buffer, proxy.getAuthentication() );
            buffer.append( proxy.getHost() ).append( ':' ).append( proxy.getPort() ).append( '>' );
        }

        Authentication auth = repository.getAuthentication();
        appendAuth( buffer, auth );

        buffer.append( repository.getContentType() ).append( '-' );
        buffer.append( normalizeRepoUrl( repository.getUrl() ) );

        return buffer.toString();
    }

    private String normalizeRepoUrl( String url )
    {
        String result = url;
        if ( url != null && !url.endsWith( "/" ) )
        {
            result = url + '/';
        }
        return result;
    }

    private void appendAuth( StringBuilder buffer, Authentication auth )
    {
        if ( auth != null )
        {
            SimpleDigest digest = new SimpleDigest();
            digest.update( auth.getUsername() );
            digest.update( auth.getPassword() );
            digest.update( auth.getPrivateKeyFile() );
            digest.update( auth.getPassphrase() );
            buffer.append( digest.digest() ).append( '@' );
        }
    }

    public boolean isUpdatedRequired( RepositorySystemSession session, long lastModified, String policy )
    {
        boolean checkForUpdates;

        if ( policy == null )
        {
            policy = "";
        }

        if ( RepositoryPolicy.UPDATE_POLICY_ALWAYS.equals( policy ) )
        {
            checkForUpdates = true;
        }
        else if ( RepositoryPolicy.UPDATE_POLICY_DAILY.equals( policy ) )
        {
            Calendar cal = Calendar.getInstance();
            cal.set( Calendar.HOUR_OF_DAY, 0 );
            cal.set( Calendar.MINUTE, 0 );
            cal.set( Calendar.SECOND, 0 );
            cal.set( Calendar.MILLISECOND, 0 );

            checkForUpdates = cal.getTimeInMillis() > lastModified;
        }
        else if ( policy.startsWith( RepositoryPolicy.UPDATE_POLICY_INTERVAL ) )
        {
            String s = policy.substring( RepositoryPolicy.UPDATE_POLICY_INTERVAL.length() + 1 );
            int minutes = Integer.valueOf( s );

            Calendar cal = Calendar.getInstance();
            cal.add( Calendar.MINUTE, -minutes );

            checkForUpdates = cal.getTimeInMillis() > lastModified;
        }
        else
        {
            // assume "never"
            checkForUpdates = false;
        }

        return checkForUpdates;
    }

    private Properties read( File touchFile )
    {
        Properties props = new TrackingFileManager().setLogger( logger ).read( touchFile );
        return ( props != null ) ? props : new Properties();
    }

    public void touchArtifact( RepositorySystemSession session, UpdateCheck<Artifact, ArtifactTransferException> check )
    {
        Artifact artifact = check.getItem();
        File artifactFile = check.getFile();
        File touchFile = getTouchFile( artifact, artifactFile );

        String dataKey = getDataKey( artifact, artifactFile, check.getAuthoritativeRepository() );
        String transferKey = getTransferKey( artifact, artifactFile, check.getRepository() );

        Properties props = write( touchFile, dataKey, transferKey, check.getException() );

        if ( artifactFile.exists() && !hasErrors( props ) )
        {
            touchFile.delete();
        }
    }

    private boolean hasErrors( Properties props )
    {
        for ( Object key : props.keySet() )
        {
            if ( key.toString().endsWith( ERROR_KEY_SUFFIX ) )
            {
                return true;
            }
        }
        return false;
    }

    public void touchMetadata( RepositorySystemSession session, UpdateCheck<Metadata, MetadataTransferException> check )
    {
        Metadata metadata = check.getItem();
        File metadataFile = check.getFile();
        File touchFile = getTouchFile( metadata, metadataFile );

        String dataKey = getDataKey( metadata, metadataFile, check.getAuthoritativeRepository() );
        String transferKey = getTransferKey( metadata, metadataFile, check.getRepository() );

        write( touchFile, dataKey, transferKey, check.getException() );
    }

    private Properties write( File touchFile, String dataKey, String transferKey, Exception error )
    {
        Map<String, String> updates = new HashMap<String, String>();

        String timestamp = Long.toString( System.currentTimeMillis() );

        if ( error == null )
        {
            updates.put( dataKey + ERROR_KEY_SUFFIX, null );
            updates.put( dataKey + UPDATED_KEY_SUFFIX, timestamp );
            updates.put( transferKey + UPDATED_KEY_SUFFIX, null );
        }
        else if ( error instanceof ArtifactNotFoundException || error instanceof MetadataNotFoundException )
        {
            updates.put( dataKey + ERROR_KEY_SUFFIX, NOT_FOUND );
            updates.put( dataKey + UPDATED_KEY_SUFFIX, timestamp );
            updates.put( transferKey + UPDATED_KEY_SUFFIX, null );
        }
        else
        {
            String msg = error.getMessage();
            if ( msg == null || msg.length() <= 0 )
            {
                msg = error.getClass().getSimpleName();
            }
            updates.put( dataKey + ERROR_KEY_SUFFIX, msg );
            updates.put( dataKey + UPDATED_KEY_SUFFIX, null );
            updates.put( transferKey + UPDATED_KEY_SUFFIX, timestamp );
        }

        return new TrackingFileManager().setLogger( logger ).update( touchFile, updates );
    }

}
