package org.sonatype.aether.collection;

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

import org.sonatype.aether.graph.Dependency;

/**
 * Decides whether the dependencies of a dependency node should be traversed as well.
 * 
 * @author Benjamin Bentmann
 */
public interface DependencyTraverser
{

    /**
     * Decides whether the dependencies of the specified dependency should be traversed.
     * 
     * @param dependency The dependency to check, must not be {@code null}.
     * @return {@code true} if the dependency graph builder should recurse into the specified dependency and process its
     *         dependencies, {@code false} otherwise.
     */
    boolean traverseDependency( Dependency dependency );

    /**
     * Derives a dependency traverser that will be used to decide whether the transitive dependencies of the dependency
     * given in the collection context shall be traversed. When calculating the child traverser, implementors are
     * strongly advised to simply return the current instance if nothing changed to help save memory.
     * 
     * @param context The dependency collection context, must not be {@code null}.
     * @return The dependency traverser for the target node, must not be {@code null}.
     */
    DependencyTraverser deriveChildTraverser( DependencyCollectionContext context );

}
