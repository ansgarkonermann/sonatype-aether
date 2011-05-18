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

package org.sonatype.aether.impl.internal;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Pool of immutable object instances, used to avoid excessive memory consumption of (dirty) dependency graph which tends to have
 * many duplicate artifacts/dependencies.
 *
 * @author Benjamin Bentmann
 */
class ObjectPool<T> {

  private final Map<Object, Reference<T>> objects = new WeakHashMap<Object, Reference<T>>(256);

  public synchronized T intern(T object) {
    Reference<T> pooledRef = objects.get(object);
    if (pooledRef != null) {
      T pooled = pooledRef.get();
      if (pooled != null) {
        return pooled;
      }
    }

    objects.put(object, new WeakReference<T>(object));
    return object;
  }
}
