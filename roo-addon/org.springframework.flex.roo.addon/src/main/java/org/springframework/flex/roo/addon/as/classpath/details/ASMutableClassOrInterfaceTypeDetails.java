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

import org.springframework.flex.roo.addon.as.classpath.details.metatag.ASMetaTagMetadata;
import org.springframework.flex.roo.addon.as.model.ActionScriptSymbolName;

/**
 * Details about the different members in a class or interface.
 * 
 * @author Jeremy Grelle
 */
public interface ASMutableClassOrInterfaceTypeDetails extends ASClassOrInterfaceTypeDetails {

    /**
     * Adds a new type-level meta tag. There must not already be an equivalent meta tag of this defined on the type.
     * 
     * @param metaTag to add (required)
     * @param flush changes to disk immediately
     */
    void addTypeMetaTag(ASMetaTagMetadata metaTag, boolean flush);

    /**
     * Removes the type-level meta tag of the name indicated. This meta tag must already exist.
     * 
     * @param name of the meta tag to remove (required)
     * @param flush changes to disk immediately
     */
    void removeTypeMetaTag(String name, boolean flush);

    /**
     * Adds a new field. There must not be a field of this name already existing.
     * 
     * @param fieldMetadata to add (required)
     * @param flush changes to disk immediately
     */
    void addField(ASFieldMetadata fieldMetadata, boolean flush);

    /**
     * Removes an existing field. A field with the specified name must already exist.
     * 
     * @param fieldName to remove (required)
     * @param flush changes to disk immediately
     */
    void removeField(ActionScriptSymbolName fieldName, boolean flush);

    /**
     * Adds a new method. A method with the same name and parameter types must not already exist.
     * 
     * @param methodMetadata to add (required)
     * @param flush changes to disk immediately
     */
    void addMethod(ASMethodMetadata methodMetadata, boolean flush);

    /**
     * Adds a new type-level meta tag. There must not already be an equivalent meta tag of this defined on the type.
     * 
     * Changes will be immediately written to disk.
     * 
     * @param metaTag to add (required)
     */
    void addTypeMetaTag(ASMetaTagMetadata metaTag);

    /**
     * Removes the type-level meta tag of the name indicated. This meta tag must already exist.
     * 
     * Changes will be immediately written to disk.
     * 
     * @param name of the meta tag to remove (required)
     */
    void removeTypeMetaTag(String name);

    /**
     * Adds a new field. There must not be a field of this name already existing.
     * 
     * Changes will be immediately written to disk.
     * 
     * @param fieldMetadata to add (required)
     */
    void addField(ASFieldMetadata fieldMetadata);

    /**
     * Removes an existing field. A field with the specified name must already exist.
     * 
     * Changes will be immediately written to disk.
     * 
     * @param fieldName to remove (required)
     */
    void removeField(ActionScriptSymbolName fieldName);

    /**
     * Adds a new method. A method with the same name and parameter types must not already exist.
     * 
     * Changes will be immediately written to disk.
     * 
     * @param methodMetadata to add (required)
     */
    void addMethod(ASMethodMetadata methodMetadata);

    /**
     * Commit changes to disk
     */
    void commit();

    void updateField(ASFieldMetadata fieldMetadata, boolean flush);

    void updateField(ASFieldMetadata fieldMetadata);
}
