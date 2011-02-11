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

package org.springframework.flex.roo.addon.as.classpath.as3parser.details;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.flex.roo.addon.as.classpath.as3parser.As3ParserUtils;
import org.springframework.flex.roo.addon.as.classpath.as3parser.CompilationUnitServices;
import org.springframework.flex.roo.addon.as.classpath.details.metatag.ASMetaTagMetadata;
import org.springframework.flex.roo.addon.as.classpath.details.metatag.BooleanAttributeValue;
import org.springframework.flex.roo.addon.as.classpath.details.metatag.IntegerAttributeValue;
import org.springframework.flex.roo.addon.as.classpath.details.metatag.MetaTagAttributeValue;
import org.springframework.flex.roo.addon.as.classpath.details.metatag.StringAttributeValue;
import org.springframework.flex.roo.addon.as.model.ActionScriptSymbolName;
import org.springframework.roo.support.util.Assert;

import uk.co.badgersinfoil.metaas.dom.ASMetaTag;
import uk.co.badgersinfoil.metaas.dom.MetaTagable;

/**
 * Parser-specific metadata representation of an ActionScript meta-tag.
 *
 * @author Jeremy Grelle
 */
public class As3ParserMetaTagMetadata implements ASMetaTagMetadata {

    // provided
    private final String name;

    private final Map<ActionScriptSymbolName, MetaTagAttributeValue<?>> attributes = new LinkedHashMap<ActionScriptSymbolName, MetaTagAttributeValue<?>>();

    @SuppressWarnings("unchecked")
    public As3ParserMetaTagMetadata(ASMetaTag metaTag) {
        Assert.notNull(metaTag, "Meta Tag required");

        this.name = metaTag.getName();
        for (ASMetaTag.Param param : (List<ASMetaTag.Param>) metaTag.getParams()) {
            MetaTagAttributeValue<?> attr = As3ParserUtils.getMetaTagAttributeValue(param);
            this.attributes.put(attr.getName(), attr);
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

    @SuppressWarnings("unchecked")
    public static void addMetaTagToElement(CompilationUnitServices compilationUnitServices, ASMetaTagMetadata metaTag, MetaTagable element,
        boolean permitFlush) {

        Assert.notNull(compilationUnitServices, "Compilation unit services required");
        Assert.notNull(metaTag, "Metatag required");
        Assert.notNull(element, "Element required");

        for (ASMetaTag existingTag : (List<ASMetaTag>) element.getAllMetaTags()) {
            Assert.isTrue(!metaTag.getName().equals(existingTag.getName()), "Found an existing meta tag of type '" + metaTag.getName() + "'");
        }

        ASMetaTag newMetaTag = element.newMetaTag(metaTag.getName());
        for (ActionScriptSymbolName attrName : metaTag.getAttributeNames()) {
            MetaTagAttributeValue<?> value = metaTag.getAttribute(attrName);
            if (value instanceof BooleanAttributeValue) {
                newMetaTag.addParam(attrName.getSymbolName(), ((BooleanAttributeValue) value).getValue());
            } else if (value instanceof IntegerAttributeValue) {
                newMetaTag.addParam(attrName.getSymbolName(), ((IntegerAttributeValue) value).getValue());
            } else if (value instanceof StringAttributeValue) {
                newMetaTag.addParam(attrName.getSymbolName(), ((StringAttributeValue) value).getValue());
            } else {
                throw new IllegalArgumentException("Cannot add uknown meta tag attribute type.");
            }
        }

        if (permitFlush) {
            compilationUnitServices.flush();
        }
    }

    public static void removeMetatagFromElement(CompilationUnitServices compilationUnitServices, MetaTagable element, String name, boolean permitFlush) {
        Assert.notNull(compilationUnitServices, "Compilation unit services required");
        Assert.notNull(name, "Name required");
        Assert.notNull(element, "Element required");

        ASMetaTag tag = element.getFirstMetatag(name);

        Assert.notNull(tag, "Could not locate metatag '" + name + "' to delete");

        // TODO - this doesn't actually work as the getAllMetaTags list is an unmodifiable collections
        element.getAllMetaTags().remove(tag);

        // For this to work, we'll have to extend the existing parser and add in something like
        // MetaTagable#removeTag(String name)

        if (permitFlush) {
            compilationUnitServices.flush();
        }
    }

}
