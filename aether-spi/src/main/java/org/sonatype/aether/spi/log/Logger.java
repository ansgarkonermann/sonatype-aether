package org.sonatype.aether.spi.log;

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
 * A simple logger to facilitate emission of diagnostic messages.
 * 
 * @author Benjamin Bentmann
 */
public interface Logger
{

    /**
     * Indicates whether debug logging is enabled.
     * 
     * @return {@code true} if debug logging is enabled, {@code false} otherwise.
     */
    boolean isDebugEnabled();

    /**
     * Emits the specified message.
     * 
     * @param msg The message to log, must not be {@code null}.
     */
    void debug( String msg );

    /**
     * Emits the specified message along with a stack trace of the given exception.
     * 
     * @param msg The message to log, must not be {@code null}.
     * @param error The exception to log, may be {@code null}.
     */
    void debug( String msg, Throwable error );

    /**
     * Indicates whether warn logging is enabled.
     * 
     * @return {@code true} if warn logging is enabled, {@code false} otherwise.
     */
    boolean isWarnEnabled();

    /**
     * Emits the specified message.
     * 
     * @param msg The message to log, must not be {@code null}.
     */
    void warn( String msg );

    /**
     * Emits the specified message along with a stack trace of the given exception.
     * 
     * @param msg The message to log, must not be {@code null}.
     * @param error The exception to log, may be {@code null}.
     */
    void warn( String msg, Throwable error );

}
