package org.springframework.flex.messaging.remoting;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Method-level annotation to mark a method as included when exposing the object for AMF-based remoting 
 * using the @{@link RemotingDestination} annotation. 
 * 
 * @author Jeremy Grelle
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface RemotingInclude {

}
