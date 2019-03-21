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
        this.excludeMethods = extractExcludeMethods(beanType);
        this.includeMethods = extractIncludeMethods(beanType);
    }

    public String getBeanName() {
        return this.beanName;
    }

    public String[] getExcludeMethods() {
        return this.excludeMethods;
    }

    public String[] getIncludeMethods() {
        return this.includeMethods;
    }

    public RemotingDestination getRemotingDestination() {
        return this.remotingDestination;
    }

    private String[] extractExcludeMethods(Class<?> serviceClass) {
        final Set<String> excludes = new HashSet<String>();
        ReflectionUtils.doWithMethods(serviceClass, new MethodCallback() {

            public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                excludes.add(method.getName());
            }
        }, new FlexExcludeFilter());
        return excludes.toArray(new String[excludes.size()]);
    }

    private String[] extractIncludeMethods(Class<?> serviceClass) {
        final Set<String> includes = new HashSet<String>();
        ReflectionUtils.doWithMethods(serviceClass, new MethodCallback() {

            public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
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
