package org.springframework.flex.core.io;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AmfIgnoreField {

    boolean onSerialization() default true;
    
    boolean onDeserialization() default true;
}
