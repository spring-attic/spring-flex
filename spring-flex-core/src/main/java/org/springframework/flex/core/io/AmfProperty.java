package org.springframework.flex.core.io;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface AmfProperty {
    
    /**
     * The name of the corresponding property in the AMF payload.  
     */
    String value();
    
}
