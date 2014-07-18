package org.springframework.core.convert;

import java.lang.reflect.Field;

/**
 * Created by IntelliJ IDEA.
 *
 * @author pepebarragan
 *         Date: 6/8/13
 *         Time: 11:45 PM
 */
public class FieldTypeDescriptor extends TypeDescriptor {

	private Field field = null;

	/**
	 * Create a new type descriptor from a {@link java.lang.reflect.Field}.
	 * <p>Use this constructor when a source or target conversion point is a field.
	 *
	 * @param field the field
	 */
	public FieldTypeDescriptor(Field field) {
		super(field);
		this.field = field;
	}



	/**
	 * Getter for field was wrapped
	 *
	 * @return
	 */
	public Field getField() {
		return field;
	}
}
