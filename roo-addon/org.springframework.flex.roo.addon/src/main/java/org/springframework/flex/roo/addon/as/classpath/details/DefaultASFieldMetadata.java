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

import java.util.ArrayList;
import java.util.List;

import org.springframework.flex.roo.addon.as.classpath.details.metatag.ASMetaTagMetadata;
import org.springframework.flex.roo.addon.as.model.ASTypeVisibility;
import org.springframework.flex.roo.addon.as.model.ActionScriptSymbolName;
import org.springframework.flex.roo.addon.as.model.ActionScriptType;
import org.springframework.roo.support.util.Assert;

/**
 * Default metadata representation of an ActionScript field.
 *
 * @author Jeremy Grelle
 */
public class DefaultASFieldMetadata extends AbstractASFieldMetadata {

    private final String declaredByMetadataId;

    private final ActionScriptType fieldType;

    private final ActionScriptSymbolName fieldName;

    private final ASTypeVisibility visibility;

    private final String fieldInitializer;

    private List<ASMetaTagMetadata> metaTags = new ArrayList<ASMetaTagMetadata>();

    public DefaultASFieldMetadata(String declaredByMetadataId, ActionScriptType fieldType, ActionScriptSymbolName fieldName,
        ASTypeVisibility visibility, String fieldInitializer, List<ASMetaTagMetadata> metaTags) {
        Assert.hasText(declaredByMetadataId, "Declared by metadata ID required");
        Assert.notNull(fieldName, "Field name required");
        Assert.notNull(fieldType, "Field type required");

        this.declaredByMetadataId = declaredByMetadataId;
        this.fieldType = fieldType;
        this.fieldName = fieldName;
        this.visibility = visibility != null ? visibility : ASTypeVisibility.PUBLIC;
        this.fieldInitializer = fieldInitializer;

        if (metaTags != null) {
            this.metaTags = metaTags;
        }
    }

    @Override
    public String getDeclaredByMetadataId() {
        return this.declaredByMetadataId;
    }

    public ActionScriptType getFieldType() {
        return this.fieldType;
    }

    @Override
    public ActionScriptSymbolName getFieldName() {
        return this.fieldName;
    }

    public ASTypeVisibility getVisibility() {
        return this.visibility;
    }

    public List<ASMetaTagMetadata> getMetaTags() {
        return this.metaTags;
    }

    public String getFieldInitializer() {
        return this.fieldInitializer;
    }
}
