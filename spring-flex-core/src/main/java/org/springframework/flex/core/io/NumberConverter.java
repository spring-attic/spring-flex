/*
 * Copyright 2002-2011 the original author or authors.
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

package org.springframework.flex.core.io;

import java.util.HashSet;
import java.util.Set;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.util.NumberUtils;

/**
 * 
 * TODO Document NumberConverter
 * <p />
 *
 * @author Jeremy Grelle
 */
public class NumberConverter implements GenericConverter {

    @SuppressWarnings("unchecked")
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        Number numberValue = (Number) source;
        if (numberValue == null) {
            return Double.NaN;
        } else if (numberValue.equals(Double.NaN)) {
            return null;
        } else if (!sourceType.getType().equals(targetType.getType())){
            return NumberUtils.convertNumberToTargetClass(numberValue, (Class<? extends Number>)targetType.getObjectType());
        }
        return source;
    }

    public Set<ConvertiblePair> getConvertibleTypes() {
        Set<ConvertiblePair> convertibleTypes = new HashSet<ConvertiblePair>();
        convertibleTypes.add(new ConvertiblePair(Number.class, Number.class));
        return convertibleTypes;
    }
}