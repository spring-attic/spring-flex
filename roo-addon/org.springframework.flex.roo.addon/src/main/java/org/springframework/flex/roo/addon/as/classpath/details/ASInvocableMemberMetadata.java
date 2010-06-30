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

import java.util.List;

import org.springframework.flex.roo.addon.as.classpath.details.metatag.ASMetaTagMetadata;
import org.springframework.flex.roo.addon.as.model.ActionScriptSymbolName;
import org.springframework.flex.roo.addon.as.model.ActionScriptType;

/**
 * Metadata concerning an invocable ActionScript member, namely a method or constructor.
 * 
 * @author Jeremy Grelle
 */
public interface ASInvocableMemberMetadata extends ASIdentifiableMember {

    /**
     * @return the parameter types (never null, but may be empty)
     */
    List<ActionScriptType> getParameterTypes();

    /**
     * @return the parameter names, if available (never null, but may be empty)
     */
    List<ActionScriptSymbolName> getParameterNames();

    /**
     * @return meta tags on this invocable member (never null, but may be empty)
     */
    List<ASMetaTagMetadata> getMetaTags();

    /**
     * @return the body of the method, if available (can be null if unavailable)
     */
    String getBody();

}