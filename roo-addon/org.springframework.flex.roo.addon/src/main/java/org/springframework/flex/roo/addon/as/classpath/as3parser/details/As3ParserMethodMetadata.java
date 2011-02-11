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
import org.springframework.flex.roo.addon.as.classpath.details.ASMethodMetadata;
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
 * Parser-specific metadata representation of an ActionScript method.
 *
 * @author Jeremy Grelle
 */
public class As3ParserMethodMetadata implements ASMethodMetadata {

    private final ActionScriptSymbolName methodName;

    private final ActionScriptType returnType;

    private final String declaredByMetadataId;

    private final List<ASMetaTagMetadata> metaTags = new ArrayList<ASMetaTagMetadata>();

    private String body = "";

    private final Map<ActionScriptSymbolName, ActionScriptType> params = new LinkedHashMap<ActionScriptSymbolName, ActionScriptType>();

    private final ASTypeVisibility visibility;

    @SuppressWarnings("unchecked")
    public As3ParserMethodMetadata(String declaredByMetadataId, ASMethod method, CompilationUnitServices compilationUnitServices) {
        Assert.notNull(declaredByMetadataId, "Declared by metadata ID required");
        Assert.notNull(method, "Method declaration required");
        Assert.notNull(compilationUnitServices, "Compilation unit services required");

        this.declaredByMetadataId = declaredByMetadataId;

        this.methodName = new ActionScriptSymbolName(method.getName());

        this.returnType = As3ParserUtils.getActionScriptType(compilationUnitServices.getCompilationUnitPackage(),
            compilationUnitServices.getImports(), method.getType());

        this.visibility = As3ParserUtils.getASTypeVisibility(method.getVisibility());

        List<ASMetaTag> metaTagList = method.getAllMetaTags();
        if (metaTagList != null) {
            for (ASMetaTag metaTag : metaTagList) {
                As3ParserMetaTagMetadata md = new As3ParserMetaTagMetadata(metaTag);
                this.metaTags.add(md);
            }
        }

        List<Statement> statements = method.getStatementList();
        for (Statement statement : statements) {
            this.body += statement.toString();
        }

        List<ASArg> args = method.getArgs();
        for (ASArg arg : args) {
            ActionScriptType paramType = As3ParserUtils.getActionScriptType(compilationUnitServices.getCompilationUnitPackage(),
                compilationUnitServices.getImports(), arg.getType());
            this.params.put(new ActionScriptSymbolName(arg.getName()), paramType);
        }
    }

    public ActionScriptSymbolName getMethodName() {
        return this.methodName;
    }

    public ActionScriptType getReturnType() {
        return this.returnType;
    }

    public List<ASMetaTagMetadata> getMetaTags() {
        return this.metaTags;
    }

    public String getBody() {
        return this.body;
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

    public static void addMethod(CompilationUnitServices compilationUnitServices, ASType type, ASMethodMetadata declaredMethod, boolean permitFlush) {

        Assert.isNull(type.getMethod(declaredMethod.getMethodName().getSymbolName()), "Method with name "
            + declaredMethod.getMethodName().getSymbolName() + " already exists and ActionScript does not allow method overloading.");

        As3ParserUtils.importTypeIfRequired(compilationUnitServices, declaredMethod.getReturnType());
        ASMethod method = type.newMethod(declaredMethod.getMethodName().getSymbolName(),
            As3ParserUtils.getAs3ParserVisiblity(declaredMethod.getVisibility()), declaredMethod.getReturnType().getSimpleTypeName());

        // TODO - The parser doesn't allow any control over re-ordering methods. It would be good if we could at the
        // very least ensure methods get added after the constructor.

        // Add MetaTags
        for (ASMetaTagMetadata metaTag : declaredMethod.getMetaTags()) {
            As3ParserMetaTagMetadata.addMetaTagToElement(compilationUnitServices, metaTag, method, false);
        }

        // Add Arguments
        for (int x = 0; x < declaredMethod.getParameterNames().size(); x++) {
            ActionScriptSymbolName argName = declaredMethod.getParameterNames().get(x);
            ActionScriptType argType = declaredMethod.getParameterTypes().get(x);
            As3ParserUtils.importTypeIfRequired(compilationUnitServices, argType);
            method.addParam(argName.getSymbolName(), argType.getSimpleTypeName());
        }

        if (permitFlush) {
            compilationUnitServices.flush();
        }
    }

}
