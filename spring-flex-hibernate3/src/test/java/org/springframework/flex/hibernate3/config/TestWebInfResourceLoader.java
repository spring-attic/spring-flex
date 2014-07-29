/*
 * Copyright 2002-2009 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.flex.hibernate3.config;

import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

public class TestWebInfResourceLoader implements ResourceLoader {

    ApplicationContext context;

    String basePath;

    public TestWebInfResourceLoader(ApplicationContext context) {
        this(context, "classpath:org/springframework/flex/hibernate3/config/");
    }

    public TestWebInfResourceLoader(ApplicationContext context, String basePath) {
        this.context = context;
        this.basePath = basePath;
    }

    public ClassLoader getClassLoader() {
        return this.context.getClassLoader();
    }

    public Resource getResource(String location) {
        if (location.startsWith("/WEB-INF/flex/")) {
            location = location.replace("/WEB-INF/flex/", this.basePath);
        }
        return this.context.getResource(location);
    }

}
