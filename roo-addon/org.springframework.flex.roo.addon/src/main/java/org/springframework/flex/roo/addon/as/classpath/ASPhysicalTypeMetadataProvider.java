/*
 * Copyright 2002-2010 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.flex.roo.addon.as.classpath;

import org.springframework.flex.roo.addon.as.model.ActionScriptType;
import org.springframework.roo.metadata.MetadataProvider;
import org.springframework.roo.project.ClasspathProvidingProjectMetadata;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectMetadata;

/**
 * {@link MetadataProvider} for ActionScript source files.
 *
 * @author Jeremy Grelle
 */
public interface ASPhysicalTypeMetadataProvider extends MetadataProvider {

    /**
     * Attempts to locate the specified {@link ActionScriptType} by searching the physical disk (does not search for
     * existing {@link ASPhysicalTypeMetadata}).
     * 
     * <p>
     * This method resolves the issue that a {@link ActionScriptType} is location independent, yet
     * {@link ASPhysicalTypeIdentifier} instances are location dependent (ie a {@link ASPhysicalTypeIdentifier} relates
     * to a given physical file, whereas a {@link ActionScriptType} simply assumes the type is available from the
     * classpath). This resolution is achieved by first scanning the {@link PathResolver#getSourcePaths()} locations,
     * and then scanning any locations provided by the {@link ClasspathProvidingProjectMetadata} (if the
     * {@link ProjectMetadata} implements this extended interface).
     * 
     * <p>
     * Due to the "best effort" basis of classpath resolution, callers should not rely on complex classpath resolution
     * outcomes. However, callers can rely on robust determination of types defined in {@link Path}s from
     * {@link PathResolver#getSourcePaths()}, using the {@link Path} order returned by that method.
     * 
     * @param actionScriptType the type to locate (required)
     * @return the string (in {@link ASPhysicalTypeIdentifier} format) if found, or null if not found
     */
    String findIdentifier(ActionScriptType actionScriptType);
}
