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
import org.springframework.flex.roo.addon.as.classpath.details.ASConstructorMetadata;
import org.springframework.flex.roo.addon.as.classpath.details.metatag.ASMetaTagMetadata;
import org.springframework.flex.roo.addon.as.model.ASTypeVisibility;
import org.springframework.flex.roo.addon.as.model.ActionScriptSymbolName;
import org.springframework.flex.roo.addon.as.model.ActionScriptType;
import org.springframework.roo.support.util.Assert;

import uk.co.badgersinfoil.metaas.dom.ASArg;
import uk.co.badgersinfoil.metaas.dom.ASMetaTag;
import uk.co.badgersinfoil.metaas.dom.ASMethod;
import uk.co.badgersinfoil.metaas.dom.ASType;
import uk.co.badgersinfoil.metaas.dom.Statement;

/**
 * Parser-specific metadata representation of an ActionScript constructor method.
 *
 * @author Jeremy Grelle
 */
public class As3ParserConstructorMetadata implements ASConstructorMetadata {

    private final String declaredByMetadataId;

    private final String methodBody;

    private final List<ASMetaTagMetadata> metaTags = new ArrayList<ASMetaTagMetadata>();

    private final Map<ActionScriptSymbolName, ActionScriptType> params = new LinkedHashMap<ActionScriptSymbolName, ActionScriptType>();

    private final ASTypeVisibility visibility;

    @SuppressWarnings("unchecked")
    public As3ParserConstructorMetadata(String declaredByMetadataId, ASMethod method, CompilationUnitServices compilationUnitServices) {
        Assert.notNull(declaredByMetadataId, "Declared by metadata ID required");
        Assert.notNull(method, "Method declaration required");
        Assert.notNull(compilationUnitServices, "Compilation unit services required");

        this.declaredByMetadataId = declaredByMetadataId;

        StringBuffer bodyBuf = new StringBuffer();
        for (Statement statement : (List<Statement>) method.getStatementList()) {
            bodyBuf.append(statement.toString());
        }
        this.methodBody = bodyBuf.toString();

        List<ASMetaTag> metaTagList = method.getAllMetaTags();
        if (metaTagList != null) {
            for (ASMetaTag metaTag : metaTagList) {
                As3ParserMetaTagMetadata md = new As3ParserMetaTagMetadata(metaTag);
                this.metaTags.add(md);
            }
        }

        List<ASArg> args = method.getArgs();
        for (ASArg arg : args) {
            ActionScriptType paramType = As3ParserUtils.getActionScriptType(compilationUnitServices.getCompilationUnitPackage(),
                compilationUnitServices.getImports(), arg.getType());
            this.params.put(new ActionScriptSymbolName(arg.getName()), paramType);
        }

        this.visibility = As3ParserUtils.getASTypeVisibility(method.getVisibility());
    }

    public String getBody() {
        return this.methodBody;
    }

    public List<ASMetaTagMetadata> getMetaTags() {
        return this.metaTags;
    }

    public List<ActionScriptSymbolName> getParameterNames() {
        return new ArrayList<ActionScriptSymbolName>(this.params.keySet());
    }

    public List<ActionScriptType> getParameterTypes() {
        return new ArrayList<ActionScriptType>(this.params.values());
    }

    public String getDeclaredByMetadataId() {
        return this.declaredByMetadataId;
    }

    public ASTypeVisibility getVisibility() {
        return this.visibility;
    }

    public static void addConstructor(CompilationUnitServices compilationUnitServices, ASType type, ASConstructorMetadata declaredConstructor,
        boolean permitFlush) {

        Assert.isNull(type.getMethod(type.getName()), "ActionScript classes may only have one constructor method.");

        ASMethod constructor = type.newMethod(type.getName(), As3ParserUtils.getAs3ParserVisiblity(declaredConstructor.getVisibility()), null);

        // TODO - The parser doesn't allow any control over re-ordering methods. It would be good if we could ensure the
        // constructor is the first method in the class.

        // Add MetaTags
        for (ASMetaTagMetadata metaTag : declaredConstructor.getMetaTags()) {
            As3ParserMetaTagMetadata.addMetaTagToElement(compilationUnitServices, metaTag, constructor, false);
        }

        // Add Arguments
        for (int x = 0; x < declaredConstructor.getParameterNames().size(); x++) {
            ActionScriptSymbolName argName = declaredConstructor.getParameterNames().get(x);
            ActionScriptType argType = declaredConstructor.getParameterTypes().get(x);
            As3ParserUtils.importTypeIfRequired(compilationUnitServices, argType);
            constructor.addParam(argName.getSymbolName(), argType.getSimpleTypeName());
        }

        if (permitFlush) {
            compilationUnitServices.flush();
        }
    }
}
