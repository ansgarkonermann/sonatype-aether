package org.sonatype.aether.repository;

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

import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.metadata.Metadata;

/**
 * A query to the local repository for the existence of metadata.
 * 
 * @see LocalRepositoryManager#find(RepositorySystemSession, LocalMetadataRequest)
 */
public class LocalMetadataRequest
{

    private Metadata metadata;

    private String context = "";

    private RemoteRepository repository = null;

    /**
     * Creates an uninitialized query.
     */
    public LocalMetadataRequest()
    {
        // enables default constructor
    }

    /**
     * Creates a query with the specified properties.
     * 
     * @param metadata The metadata to query for, may be {@code null}.
     * @param repository The source remote repository for the metadata, may be {@code null} for local metadata.
     * @param context The resolution context for the metadata, may be {@code null}.
     */
    public LocalMetadataRequest( Metadata metadata, RemoteRepository repository, String context )
    {
        setMetadata( metadata );
        setRepository( repository );
        setContext( context );
    }

    /**
     * Gets the metadata to query for.
     * 
     * @return The metadata or {@code null} if not set.
     */
    public Metadata getMetadata()
    {
        return metadata;
    }

    /**
     * Sets the metadata to query for.
     * 
     * @param metadata The metadata, may be {@code null}.
     * @return This query for chaining, never {@code null}.
     */
    public LocalMetadataRequest setMetadata( Metadata metadata )
    {
        this.metadata = metadata;
        return this;
    }

    /**
     * Gets the resolution context.
     * 
     * @return The resolution context, never {@code null}.
     */
    public String getContext()
    {
        return context;
    }

    /**
     * Sets the resolution context.
     * 
     * @param context The resolution context, may be {@code null}.
     * @return This query for chaining, never {@code null}.
     */
    public LocalMetadataRequest setContext( String context )
    {
        this.context = ( context != null ) ? context : "";
        return this;
    }

    /**
     * Gets the remote repository to use as source of the metadata.
     * 
     * @return The remote repositories, may be {@code null} for local metadata.
     */
    public RemoteRepository getRepository()
    {
        return repository;
    }

    /**
     * Sets the remote repository to use as sources of the metadata.
     * 
     * @param repository The remote repository, may be {@code null}.
     * @return This query for chaining, may be {@code null} for local metadata.
     */
    public LocalMetadataRequest setRepository( RemoteRepository repository )
    {
        this.repository = repository;
        return this;
    }

    @Override
    public String toString()
    {
        return getMetadata() + " @ " + getRepository();
    }

}
