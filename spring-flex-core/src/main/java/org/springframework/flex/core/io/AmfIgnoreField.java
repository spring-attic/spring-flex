/*
 * Copyright 2002-2011 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   https://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.flex.core.io;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates a field to be skipped during the AMF conversion process.  This annotation is 
 * only considered when {@link AbstractAmfConversionServiceConfigProcessor#setUseDirectFieldAccess(boolean) useDirectFieldAccess} 
 * is set to {@code true}.
 * 
 * <p>
 * By default, the field will be ignored during both serialization and deserialization.  To change this behavior, set the 
 * {@link #onSerialization() onSerialization} or {@link #onDeserialization() onDeserialization} attribute accordingly.
 *
 * @author Jeremy Grelle
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AmfIgnoreField {

    /**
     * When {@code true}, the field will be ignored during serialization to AMF.  Defaults to {@code true}.
     */
    boolean onSerialization() default true;
    
    /**
     * When {@code true}, the field will be ignored during deserialization from AMF.  Defaults to {@code true}.
     */
    boolean onDeserialization() default true;
}
