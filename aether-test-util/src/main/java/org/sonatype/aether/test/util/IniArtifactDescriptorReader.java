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
 ******************************************************************************/

package org.sonatype.aether.test.util;

import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.resolution.ArtifactDescriptorException;
import org.sonatype.aether.resolution.ArtifactDescriptorRequest;
import org.sonatype.aether.resolution.ArtifactDescriptorResult;

/**
 * Uses a {@link IniArtifactDataReader} to parse an artifact description. <p> Note: May not directly implement
 * ArtifactDescriptorReader from aether-impl because of circular dependencies, and has to be wrapped for use in test classes.
 *
 * @author Benjamin Hanzelmann
 */
public class IniArtifactDescriptorReader {

  private IniArtifactDataReader reader;

  /** Use the given prefix to load the artifact descriptions. */
  public IniArtifactDescriptorReader(String prefix) {
    reader = new IniArtifactDataReader(prefix);
  }

  /**
   * Parses the resource <code>$prefix/gid_aid_ext_ver.ini</code> from the request artifact as an artifact description and wraps
   * it into an ArtifactDescriptorResult.
   */
  public ArtifactDescriptorResult readArtifactDescriptor(RepositorySystemSession session,
                                                         ArtifactDescriptorRequest request)
      throws ArtifactDescriptorException {
    Artifact artifact = request.getArtifact();
    String resourceName =
        String.format("%s_%s_%s_%s.ini", artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(),
                      artifact.getExtension());

    ArtifactDescriptorResult result = new ArtifactDescriptorResult(request);
    result.setArtifact(artifact);

    try {
      ArtifactDescription data = reader.parse(resourceName);
      result.setDependencies(data.getDependencies());
      result.setManagedDependencies(data.getManagedDependencies());
      result.setRepositories(data.getRepositories());
      result.setRelocations(data.getRelocations());
      return result;
    }
    catch (Exception e) {
      throw new ArtifactDescriptorException(result, e.getMessage());
    }
  }
}
