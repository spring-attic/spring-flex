
package org.springframework.flex.core.io;

import java.util.HashSet;
import java.util.Set;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.util.NumberUtils;

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