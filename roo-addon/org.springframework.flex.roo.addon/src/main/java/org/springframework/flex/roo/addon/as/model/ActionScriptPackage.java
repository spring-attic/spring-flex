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

import org.springframework.roo.support.util.Assert;

/**
 * An ActionScript source package.
 *
 * @author Jeremy Grelle
 */
public class ActionScriptPackage implements Comparable<ActionScriptPackage> {

    private final String fullyQualifiedPackageName;

    public ActionScriptPackage(String fullyQualifiedPackageName) {
        Assert.notNull(fullyQualifiedPackageName, "Fully qualified package name required");
        ActionScriptSymbolName.assertActionScriptNameLegal(fullyQualifiedPackageName);
        Assert.isTrue(fullyQualifiedPackageName.toLowerCase().equals(fullyQualifiedPackageName), "The entire package name must be lowercase");
        this.fullyQualifiedPackageName = fullyQualifiedPackageName;
    }

    public String getFullyQualifiedPackageName() {
        return this.fullyQualifiedPackageName;
    }

    @Override
    public final int hashCode() {
        return this.fullyQualifiedPackageName.hashCode();
    }

    @Override
    public final boolean equals(Object obj) {
        return obj != null && obj instanceof ActionScriptPackage && this.compareTo((ActionScriptPackage) obj) == 0;
    }

    public final int compareTo(ActionScriptPackage o) {
        if (o == null) {
            return -1;
        }
        return this.fullyQualifiedPackageName.compareTo(o.fullyQualifiedPackageName);
    }

    @Override
    public final String toString() {
        return this.fullyQualifiedPackageName;
    }

}
