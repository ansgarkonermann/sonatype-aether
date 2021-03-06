package org.sonatype.aether.transfer;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/

import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;

/**
 * @author Benjamin Bentmann
 */
public class ArtifactNotFoundException
    extends ArtifactTransferException
{

    public ArtifactNotFoundException( Artifact artifact, RemoteRepository repository )
    {
        super( artifact, repository, "Could not find artifact " + artifact + getString( " in ", repository )
            + getLocalPathInfo( artifact, repository ) );
    }

    private static String getLocalPathInfo( Artifact artifact, RemoteRepository repository )
    {
        String localPath = ( artifact != null ) ? artifact.getProperty( "localPath", null ) : null;
        if ( localPath != null && repository == null )
        {
            return " at specified path " + localPath;
        }
        else
        {
            return "";
        }
    }

    public ArtifactNotFoundException( Artifact artifact, RemoteRepository repository, String message )
    {
        super( artifact, repository, message );
    }

}
