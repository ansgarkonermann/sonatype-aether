package org.sonatype.aether.util;

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
 * A utility class to ease string processing.
 * 
 * @author Benjamin Hanzelmann
 * @author Benjamin Bentmann
 */
public class StringUtils
{

    private StringUtils()
    {
        // hide constructor
    }

    /**
     * Checks whether a string is {@code null} or of zero length.
     * 
     * @param string The string to check, may be {@code null}.
     * @return {@code true} if the string is {@code null} or of zero length, {@code false} otherwise.
     */
    public static boolean isEmpty( String string )
    {
        return string == null || string.length() <= 0;
    }

}
