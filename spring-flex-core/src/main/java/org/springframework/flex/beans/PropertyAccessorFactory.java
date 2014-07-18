package org.springframework.flex.beans;


import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperAccessor;
import org.springframework.beans.ConfigurablePropertyAccessor;

/**
 * Simple factory facade for obtaining {@link org.springframework.beans.PropertyAccessor} instances,
 * in particular for {@link org.springframework.beans.BeanWrapper} instances. Conceals the actual
 * target implementation classes and their extended public signature.
 *
 * @author Juergen Hoeller
 * @author Jose Barragan
 * @since 2.5.2
 */
public abstract class PropertyAccessorFactory {

	/**
	 * Obtain a BeanWrapper for the given target object,
	 * accessing properties in JavaBeans style.
	 * @param target the target object to wrap
	 * @return the property accessor
	 * @see org.springframework.beans.BeanWrapperAccessor
	 */
	public static BeanWrapper forBeanPropertyAccess(Object target) {
		return new BeanWrapperAccessor(target);
	}

	/**
	 * Obtain a PropertyAccessor for the given target object,
	 * accessing properties in direct field style.
	 * @param target the target object to wrap
	 * @return the property accessor
	 * @see org.springframework.beans.DirectFieldAccessor
	 */
	public static ConfigurablePropertyAccessor forDirectFieldAccess(Object target) {
		return new DirectFieldAccessor(target);
	}
}
