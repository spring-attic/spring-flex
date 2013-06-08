package org.springframework.core.convert;

/**
 * Created by IntelliJ IDEA.
 *
 * @author pepebarragan
 *         Date: 6/9/13
 *         Time: 12:16 AM
 */
public class PropertyTypeDescriptor extends TypeDescriptor {

	/**
	 * Create a new type descriptor from a {@link org.springframework.core.convert.Property}.
	 * <p>Use this constructor when a source or target conversion point is a
	 * property on a Java class.
	 *
	 * @param property the property
	 */
	public PropertyTypeDescriptor(Property property) {
		super(property);
	}
}
