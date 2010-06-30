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

package org.springframework.flex.roo.addon.as.classpath.details;

import org.springframework.flex.roo.addon.as.model.ASTypeVisibility;

/**
 * Allows an ActionScript member to be traced back to its declaring type.
 * 
 * @author Jeremy Grelle
 */
public interface ASIdentifiableMember {

    /**
     * @return the ID of the metadata that declared this member (never null)
     */
    String getDeclaredByMetadataId();

    /**
     * Indicates the visibility of the member.
     * 
     * @return the visibility, if available (required)
     */
    ASTypeVisibility getVisibility();

}
