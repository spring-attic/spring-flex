package org.springframework.flex.convert;
/*
 * Copyright 2002-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.springframework.core.convert.TypeDescriptor;

import java.lang.reflect.Field;

/**
 * Context about a type to convert to.
 *
 * @author Jose Barragan
 * @since 1.5.3
 */
public class FieldTypeDescriptor extends TypeDescriptor {

	private Field field;

	/**
	 * Create a new type descriptor from a {@link java.lang.reflect.Field}.
	 * Use this constructor when source or target conversion point is a field.
	 *
	 * @param field the field
	 */
	public FieldTypeDescriptor(Field field) {
		super(field);
		this.field = field;
	}

	/**
	 * Return the wrapped Field, if any.
	 * <p>Note: Either MethodParameter or Field is available.
	 * @return the Field, or <code>null</code> if none
	 */
	public Field getField() {
		return field;
	}
}
