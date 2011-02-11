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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.flex.roo.addon.as.model.ActionScriptSymbolName;
import org.springframework.roo.support.util.Assert;

/**
 * Default metadata representation of an ActionScript meta-tag.
 *
 * @author Jeremy Grelle
 */
public class DefaultASMetaTagMetadata implements ASMetaTagMetadata {

    private final String name;

    private final Map<ActionScriptSymbolName, MetaTagAttributeValue<?>> attributes = new LinkedHashMap<ActionScriptSymbolName, MetaTagAttributeValue<?>>();

    public DefaultASMetaTagMetadata(String name, List<MetaTagAttributeValue<?>> attributes) {
        this.name = name;
        if (attributes != null) {
            for (MetaTagAttributeValue<?> attr : attributes) {
                this.attributes.put(attr.getName(), attr);
            }
        }
    }

    public String getName() {
        return this.name;
    }

    public MetaTagAttributeValue<?> getAttribute(ActionScriptSymbolName attributeName) {
        Assert.notNull(attributeName, "Attribute name required");
        return this.attributes.get(attributeName);
    }

    public List<ActionScriptSymbolName> getAttributeNames() {
        return new ArrayList<ActionScriptSymbolName>(this.attributes.keySet());
    }

}
