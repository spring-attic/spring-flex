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

import org.springframework.flex.roo.addon.as.model.ActionScriptSymbolName;

/**
 * Convenient base class for metadata representation of an ActionScript field.
 *
 * @author Jeremy Grelle
 */
public abstract class AbstractASFieldMetadata implements ASFieldMetadata {

    public abstract ActionScriptSymbolName getFieldName();

    public abstract String getDeclaredByMetadataId();

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (getDeclaredByMetadataId() == null ? 0 : getDeclaredByMetadataId().hashCode());
        result = prime * result + (getFieldName() == null ? 0 : getFieldName().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!ASFieldMetadata.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        ASFieldMetadata other = (ASFieldMetadata) obj;
        if (getDeclaredByMetadataId() == null) {
            if (other.getDeclaredByMetadataId() != null) {
                return false;
            }
        } else if (!getDeclaredByMetadataId().equals(other.getDeclaredByMetadataId())) {
            return false;
        }
        if (getFieldName() == null) {
            if (other.getFieldName() != null) {
                return false;
            }
        } else if (!getFieldName().equals(other.getFieldName())) {
            return false;
        }
        return true;
    }

}