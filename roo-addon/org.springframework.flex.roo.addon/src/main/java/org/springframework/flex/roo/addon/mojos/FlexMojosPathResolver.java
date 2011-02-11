/*
 * Copyright 2002-2010 the original author or authors.
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

package org.springframework.flex.roo.addon.mojos;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.file.monitor.MonitoringRequest;
import org.springframework.roo.project.AbstractPathResolver;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathInformation;

/**
 * {@link PathResolver} implementation that resolves paths using the conventions of the Flex Mojos Maven plugin.
 *
 * @author Jeremy Grelle
 */
@Component(immediate = true)
@Service(FlexPathResolver.class)
public class FlexMojosPathResolver extends AbstractPathResolver implements FlexPathResolver {

    private final List<PathInformation> pathInformation = new ArrayList<PathInformation>();

    protected void activate(ComponentContext context) {
        String workingDir = context.getBundleContext().getProperty("roo.working.directory");
        File root = MonitoringRequest.getInitialMonitoringRequest(workingDir).getFile();
        this.pathInformation.add(new PathInformation(FlexPath.SRC_MAIN_FLEX, true, new File(root, "src/main/flex")));
        this.pathInformation.add(new PathInformation(FlexPath.LIBS, false, new File(root, "libs")));
        init();
    }

    public List<Path> getFlexSourcePaths() {
        List<Path> flexSourcePaths = new ArrayList<Path>();
        for (Path path : getPaths()) {
            if (path instanceof FlexPath) {
                flexSourcePaths.add(path);
            }
        }
        return flexSourcePaths;
    }

    @Override
    protected List<PathInformation> getPathInformation() {
        return this.pathInformation;
    }

}
