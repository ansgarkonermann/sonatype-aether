package org.sonatype.aether.util.graph.transformer;

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

/**
 * A collection of keys used by the dependency graph transformers when exchanging information via the graph
 * transformation context.
 * 
 * @author Benjamin Bentmann
 * @see org.sonatype.aether.collection.DependencyGraphTransformationContext#get(Object)
 */
public final class TransformationContextKeys
{

    /**
     * The key in the graph transformation context where a {@code Map<DependencyNode, Object>} is stored which maps
     * dependency nodes to their conflict ids. All nodes that map to an equal conflict id belong to the same group of
     * conflicting dependencies. Note that the map keys use reference equality.
     * 
     * @see ConflictMarker
     */
    public static final Object CONFLICT_IDS = "conflictIds";

    /**
     * The key in the graph transformation context where a {@code List<Object>} is stored that denotes a topological
     * sorting of the conflict ids.
     * 
     * @see ConflictIdSorter
     */
    public static final Object SORTED_CONFLICT_IDS = "sortedConflictIds";

    private TransformationContextKeys()
    {
        // hide constructor
    }

}
