package org.springframework.flex.core.io;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Version;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.core.convert.support.PropertyTypeDescriptor;
import org.springframework.util.NumberUtils;


public class JpaNumericVersionConverter implements ConditionalGenericConverter {

    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (targetType instanceof PropertyTypeDescriptor) {
            PropertyTypeDescriptor propType = (PropertyTypeDescriptor) targetType;
            if (propType.getAnnotation(Version.class) != null) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        Number numberValue = (Number) source;
        if (numberValue.intValue() < 0 && !targetType.isPrimitive()) {
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
