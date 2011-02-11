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

import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.ProjectMetadata;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;
import org.springframework.roo.support.logging.HandlerUtils;
import org.springframework.roo.support.util.Assert;

/**
 * Roo CLI commands for working with Flex projects.
 * 
 * @author Jeremy Grelle
 */
@Component
@Service
public class FlexCommands implements CommandMarker {

    private static Logger logger = HandlerUtils.getLogger(FlexCommands.class);

    @Reference
    private FlexOperations operations;

    @Reference
    private MetadataService metadataService;

    @CliAvailabilityIndicator( { "flex setup", "flex remoting scaffold", "flex remoting all" })
    public boolean isFlexAvailable() {
        return this.operations.isFlexAvailable();
    }

    @CliCommand(value = "flex setup", help = "Install Spring BlazeDS configuration artifacts into your project")
    public void installFlex() {
        this.operations.installFlex();
    }

    @CliCommand(value = "flex remoting scaffold", help = "Create a new scaffold Service (ie with full CRUD operations) exposed as a Flex Remoting Destination")
    public void newRemotingDestination(
        @CliOption(key = { "name", "" }, mandatory = true, help = "The path and name of the service object to be created") JavaType service,
        @CliOption(key = "entity", mandatory = false, optionContext = "update,project", unspecifiedDefaultValue = "*", help = "The name of the entity object which the service exposes to the flex client") JavaType entity) {
        this.operations.createRemotingDestination(service, entity);
    }

    @CliCommand(value = "flex remoting all", help = "Scaffold a Service for all entities without an existing Remoting Destination")
    public void generateAll(
        @CliOption(key = "package", mandatory = true, help = "The package in which new services will be placed") JavaPackage javaPackage) {
        ProjectMetadata projectMetadata = (ProjectMetadata) this.metadataService.get(ProjectMetadata.getProjectIdentifier());
        Assert.notNull(projectMetadata, "Could not obtain ProjectMetadata");
        if (!javaPackage.getFullyQualifiedPackageName().startsWith(projectMetadata.getTopLevelPackage().getFullyQualifiedPackageName())) {
            logger.warning("Your service was created outside of the project's top level package and is therefore not included in the preconfigured component scanning. Please adjust your component scanning manually in applicationContext.xml");
        }
        this.operations.generateAll(javaPackage);
    }

}