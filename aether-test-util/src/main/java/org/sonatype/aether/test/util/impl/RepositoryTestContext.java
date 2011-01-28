package org.sonatype.aether.test.util.impl;

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
import org.sonatype.aether.test.impl.RecordingRepositoryListener;
import org.sonatype.aether.test.impl.TestRepositorySystemSession;

public class RepositoryTestContext
{
    TestRepositorySystemSession session;

    Artifact artifact;

    public TestRepositorySystemSession getSession()
    {
        return session;
    }

    public void setSession( TestRepositorySystemSession session )
    {
        this.session = session;
    }

    public Artifact getArtifact()
    {
        return artifact;
    }

    public void setArtifact( Artifact artifact )
    {
        this.artifact = artifact;
    }

    public RecordingRepositoryListener getRecordingRepositoryListener()
    {
        if ( session.getRepositoryListener() instanceof RecordingRepositoryListener )
        {
            return (RecordingRepositoryListener) session.getRepositoryListener();
        }
        else
        {
            return new RecordingRepositoryListener( session.getRepositoryListener() );
        }
    }
}
