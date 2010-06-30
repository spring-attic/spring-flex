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

import java.util.HashSet;
import java.util.Set;

/**
 * An ActionScript type.
 *
 * @author Jeremy Grelle
 */
public final class ActionScriptType implements Comparable<ActionScriptType> {

    private int array = 0;

    private final boolean defaultPackage;

    private final ASDataType dataType;

    private final String fullyQualifiedTypeName;

    private String simpleTypeName;

    public static final ActionScriptType BOOLEAN_TYPE = new ActionScriptType("Boolean");

    public static final ActionScriptType INT_TYPE = new ActionScriptType("int");

    public static final ActionScriptType NULL_TYPE = new ActionScriptType("Null");

    public static final ActionScriptType NUMBER_TYPE = new ActionScriptType("Number");

    public static final ActionScriptType STRING_TYPE = new ActionScriptType("String");

    public static final ActionScriptType UINT_TYPE = new ActionScriptType("uint");

    public static final ActionScriptType VOID_TYPE = new ActionScriptType("void");

    public static final ActionScriptType OBJECT_TYPE = new ActionScriptType("Object");

    public static final ActionScriptType ARRAY_TYPE = new ActionScriptType("Array");

    public static final ActionScriptType DATE_TYPE = new ActionScriptType("Date");

    public static final ActionScriptType ERROR_TYPE = new ActionScriptType("Error");

    public static final ActionScriptType FUNCTION_TYPE = new ActionScriptType("Function");

    public static final ActionScriptType REGEXP_TYPE = new ActionScriptType("RegExp");

    public static final ActionScriptType XML_TYPE = new ActionScriptType("XML");

    public static final ActionScriptType XML_LIST_TYPE = new ActionScriptType("XMLList");

    private static final Set<String> implicitTypes = new HashSet<String>();

    static {
        implicitTypes.add("Boolean");
        implicitTypes.add("int");
        implicitTypes.add("Null");
        implicitTypes.add("Number");
        implicitTypes.add("String");
        implicitTypes.add("uint");
        implicitTypes.add("void");
        implicitTypes.add("Object");
        implicitTypes.add("Array");
        implicitTypes.add("Date");
        implicitTypes.add("Error");
        implicitTypes.add("Function");
        implicitTypes.add("RegExp");
        implicitTypes.add("XML");
        implicitTypes.add("XMLList");
    }

    public ActionScriptType(String fullyQualifiedTypeName) {
        this(fullyQualifiedTypeName, 0, ASDataType.TYPE);
    }

    public ActionScriptType(String fullyQualifiedTypeName, int array, ASDataType dataType) {
        if (fullyQualifiedTypeName == null || fullyQualifiedTypeName.length() == 0) {
            throw new IllegalArgumentException("Fully qualified type name required");
        }
        ActionScriptSymbolName.assertActionScriptNameLegal(fullyQualifiedTypeName);
        this.fullyQualifiedTypeName = fullyQualifiedTypeName;
        this.defaultPackage = !fullyQualifiedTypeName.contains(".");
        if (this.defaultPackage) {
            this.simpleTypeName = fullyQualifiedTypeName;
        } else {
            int offset = fullyQualifiedTypeName.lastIndexOf(".");
            this.simpleTypeName = fullyQualifiedTypeName.substring(offset + 1);
        }
        if (!this.defaultPackage && !Character.isUpperCase(this.simpleTypeName.charAt(0))) {
            throw new IllegalArgumentException("The first letter of the type name portion must be uppercase (attempted '" + fullyQualifiedTypeName
                + "')");
        }

        this.array = array;
        this.dataType = dataType;
    }

    public ActionScriptPackage getPackage() {
        if (isDefaultPackage()) {
            return new ActionScriptPackage("");
        }
        int offset = this.fullyQualifiedTypeName.lastIndexOf(".");
        return new ActionScriptPackage(this.fullyQualifiedTypeName.substring(0, offset));
    }

    public boolean isDefaultPackage() {
        return this.defaultPackage;
    }

    @Override
    public final int hashCode() {
        return this.fullyQualifiedTypeName.hashCode();
    }

    public boolean isArray() {
        return this.array > 0;
    }

    public boolean isNumeric() {
        return this.equals(INT_TYPE) || this.equals(NUMBER_TYPE);
    }

    public int getArray() {
        return this.array;
    }

    /**
     * @return the name (does not contain any periods; never null or empty)
     */
    public String getSimpleTypeName() {
        return this.simpleTypeName;
    }

    /**
     * @return the fully qualified name (complies with the rules specified in the constructor)
     */
    public String getFullyQualifiedTypeName() {
        return this.fullyQualifiedTypeName;
    }

    public ASDataType getDataType() {
        return this.dataType;
    }

    @Override
    public final boolean equals(Object obj) {
        // NB: Not using the normal convention of delegating to compareTo (for efficiency reasons)
        return obj != null && obj instanceof ActionScriptType && this.fullyQualifiedTypeName.equals(((ActionScriptType) obj).fullyQualifiedTypeName)
            && this.dataType == ((ActionScriptType) obj).dataType;
    }

    public final int compareTo(ActionScriptType o) {
        // NB: If adding more fields to this class ensure the equals(Object) method is updated accordingly
        if (o == null) {
            return -1;
        }
        return this.fullyQualifiedTypeName.compareTo(o.fullyQualifiedTypeName);
    }

    public static boolean isImplicitType(String typeName) {
        return implicitTypes.contains(typeName);
    }
}
