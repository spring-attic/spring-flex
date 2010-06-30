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

package org.springframework.flex.roo.addon;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.osgi.service.component.ComponentContext;
import org.springframework.flex.roo.addon.mojos.FlexPathResolver;
import org.springframework.roo.file.monitor.DirectoryMonitoringRequest;
import org.springframework.roo.file.monitor.MonitoringRequest;
import org.springframework.roo.file.monitor.NotifiableFileMonitorService;
import org.springframework.roo.file.monitor.event.FileDetails;
import org.springframework.roo.file.monitor.event.FileOperation;
import org.springframework.roo.file.undo.CreateDirectory;
import org.springframework.roo.file.undo.FilenameResolver;
import org.springframework.roo.file.undo.UndoManager;
import org.springframework.roo.metadata.MetadataDependencyRegistry;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.metadata.MetadataNotificationListener;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.ProjectMetadata;
import org.springframework.roo.support.util.Assert;

/**
 * {@link MetadataNotificationListener} that monitors the filesystem for changes to Flex source files.
 *
 * @author Jeremy Grelle
 */
@Component
public class FlexProjectListener implements MetadataNotificationListener {

    // TODO - Is there a better way to achieve the monitoring of the necessary Flex directories?

    @Reference
    private MetadataService metadataService;

    @Reference
    private MetadataDependencyRegistry metadataDependencyRegistry;

    @Reference
    private UndoManager undoManager;

    @Reference
    private NotifiableFileMonitorService fileMonitorService;

    @Reference
    private FlexPathResolver pathResolver;

    private boolean pathsRegistered = false;

    protected void activate(ComponentContext context) {
        this.metadataDependencyRegistry.addNotificationListener(this);
    }

    protected void deactivate(ComponentContext context) {
        this.metadataDependencyRegistry.removeNotificationListener(this);
    }

    public void notify(String upstreamDependency, String downstreamDependency) {
        if (this.pathsRegistered) {
            return;
        }

        Assert.isTrue(MetadataIdentificationUtils.isValid(upstreamDependency), "Upstream dependency is an invalid metadata identification string ('"
            + upstreamDependency + "')");

        if (upstreamDependency.equals(ProjectMetadata.getProjectIdentifier())) {
            // Acquire the Project Metadata, if available
            ProjectMetadata md = (ProjectMetadata) this.metadataService.get(upstreamDependency);
            if (md == null) {
                return;
            }

            FilenameResolver filenameResolver = new PathResolvingAwareFilenameResolver();

            Set<FileOperation> notifyOn = new HashSet<FileOperation>();
            notifyOn.add(FileOperation.MONITORING_START);
            notifyOn.add(FileOperation.MONITORING_FINISH);
            notifyOn.add(FileOperation.CREATED);
            notifyOn.add(FileOperation.RENAMED);
            notifyOn.add(FileOperation.UPDATED);
            notifyOn.add(FileOperation.DELETED);

            for (Path p : this.pathResolver.getFlexSourcePaths()) {
                // Verify path exists and ensure it's monitored, except root (which we assume is already monitored via
                // ProcessManager)
                if (!Path.ROOT.equals(p)) {
                    String fileIdentifier = this.pathResolver.getRoot(p);
                    File file = new File(fileIdentifier);
                    Assert.isTrue(!file.exists() || file.exists() && file.isDirectory(), "Path '" + fileIdentifier
                        + "' must either not exist or be a directory");
                    if (!file.exists()) {
                        // Create directory, but no notifications as that will happen once we start monitoring it below
                        new CreateDirectory(this.undoManager, filenameResolver, file);
                    }
                    MonitoringRequest request = new DirectoryMonitoringRequest(file, true, notifyOn);
                    if (md.isValid()) {
                        this.fileMonitorService.add(request);
                    } else {
                        this.fileMonitorService.remove(request);
                    }
                }
            }

            // Explicitly perform a scan now that we've added all the directories we wish to monitor
            this.fileMonitorService.scanAll();

            // Avoid doing this operation again unless the validity changes
            this.pathsRegistered = md.isValid();
        }
    }

    private final class PathResolvingAwareFilenameResolver implements FilenameResolver {

        public String getMeaningfulName(File file) {
            Assert.notNull(file, "File required");
            return FlexProjectListener.this.pathResolver.getFriendlyName(FileDetails.getCanonicalPath(file));
        }
    }

}
