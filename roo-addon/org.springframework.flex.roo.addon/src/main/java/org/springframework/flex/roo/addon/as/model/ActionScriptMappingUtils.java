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

package org.springframework.flex.roo.addon.as.model;

import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.flex.roo.addon.as.classpath.details.ASFieldMetadata;
import org.springframework.flex.roo.addon.as.classpath.details.DefaultASFieldMetadata;
import org.springframework.roo.classpath.details.DefaultFieldMetadata;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.model.DataType;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

/**
 * Utility class for mapping between Java and ActionScript types.
 *
 * @author Jeremy Grelle
 */
public abstract class ActionScriptMappingUtils {

    private static final Map<JavaType, ActionScriptType> javaToAmfTypeMap = new HashMap<JavaType, ActionScriptType>();

    private static final Map<ActionScriptType, JavaType> amfToJavaTypeMap = new HashMap<ActionScriptType, JavaType>();

    static {
        javaToAmfTypeMap.put(new JavaType(Enum.class.getName()), ActionScriptType.STRING_TYPE);
        javaToAmfTypeMap.put(JavaType.STRING_OBJECT, ActionScriptType.STRING_TYPE);
        javaToAmfTypeMap.put(JavaType.BOOLEAN_OBJECT, ActionScriptType.BOOLEAN_TYPE);
        javaToAmfTypeMap.put(JavaType.BOOLEAN_PRIMITIVE, ActionScriptType.BOOLEAN_TYPE);
        javaToAmfTypeMap.put(JavaType.INT_OBJECT, ActionScriptType.INT_TYPE);
        javaToAmfTypeMap.put(JavaType.INT_PRIMITIVE, ActionScriptType.INT_TYPE);
        javaToAmfTypeMap.put(JavaType.SHORT_OBJECT, ActionScriptType.INT_TYPE);
        javaToAmfTypeMap.put(JavaType.SHORT_PRIMITIVE, ActionScriptType.INT_TYPE);
        javaToAmfTypeMap.put(JavaType.BYTE_OBJECT, ActionScriptType.INT_TYPE);
        javaToAmfTypeMap.put(new JavaType(Byte.class.getName(), 1, DataType.PRIMITIVE, null, null), ActionScriptType.INT_TYPE);
        javaToAmfTypeMap.put(new JavaType(Byte.class.getName(), 1, DataType.TYPE, null, null), new ActionScriptType("flash.utils.ByteArray"));
        javaToAmfTypeMap.put(JavaType.DOUBLE_OBJECT, ActionScriptType.NUMBER_TYPE);
        javaToAmfTypeMap.put(JavaType.DOUBLE_PRIMITIVE, ActionScriptType.NUMBER_TYPE);
        javaToAmfTypeMap.put(JavaType.LONG_OBJECT, ActionScriptType.NUMBER_TYPE);
        javaToAmfTypeMap.put(JavaType.LONG_PRIMITIVE, ActionScriptType.NUMBER_TYPE);
        javaToAmfTypeMap.put(JavaType.FLOAT_OBJECT, ActionScriptType.NUMBER_TYPE);
        javaToAmfTypeMap.put(JavaType.FLOAT_PRIMITIVE, ActionScriptType.NUMBER_TYPE);
        javaToAmfTypeMap.put(JavaType.CHAR_OBJECT, ActionScriptType.STRING_TYPE);
        javaToAmfTypeMap.put(JavaType.CHAR_PRIMITIVE, ActionScriptType.STRING_TYPE);
        javaToAmfTypeMap.put(new JavaType(Character.class.getName(), 1, DataType.TYPE, null, null), ActionScriptType.STRING_TYPE);
        javaToAmfTypeMap.put(new JavaType(Character.class.getName(), 1, DataType.PRIMITIVE, null, null), ActionScriptType.STRING_TYPE);
        javaToAmfTypeMap.put(new JavaType(BigInteger.class.getName(), 0, DataType.TYPE, null, null), ActionScriptType.STRING_TYPE);
        javaToAmfTypeMap.put(new JavaType(BigDecimal.class.getName(), 0, DataType.TYPE, null, null), ActionScriptType.STRING_TYPE);
        javaToAmfTypeMap.put(new JavaType(Calendar.class.getName(), 0, DataType.TYPE, null, null), ActionScriptType.DATE_TYPE);
        javaToAmfTypeMap.put(new JavaType(Date.class.getName(), 0, DataType.TYPE, null, null), ActionScriptType.DATE_TYPE);

        amfToJavaTypeMap.put(ActionScriptType.STRING_TYPE, JavaType.STRING_OBJECT);
        amfToJavaTypeMap.put(ActionScriptType.BOOLEAN_TYPE, JavaType.BOOLEAN_OBJECT);
        amfToJavaTypeMap.put(ActionScriptType.INT_TYPE, JavaType.INT_OBJECT);
        amfToJavaTypeMap.put(ActionScriptType.NUMBER_TYPE, JavaType.DOUBLE_OBJECT);
        amfToJavaTypeMap.put(ActionScriptType.DATE_TYPE, new JavaType(Date.class.getName(), 0, DataType.TYPE, null, null));
        amfToJavaTypeMap.put(new ActionScriptType("flash.utils.ByteArray"), new JavaType(Byte.class.getName(), 1, DataType.TYPE, null, null));
        amfToJavaTypeMap.put(new ActionScriptType("mx.collections.ArrayCollection"), new JavaType(List.class.getName(), 0, DataType.TYPE, null, null));
        amfToJavaTypeMap.put(ActionScriptType.ARRAY_TYPE, new JavaType(List.class.getName(), 0, DataType.TYPE, null, null));
        amfToJavaTypeMap.put(ActionScriptType.OBJECT_TYPE, new JavaType(Map.class.getName(), 0, DataType.TYPE, null, null));
    }

    public static ActionScriptType toActionScriptType(JavaType javaType) {
        if (javaToAmfTypeMap.containsKey(javaType)) {
            return javaToAmfTypeMap.get(javaType);
        }

        if (javaType.isCommonCollectionType()) {
            if (javaType.getSimpleTypeName().endsWith("Map")) {
                if (javaType.isArray()) {
                    return ActionScriptType.ARRAY_TYPE;
                } else {
                    return ActionScriptType.OBJECT_TYPE;
                }
            } else {
                return new ActionScriptType("mx.collections.ArrayCollection");
            }
        }

        return new ActionScriptType(javaType.getFullyQualifiedTypeName(), (javaType.isArray() ? 1 : 0), ASDataType.TYPE);
    }

    public static JavaType toJavaType(ActionScriptType asType) {
        if (amfToJavaTypeMap.containsKey(asType)) {
            return amfToJavaTypeMap.get(asType);
        }

        return new JavaType(asType.getFullyQualifiedTypeName());
    }

    public static ActionScriptSymbolName toActionScriptSymbolName(JavaSymbolName name) {
        return new ActionScriptSymbolName(name.getSymbolName());
    }

    private static JavaSymbolName toJavaSymbolName(ActionScriptSymbolName fieldName) {
        return new JavaSymbolName(fieldName.getSymbolName());
    }

    public static ActionScriptPackage toActionScriptPackage(JavaPackage javaPackage) {
        return new ActionScriptPackage(javaPackage.getFullyQualifiedPackageName());
    }

    public static JavaPackage toJavaPackage(ActionScriptPackage asPackage) {
        return new JavaPackage(asPackage.getFullyQualifiedPackageName());
    }

    public static ASTypeVisibility toASTypeVisibility(int mod) {
        if (Modifier.isPublic(mod)) {
            return ASTypeVisibility.PUBLIC;
        } else if (Modifier.isProtected(mod)) {
            return ASTypeVisibility.PROTECTED;
        } else if (Modifier.isPrivate(mod)) {
            return ASTypeVisibility.PRIVATE;
        } else {
            return ASTypeVisibility.DEFAULT;
        }
    }

    private static int toJavaModifier(ASTypeVisibility visibility) {
        switch (visibility) {
            case PUBLIC:
                return Modifier.PUBLIC;
            case PRIVATE:
                return Modifier.PRIVATE;
            case PROTECTED:
                return Modifier.PROTECTED;
            case INTERNAL:
                return Modifier.PRIVATE;
            case DEFAULT:
            default:
                return 0;
        }
    }

    public static ASFieldMetadata toASFieldMetadata(String asEntityId, FieldMetadata javaField, boolean makePublic) {

        return new DefaultASFieldMetadata(asEntityId, toActionScriptType(javaField.getFieldType()),
            toActionScriptSymbolName(javaField.getFieldName()), (makePublic ? ASTypeVisibility.PUBLIC : toASTypeVisibility(javaField.getModifier())),
            toASFieldInitialzer(javaField), null);
    }

    public static String toASFieldInitialzer(FieldMetadata javaField) {

        ActionScriptType asType = toActionScriptType(javaField.getFieldType());
        if (asType.isNumeric()) {
            boolean isId = false;
            boolean isGenerated = false;

            for (AnnotationMetadata annotation : javaField.getAnnotations()) {
                if (annotation.getAnnotationType().getFullyQualifiedTypeName().equals("javax.persistence.Id")) {
                    isId = true;
                } else if (annotation.getAnnotationType().getFullyQualifiedTypeName().equals("javax.persistence.GeneratedValue")) {
                    isGenerated = true;
                }
            }

            if (isId && isGenerated) {
                return "-1";
            }

        }
        return null;
    }

    public static FieldMetadata toFieldMetadata(String javaEntityId, ASFieldMetadata asField, boolean makePrivate) {
        return new DefaultFieldMetadata(javaEntityId, (makePrivate ? Modifier.PRIVATE : toJavaModifier(asField.getVisibility())),
            toJavaSymbolName(asField.getFieldName()), toJavaType(asField.getFieldType()), null, null);
    }

    public static boolean isMappableType(ActionScriptType fieldType) {

        if (ActionScriptType.isImplicitType(fieldType.getFullyQualifiedTypeName())) {
            return false;
        }

        if (fieldType.getFullyQualifiedTypeName().startsWith("flash.") || fieldType.getFullyQualifiedTypeName().startsWith("mx.")) {
            return false;
        }

        return true;
    }
}
