package org.springframework.flex.config;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.springframework.flex.remoting.RemotingDestination;
import org.springframework.flex.remoting.RemotingExclude;
import org.springframework.flex.remoting.RemotingInclude;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;

class RemotingDestinationMetadata {

	private final RemotingDestination remotingDestination;
	private final String beanName;
	private final String[] excludeMethods;
	private final String[] includeMethods;
	
	RemotingDestinationMetadata(RemotingDestination remotingDestination, String beanName, Class<?> beanType) {
		this.remotingDestination = remotingDestination;
		this.beanName = beanName;
		excludeMethods = extractExcludeMethods(beanType);
		includeMethods = extractIncludeMethods(beanType);
	}

	public String getBeanName() {
		return beanName;
	}

	public String[] getExcludeMethods() {
		return excludeMethods;
	}

	public String[] getIncludeMethods() {
		return includeMethods;
	}
	
	public RemotingDestination getRemotingDestination() {
		return remotingDestination;
	}
	
	private String[] extractExcludeMethods(Class<?> serviceClass) {
		final Set<String> excludes = new HashSet<String>();
		ReflectionUtils.doWithMethods(serviceClass, new MethodCallback() {
			public void doWith(Method method) throws IllegalArgumentException,
					IllegalAccessException {
				excludes.add(method.getName());
			}
		}, new FlexExcludeFilter());
		return excludes.toArray(new String[excludes.size()]);
	}

	private String[] extractIncludeMethods(Class<?> serviceClass) {
		final Set<String> includes = new HashSet<String>();
		ReflectionUtils.doWithMethods(serviceClass, new MethodCallback() {
			public void doWith(Method method) throws IllegalArgumentException,
					IllegalAccessException {
				includes.add(method.getName());
			}
		}, new FlexIncludeFilter());
		return includes.toArray(new String[includes.size()]);
	}
	
	private static class FlexExcludeFilter implements ReflectionUtils.MethodFilter {
		public boolean matches(Method method) {
			return method.getAnnotation(RemotingExclude.class) != null;
		}
	}
	
	private static class FlexIncludeFilter implements ReflectionUtils.MethodFilter {
		public boolean matches(Method method) {
			return method.getAnnotation(RemotingInclude.class) != null;
		}
	}
}
