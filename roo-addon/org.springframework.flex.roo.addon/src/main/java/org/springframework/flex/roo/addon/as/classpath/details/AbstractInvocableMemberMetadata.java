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
 * Convenient base class for metadata representation of an invocable ActionScript member.
 *
 * @author Jeremy Grelle
 */
public abstract class AbstractInvocableMemberMetadata implements ASInvocableMemberMetadata {

    private final String declaredByMetadataId;

    private final String methodBody;

    private List<ASMetaTagMetadata> metaTags = new ArrayList<ASMetaTagMetadata>();

    private List<ActionScriptType> paramTypes = new ArrayList<ActionScriptType>();

    private List<ActionScriptSymbolName> paramNames = new ArrayList<ActionScriptSymbolName>();

    private final ASTypeVisibility visibility;

    public AbstractInvocableMemberMetadata(String declaredByMetadataId) {
        this(declaredByMetadataId, null, null, null, null, null);
    }

    public AbstractInvocableMemberMetadata(String declaredByMetadataId, String methodBody, List<ASMetaTagMetadata> metaTags,
        List<ActionScriptType> paramTypes, List<ActionScriptSymbolName> paramNames, ASTypeVisibility visibility) {
        Assert.notNull(declaredByMetadataId, "Metadata ID of owning type is required.");

        this.declaredByMetadataId = declaredByMetadataId;
        this.visibility = visibility != null ? visibility : ASTypeVisibility.PUBLIC;
        this.methodBody = methodBody != null ? methodBody : "";

        if (metaTags != null) {
            this.metaTags = metaTags;
        }

        if (paramTypes != null) {
            this.paramTypes = paramTypes;
        }

        if (paramNames != null) {
            this.paramNames = paramNames;
        }
    }

    public String getDeclaredByMetadataId() {
        return this.declaredByMetadataId;
    }

    public String getBody() {
        return this.methodBody;
    }

    public List<ASMetaTagMetadata> getMetaTags() {
        return this.metaTags;
    }

    public List<ActionScriptType> getParameterTypes() {
        return this.paramTypes;
    }

    public List<ActionScriptSymbolName> getParameterNames() {
        return this.paramNames;
    }

    public ASTypeVisibility getVisibility() {
        return this.visibility;
    }
}
