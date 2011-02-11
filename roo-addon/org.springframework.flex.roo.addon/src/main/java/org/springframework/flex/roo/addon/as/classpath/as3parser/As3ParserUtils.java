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

package org.springframework.flex.roo.addon.as.classpath.as3parser;

import java.util.List;

import org.springframework.flex.roo.addon.as.classpath.details.metatag.BooleanAttributeValue;
import org.springframework.flex.roo.addon.as.classpath.details.metatag.IntegerAttributeValue;
import org.springframework.flex.roo.addon.as.classpath.details.metatag.MetaTagAttributeValue;
import org.springframework.flex.roo.addon.as.classpath.details.metatag.StringAttributeValue;
import org.springframework.flex.roo.addon.as.model.ASTypeVisibility;
import org.springframework.flex.roo.addon.as.model.ActionScriptPackage;
import org.springframework.flex.roo.addon.as.model.ActionScriptSymbolName;
import org.springframework.flex.roo.addon.as.model.ActionScriptType;
import org.springframework.roo.support.util.Assert;

import uk.co.badgersinfoil.metaas.dom.ASType;
import uk.co.badgersinfoil.metaas.dom.Visibility;
import uk.co.badgersinfoil.metaas.dom.ASMetaTag.Param;

/**
 * Utility methods for working with the ActionScript parser implementation.
 *
 * @author Jeremy Grelle
 */
public class As3ParserUtils {

    // private static final ActionScriptFactory factory = new ActionScriptFactory();

    public static final ActionScriptType getActionScriptType(ActionScriptPackage compilationUnitPackage, List<String> imports, ASType type) {
        Assert.notNull(imports, "Compilation unit imports required");
        Assert.notNull(compilationUnitPackage, "Compilation unit package required");
        Assert.notNull(type, "ASType required");

        return getActionScriptType(compilationUnitPackage, imports, type.getName());
    }

    public static final ActionScriptType getActionScriptType(ActionScriptPackage compilationUnitPackage, List<String> imports, String nameToFind) {
        Assert.notNull(imports, "Compilation unit imports required");
        Assert.notNull(compilationUnitPackage, "Compilation unit package required");
        Assert.notNull(nameToFind, "Name to find is required");

        int offset = nameToFind.lastIndexOf('.');
        if (offset > -1) {
            return new ActionScriptType(nameToFind);
        }

        if (ActionScriptType.isImplicitType(nameToFind)) {
            return new ActionScriptType(nameToFind);
        }

        String importDeclaration = getImportDeclarationFor(imports, nameToFind);
        if (importDeclaration == null) {
            String name = compilationUnitPackage.getFullyQualifiedPackageName() == "" ? nameToFind
                : compilationUnitPackage.getFullyQualifiedPackageName() + "." + nameToFind;
            return new ActionScriptType(name);
        }

        return new ActionScriptType(importDeclaration);
    }

    /*
     * public static final ASType getASType(String typeName) { Assert.notNull(typeName, "ActionScript type required");
     * return factory.newClass(typeName).getType(); }
     */

    private static final String getImportDeclarationFor(List<String> imports, String typeName) {
        Assert.notNull(imports, "Compilation unit imports required");
        Assert.notNull(typeName, "Type name required");
        for (String candidate : imports) {
            int offset = candidate.lastIndexOf('.');
            if (typeName.equals(candidate.substring(offset + 1))) {
                return candidate;
            }
        }
        return null;
    }

    public static void importTypeIfRequired(CompilationUnitServices compilationUnitServices, ActionScriptType typeToImport) {
        Assert.notNull(compilationUnitServices, "Compilation unit services is required");
        Assert.notNull(typeToImport, "ActionScript type to import is required");

        if (ActionScriptType.isImplicitType(typeToImport.getFullyQualifiedTypeName())) {
            return;
        }

        if (typeToImport.isDefaultPackage()) {
            return;
        }

        if (compilationUnitServices.getCompilationUnitPackage().equals(typeToImport.getPackage())) {
            return;
        }

        if (compilationUnitServices.getImports().contains(typeToImport.getFullyQualifiedTypeName())) {
            return;
        }

        compilationUnitServices.addImport(typeToImport.getFullyQualifiedTypeName());
    }

    public static ASTypeVisibility getASTypeVisibility(Visibility as3ParserVisibility) {
        return ASTypeVisibility.valueOf(as3ParserVisibility.toString().replaceAll("\\[|\\]", "").toUpperCase());
    }

    public static Visibility getAs3ParserVisiblity(ASTypeVisibility typeVisibility) {
        switch (typeVisibility) {
            case INTERNAL:
                return Visibility.INTERNAL;
            case PRIVATE:
                return Visibility.PRIVATE;
            case PROTECTED:
                return Visibility.PROTECTED;
            case PUBLIC:
                return Visibility.PUBLIC;
            case DEFAULT:
            default:
                return Visibility.DEFAULT;

        }
    }

    public static MetaTagAttributeValue<?> getMetaTagAttributeValue(Param param) {
        if (param.getValue() instanceof Boolean) {
            return new BooleanAttributeValue(new ActionScriptSymbolName(param.getName()), (Boolean) param.getValue());
        } else if (param.getValue() instanceof Integer) {
            return new IntegerAttributeValue(new ActionScriptSymbolName(param.getName()), (Integer) param.getValue());
        } else {
            return new StringAttributeValue(new ActionScriptSymbolName(param.getName()), String.valueOf(param.getValue()));
        }
    }
}
