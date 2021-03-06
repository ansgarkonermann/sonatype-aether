package org.sonatype.aether.spi.locator;

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

import java.util.List;

/**
 * A simple infrastructure to programmatically wire the various components of the repository system together when it is
 * used outside of an IoC container. Once a concrete implementation of a service locator has been setup, clients could
 * use
 * 
 * <pre>
 * RepositorySystem repoSystem = serviceLocator.getService( RepositorySystem.class );
 * </pre>
 * 
 * to acquire the repository system. Components that implement {@link Service} will be given an opportunity to acquire
 * further components from the locator, thereby allowing to create the complete object graph of the repository system.
 * 
 * @author Benjamin Bentmann
 */
public interface ServiceLocator
{

    /**
     * Gets an instance of the specified service.
     * 
     * @param <T> The service type.
     * @param type The interface describing the service, must not be {@code null}.
     * @return The service instance or {@code null} if the service could not be located/initialized.
     */
    <T> T getService( Class<T> type );

    /**
     * Gets all available instances of the specified service.
     * 
     * @param <T> The service type.
     * @param type The interface describing the service, must not be {@code null}.
     * @return The (read-only) list of available service instances, never {@code null}.
     */
    <T> List<T> getServices( Class<T> type );

}
