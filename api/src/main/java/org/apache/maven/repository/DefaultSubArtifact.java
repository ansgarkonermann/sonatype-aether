package org.apache.maven.repository;

import java.util.HashMap;
import java.util.Map;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * @author Benjamin Bentmann
 */
public class DefaultSubArtifact
    extends DerivedArtifact
    implements SubArtifact
{

    private final String classifier;

    private final String type;

    private Map<String, Object> properties = new HashMap<String, Object>();

    public DefaultSubArtifact( Artifact mainArtifact, String classifier, String type )
    {
        super( mainArtifact );
        this.classifier = classifier;
        this.type = type;
    }

    @Override
    public String getClassifier()
    {
        return expand( classifier, super.getClassifier() );
    }

    @Override
    public String getType()
    {
        return expand( type, super.getType() );
    }

    @Override
    public void setVersion( String version )
    {
        // ignored, version is always controlled by main artifact
    }

    private static String expand( String pattern, String replacement )
    {
        String result = "";
        if ( pattern != null )
        {
            result = pattern.replace( "*", replacement );

            if ( replacement.length() <= 0 )
            {
                if ( pattern.startsWith( "*" ) )
                {
                    int i = 0;
                    for ( ; i < result.length(); i++ )
                    {
                        char c = result.charAt( i );
                        if ( c != '-' && c != '.' )
                        {
                            break;
                        }
                    }
                    result = result.substring( i );
                }
                if ( pattern.endsWith( "*" ) )
                {
                    int i = result.length() - 1;
                    for ( ; i >= 0; i-- )
                    {
                        char c = result.charAt( i );
                        if ( c != '-' && c != '.' )
                        {
                            break;
                        }
                    }
                    result = result.substring( 0, i + 1 );
                }
            }
        }
        return result;
    }

    public <T> T getProperty( String key, Class<T> type, T defaultValue )
    {
        Object value = properties.get( key );
        return type.isInstance( value ) ? type.cast( value ) : defaultValue;
    }

    public Map<String, Object> getPropertiess()
    {
        return properties;
    }

    public DefaultSubArtifact setProperty( String key, Object value )
    {
        if ( value == null )
        {
            properties.remove( key );
        }
        else
        {
            properties.put( key, value );
        }
        return this;
    }

    public Artifact getMainArtifact()
    {
        return mainArtifact;
    }

    @Override
    public DefaultSubArtifact clone()
    {
        DefaultSubArtifact clone = (DefaultSubArtifact) super.clone();

        clone.properties = new HashMap<String, Object>( clone.properties );

        return clone;
    }

}