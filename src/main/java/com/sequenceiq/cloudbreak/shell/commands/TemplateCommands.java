/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sequenceiq.cloudbreak.shell.commands;

import static com.sequenceiq.cloudbreak.shell.support.TableRenderer.renderSingleMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.client.CloudbreakClient;
import com.sequenceiq.cloudbreak.shell.model.CloudbreakContext;
import com.sequenceiq.cloudbreak.shell.model.Hints;
import com.sequenceiq.cloudbreak.shell.model.InstanceType;
import com.sequenceiq.cloudbreak.shell.model.Region;

@Component
public class TemplateCommands implements CommandMarker {

    @Autowired
    private CloudbreakContext context;
    @Autowired
    private CloudbreakClient cloudbreak;

    @CliAvailabilityIndicator(value = "template list")
    public boolean isTemplateListCommandAvailable() {
        return true;
    }

    @CliAvailabilityIndicator(value = "template defaults")
    public boolean isTemplateDefaultCommandAvailable() {
        return true;
    }

    @CliAvailabilityIndicator(value = "template show")
    public boolean isTemplateShowCommandAvailable() {
        return true;
    }

    @CliAvailabilityIndicator(value = "template create")
    public boolean isTemplateCreateCommandAvailable() {
        return true;
    }

    @CliAvailabilityIndicator(value = "template select")
    public boolean isTemplateSelectCommandAvailable() {
        return true;
    }

    @CliCommand(value = "template list", help = "Shows the currently available cloud templates")
    public String listTemplates() {
        return renderSingleMap(cloudbreak.getTemplatesMap(), "ID", "INFO");
    }

    @CliCommand(value = "template defaults", help = "Adds the default templates to Cloudbreak")
    public String addDefaultTemplates() {
        String message = "Default templates added";
        try {
            cloudbreak.addDefaultTemplates();
        } catch (Exception e) {
            message = "Failed to add the default templates: " + e.getMessage();
        }
        return message;
    }

    @CliCommand(value = "template select", help = "Select the template by its id")
    public String selectTemplate(
            @CliOption(key = "id", mandatory = true, help = "Id of the template") String id) {
        if (cloudbreak.getTemplate(id) != null) {
            context.addTemplate(id);
            context.setHint(Hints.CREATE_CLUSTER);
            return "Template selected, id: " + id;
        } else {
            return "No template specified";
        }
    }

    @CliCommand(value = "template createEC2", help = "Create a new EC2 template")
    public String createEc2Template(
            @CliOption(key = "description", mandatory = true, help = "Description of the template") String description,
            @CliOption(key = "name", mandatory = true, help = "Name of the template") String name,
            @CliOption(key = "region", mandatory = false, specifiedDefaultValue = "EU_WEST_1", help = "region of the template") Region region,
            @CliOption(key = "amiId", mandatory = false, specifiedDefaultValue = "ami-f39f5684", help = "amiId of the template: ami-f39f5684 (ambari-warmup) or ami-f39f5684 (with-nagios)") String amiId,
            @CliOption(key = "keyName", mandatory = true, help = "keyName of the template") String keyName,
            @CliOption(key = "sshLocation", mandatory = false, specifiedDefaultValue = "0.0.0.0/0", help = "sshLocation of the template") String sshLocation,
            @CliOption(key = "instanceType", mandatory = true, help = "instanceType of the template") InstanceType instanceType
    ) {
        String id = cloudbreak.postEc2Template(name, description, region.name(), amiId, keyName, sshLocation, instanceType.name());
        context.addTemplate(id);
        context.setHint(Hints.CREATE_CLUSTER);
        return "Template created, id: " + id;
    }

    @CliCommand(value = "template show", help = "Shows the template by its id")
    public Object showTemlate(
            @CliOption(key = "id", mandatory = true, help = "Id of the template") String id) {
        return renderSingleMap(cloudbreak.getTemplateMap(id), "FIELD", "VALUE");
    }


    @CliCommand(value = "template create", help = "Create a new cloud template")
    public String createTemplate(@CliOption(key = "name", mandatory = true, help = "Name of the cloud template") String name) {
        String id = cloudbreak.postTemplate(name);
        context.addTemplate(id);
        context.setHint(Hints.CREATE_STACK);
        return "Template created, id: " + id;
    }
}