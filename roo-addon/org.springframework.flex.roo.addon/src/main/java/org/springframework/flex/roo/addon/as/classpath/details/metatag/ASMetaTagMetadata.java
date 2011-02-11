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

package org.springframework.flex.roo.addon.as.classpath.details.metatag;

import java.util.List;

import org.springframework.flex.roo.addon.as.model.ActionScriptSymbolName;

/**
 * Metadata representation of an ActionScript meta-tag.
 *
 * @author Jeremy Grelle
 */
public interface ASMetaTagMetadata {

    String getName();

    /**
     * @return the attribute names, preferably in the order they are declared in the annotation (never null, but may be
     *         empty)
     */
    List<ActionScriptSymbolName> getAttributeNames();

    /**
     * Acquires an attribute value for the requested name.
     * 
     * @return the requested attribute (or null if not found)
     */
    MetaTagAttributeValue<?> getAttribute(ActionScriptSymbolName attributeName);
}
