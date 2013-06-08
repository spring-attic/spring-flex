package org.springframework.flex.beans;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.core.convert.FieldTypeDescriptor;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * Created by IntelliJ IDEA.
 *
 * @author pepebarragan
 *         Date: 6/8/13
 *         Time: 11:36 PM
 */
public class DirectFieldAccessor extends org.springframework.beans.DirectFieldAccessor {

	private final Object target;

	private final Map<String, Field> fieldMap = new HashMap<String, Field>();


	/**
	 * Create a new DirectFieldAccessor for the given target object.
	 * @param target the target object to access
	 */
	public DirectFieldAccessor(Object target) {
		super(target);
		Assert.notNull(target, "Target object must not be null");
		this.target = target;
		ReflectionUtils.doWithFields(this.target.getClass(), new ReflectionUtils.FieldCallback() {
			public void doWith(Field field) {
				if (fieldMap.containsKey(field.getName())) {
					// ignore superclass declarations of fields already found in a subclass
				} else {
					fieldMap.put(field.getName(), field);
				}
			}
		});
	}

	@Override
	public TypeDescriptor getPropertyTypeDescriptor(String propertyName) throws BeansException {
		Field field = this.fieldMap.get(propertyName);
		if (field != null) {
			return new FieldTypeDescriptor(field);
		}
		return null;
	}
}
