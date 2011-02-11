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

package org.springframework.flex.roo.addon.as.classpath.as3parser;

import java.io.FileReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.flex.roo.addon.as.classpath.ASPhysicalTypeDetails;
import org.springframework.flex.roo.addon.as.classpath.ASPhysicalTypeIdentifier;
import org.springframework.flex.roo.addon.as.classpath.ASPhysicalTypeMetadata;
import org.springframework.flex.roo.addon.as.classpath.ASPhysicalTypeMetadataProvider;
import org.springframework.roo.metadata.AbstractMetadataItem;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.process.manager.ActiveProcessManager;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.ProcessManager;
import org.springframework.roo.support.logging.HandlerUtils;
import org.springframework.roo.support.style.ToStringCreator;
import org.springframework.roo.support.util.Assert;

import uk.co.badgersinfoil.metaas.ActionScriptFactory;
import uk.co.badgersinfoil.metaas.dom.ASCompilationUnit;

/**
 * Parser-specific metadata for an ActionScript source file.
 *
 * @author Jeremy Grelle
 */
public class As3ParserClassMetadata extends AbstractMetadataItem implements ASPhysicalTypeMetadata {

    private static final Logger logger = HandlerUtils.getLogger(As3ParserClassMetadata.class);

    private static final ActionScriptFactory asFactory = new ActionScriptFactory();

    private final String fileIdentifier;

    private ASPhysicalTypeDetails physicalTypeDetails;

    public As3ParserClassMetadata(FileManager fileManager, String fileIdentifier, String metadataIdentificationString,
        MetadataService metadataService, ASPhysicalTypeMetadataProvider asPhysicalTypeMetadataProvider) {
        super(metadataIdentificationString);
        Assert.isTrue(ASPhysicalTypeIdentifier.isValid(metadataIdentificationString), "Metadata identification string '"
            + metadataIdentificationString + "' does not appear to be a valid actionscript type identifier");
        Assert.notNull(fileManager, "File manager required");
        Assert.notNull(fileIdentifier, "File identifier required");
        Assert.notNull(metadataService, "Metadata service required");
        Assert.notNull(asPhysicalTypeMetadataProvider, "Physical type metadata provider required");

        this.fileIdentifier = fileIdentifier;

        try {
            Assert.isTrue(fileManager.exists(fileIdentifier), "Path '" + fileIdentifier + "' must exist");
            FileReader asFileReader = new FileReader(fileManager.readFile(fileIdentifier).getFile());
            ASCompilationUnit compilationUnit = asFactory.newParser().parse(asFileReader);

            this.physicalTypeDetails = new As3ParserMutableClassOrInterfaceTypeDetails(compilationUnit, fileManager, metadataIdentificationString,
                fileIdentifier, ASPhysicalTypeIdentifier.getActionScriptType(metadataIdentificationString), metadataService,
                asPhysicalTypeMetadataProvider);

            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Parsed '" + metadataIdentificationString + "'");
            }
        } catch (Throwable ex) {
            // non-fatal, it just means the type could not be parsed

            // TODO - Is there any type of ActionScript file that this parser does not support?

            /*
             * if (ex.getMessage() != null &&
             * ex.getMessage().startsWith(JavaParserMutableClassOrInterfaceTypeDetails.UNSUPPORTED_MESSAGE_PREFIX)) { //
             * we don't want the limitation of the metadata parsing subsystem to confuse the user into thinking there is
             * a problem with their source code valid = false; return; }
             */

            ProcessManager pm = ActiveProcessManager.getActiveProcessManager();
            if (pm != null && pm.isDevelopmentMode()) {
                ex.printStackTrace();
            }
            if (logger.isLoggable(Level.FINEST)) {
                logger.log(Level.FINEST, "Unable to parse '" + metadataIdentificationString + "'", ex);
            }
            this.valid = false;
        }
    }

    public ASPhysicalTypeDetails getPhysicalTypeDetails() {
        return this.physicalTypeDetails;
    }

    public String getPhysicalLocationCanonicalPath() {
        return this.fileIdentifier;
    }

    @Override
    public String toString() {
        ToStringCreator tsc = new ToStringCreator(this);
        tsc.append("identifier", getId());
        tsc.append("valid", this.valid);
        tsc.append("fileIdentifier", this.fileIdentifier);
        tsc.append("physicalTypeDetails", this.physicalTypeDetails);
        return tsc.toString();
    }
}
