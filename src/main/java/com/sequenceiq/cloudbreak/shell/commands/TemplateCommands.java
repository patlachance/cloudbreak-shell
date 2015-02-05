package com.sequenceiq.cloudbreak.shell.commands;

import static com.sequenceiq.cloudbreak.shell.support.TableRenderer.renderSingleMap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.client.CloudbreakClient;
import com.sequenceiq.cloudbreak.shell.model.AwsInstanceType;
import com.sequenceiq.cloudbreak.shell.model.AzureInstanceType;
import com.sequenceiq.cloudbreak.shell.model.CloudbreakContext;
import com.sequenceiq.cloudbreak.shell.model.GcpInstanceType;
import com.sequenceiq.cloudbreak.shell.model.Hints;
import com.sequenceiq.cloudbreak.shell.model.VolumeType;

import groovyx.net.http.HttpResponseException;

@Component
public class TemplateCommands implements CommandMarker {

    public static final int BUFFER = 1024;
    public static final int VOLUME_COUNT_MIN = 1;
    public static final int VOLUME_COUNT_MAX = 8;
    public static final int VOLUME_SIZE_MIN = 1;
    public static final int VOLUME_SIZE_MAX = 1024;
    @Autowired
    private CloudbreakContext context;
    @Autowired
    private CloudbreakClient cloudbreak;

    @CliAvailabilityIndicator(value = "template list")
    public boolean isTemplateListCommandAvailable() {
        return true;
    }

    @CliAvailabilityIndicator(value = "template show")
    public boolean isTemplateShowCommandAvailable() {
        return true;
    }

    @CliAvailabilityIndicator(value = "template delete")
    public boolean isTemplateDeleteCommandAvailable() {
        return true;
    }

    @CliAvailabilityIndicator({ "template create --GCP", "template create --EC2", "template create --AZURE" })
    public boolean isTemplateEc2CreateCommandAvailable() {
        return true;
    }

    @CliCommand(value = "template list", help = "Shows the currently available cloud templates")
    public String listTemplates() {
        try {
            Map<String, String> templatesMap = cloudbreak.getAccountTemplatesMap();
            return renderSingleMap(templatesMap, "ID", "INFO");
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @CliCommand(value = "template create --EC2", help = "Create a new EC2 template")
    public String createEc2Template(
            @CliOption(key = "name", mandatory = true, help = "Name of the template") String name,
            @CliOption(key = "instanceType", mandatory = true, help = "instanceType of the template") AwsInstanceType instanceType,
            @CliOption(key = "volumeCount", mandatory = true, help = "volumeCount of the template") Integer volumeCount,
            @CliOption(key = "volumeSize", mandatory = true, help = "volumeSize(GB) of the template") Integer volumeSize,
            @CliOption(key = "volumeType", mandatory = false, help = "volumeType of the template", specifiedDefaultValue = "Gp2") VolumeType volumeType,
            @CliOption(key = "spotPrice", mandatory = false, help = "spotPrice of the template") Double spotPrice,
            @CliOption(key = "sshLocation", mandatory = false, specifiedDefaultValue = "0.0.0.0/0", help = "sshLocation of the template") String sshLocation,
            @CliOption(key = "publicInAccount", mandatory = false, help = "flags if the template is public in the account") Boolean publicInAccount,
            @CliOption(key = "description", mandatory = false, help = "Description of the template") String description
            ) {
        try {
            if (volumeCount < VOLUME_COUNT_MIN || volumeCount > VOLUME_COUNT_MAX) {
                return "volumeCount has to be between 1 and 8.";
            }
            if (volumeSize < VOLUME_SIZE_MIN || volumeSize > VOLUME_SIZE_MAX) {
                return "VolumeSize has to be between 1 and 1024.";
            }
            String id;
            if (spotPrice == null) {
                id = cloudbreak.postEc2Template(name,
                        getDescription(description, "Aws"),
                        sshLocation == null ? "0.0.0.0/0" : sshLocation,
                        instanceType.toString(),
                        volumeCount.toString(),
                        volumeSize.toString(),
                        volumeType == null ? VolumeType.Gp2.name() : volumeType.name(),
                        publicInAccount(publicInAccount)
                );
            } else {
                id = cloudbreak.postSpotEc2Template(name,
                        getDescription(description, "Aws"),
                        sshLocation == null ? "0.0.0.0/0" : sshLocation,
                        instanceType.toString(),
                        volumeCount.toString(),
                        volumeSize.toString(),
                        volumeType == null ? VolumeType.Gp2.name() : volumeType.name(),
                        spotPrice.toString(),
                        publicInAccount(publicInAccount)
                );
            }
            createOrSelectBlueprintHint();
            return "Template created, id: " + id;
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    private String getDescription(String description, String cloudPlatform) {
        return description == null ? cloudPlatform + " template was created by the cloudbreak-shell" : description;
    }


    @CliCommand(value = "template create --AZURE", help = "Create a new AZURE template")
    public String createAzureTemplate(
            @CliOption(key = "name", mandatory = true, help = "Name of the template") String name,
            @CliOption(key = "instanceType", mandatory = true, help = "type of the VM") AzureInstanceType vmType,
            @CliOption(key = "volumeCount", mandatory = true, help = "volumeCount of the template") Integer volumeCount,
            @CliOption(key = "volumeSize", mandatory = true, help = "volumeSize(GB) of the template") Integer volumeSize,
            @CliOption(key = "publicInAccount", mandatory = false, help = "flags if the template is public in the account") Boolean publicInAccount,
            @CliOption(key = "description", mandatory = false, help = "Description of the template") String description
            ) {
        if (volumeCount < VOLUME_COUNT_MIN || volumeCount > VOLUME_COUNT_MAX) {
            return "volumeCount has to be between 1 and 8.";
        }
        if (volumeSize < VOLUME_SIZE_MIN || volumeSize > VOLUME_SIZE_MAX) {
            return "VolumeSize has to be between 1 and 1024.";
        }
        try {
            String id = cloudbreak.postAzureTemplate(name,
                    getDescription(description, "Azure"),
                    vmType.name(),
                    volumeCount.toString(),
                    volumeSize.toString(),
                    publicInAccount(publicInAccount)
            );
            createOrSelectBlueprintHint();
            return "Template created, id: " + id;
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    @CliCommand(value = "template create --GCP", help = "Create a new GCP template")
    public String createGcpTemplate(
            @CliOption(key = "name", mandatory = true, help = "Name of the template") String name,
            @CliOption(key = "instanceType", mandatory = true, help = "type of the VM") GcpInstanceType instanceType,
            @CliOption(key = "volumeCount", mandatory = true, help = "volumeCount of the template") Integer volumeCount,
            @CliOption(key = "volumeSize", mandatory = true, help = "volumeSize(GB) of the template") Integer volumeSize,
            @CliOption(key = "publicInAccount", mandatory = false, help = "flags if the template is public in the account") Boolean publicInAccount,
            @CliOption(key = "description", mandatory = false, help = "Description of the template") String description
            ) {
        if (volumeCount < VOLUME_COUNT_MIN || volumeCount > VOLUME_COUNT_MAX) {
            return "volumeCount has to be between 1 and 8.";
        }
        if (volumeSize < VOLUME_SIZE_MIN || volumeSize > VOLUME_SIZE_MAX) {
            return "VolumeSize has to be between 1 and 1024.";
        }
        try {
            String id = cloudbreak.postGccTemplate(name,
                    getDescription(description, "Gcp"),
                    instanceType.name(),
                    volumeCount.toString(),
                    volumeSize.toString(),
                    publicInAccount(publicInAccount)
            );
            createOrSelectBlueprintHint();
            return "Template created, id: " + id;
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    private boolean publicInAccount(Boolean publicInAccount) {
        return publicInAccount == null ? false : publicInAccount;
    }


    @CliCommand(value = "template show", help = "Shows the template by its id")
    public Object showTemlate(
            @CliOption(key = "id", mandatory = true, help = "Id of the template") String id) {
        try {
            return renderSingleMap(cloudbreak.getTemplateMap(id), "FIELD", "VALUE");
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.getMessage();
        }
    }

    @CliCommand(value = "template delete", help = "Shows the template by its id")
    public Object deleteTemlate(
            @CliOption(key = "id", mandatory = true, help = "Id of the template") String id) {
        try {
            return cloudbreak.deleteTemplate(id);
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    private String readFileAsString(String filePath) throws IOException {
        StringBuffer fileData = new StringBuffer();
        BufferedReader reader = new BufferedReader(
                new FileReader(filePath));
        char[] buf = new char[BUFFER];
        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) {
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        reader.close();
        return fileData.toString();
    }

    private void createOrSelectBlueprintHint() throws Exception {
        if (context.isCredentialAccessible() && context.isBlueprintAccessible()) {
            context.setHint(Hints.CONFIGURE_INSTANCEGROUP);
        } else if (!context.isBlueprintAccessible()) {
            context.setHint(Hints.SELECT_BLUEPRINT);
        } else if (!context.isCredentialAccessible()) {
            context.setHint(Hints.SELECT_CREDENTIAL);
        } else if (context.isCredentialAvailable()
                && (context.getActiveHostgoups().size() == context.getInstanceGroups().size()
                && context.getActiveHostgoups().size() != 0)) {
            context.setHint(Hints.CREATE_STACK);
        } else if (context.isStackAccessible()) {
            context.setHint(Hints.CREATE_STACK);
        } else {
            context.setHint(Hints.NONE);
        }
    }
}
