package org.example;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.cloudformation.model.*;

import software.amazon.awssdk.services.cloudformation.model.Tag;
import software.amazon.awssdk.services.cloudformation.model.Parameter;

import java.net.URI;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.Files;


public class App {

    private static final Region region = Region.US_EAST_1;
    private static final String BOOTSTRAP_STACK_NAME = "s3-static-website-bootstrap-stack";

    private static CloudFormationClient cloudFormationClient = CloudFormationClient.builder().region(region).build();

    
    public void createBootstrap(String environmentString) throws Exception {
        screenMessage("BOOSTRAP STACK CREATION START");

        // TODO file inside the jar
        Path templatePath = Paths.get("./src/main/resources/bootstrap/bootstrap.json");

        log("reading template from: "+templatePath.toAbsolutePath());

        String templateBody = new String(Files.readAllBytes(templatePath));

        Parameter environment = Parameter
            .builder()
            .parameterKey("Environment")
            .parameterValue(environmentString)
            .build();

        CreateStackRequest request = CreateStackRequest.builder()
            .stackName(BOOTSTRAP_STACK_NAME+"-"+environmentString)
            .templateBody(templateBody)
            .parameters(environment)
            .tags(Tag.builder().key("s3_static_website_environment").value(environmentString).build())
            .build();

        CreateStackResponse response = cloudFormationClient.createStack(request);

        screenMessage("BOOSTRAP STACK CREATION END");
    }

    public void listStacks() {

        screenMessage("LIST STACK START");

        Region region = Region.US_EAST_1;

        CloudFormationClient cloudFormationClient = CloudFormationClient.builder()
                .region(region)
                .build();

        ListStacksResponse listStacksResponse = cloudFormationClient.listStacks();

        for (StackSummary stackSummary : listStacksResponse.stackSummaries()) {
            if (stackSummary.stackName().contains("dev") && !(stackSummary.stackStatus().toString().equals("DELETE_COMPLETE"))) {
                System.out.println(stackSummary.stackName()+" ("+stackSummary.stackStatus()+")" );
            }
        }

        screenMessage("LIST STACK END");
    }

    // TODO use log framework here
    private static void screenMessage(String message) {
        System.out.println();
        System.out.println(String.format(" -- %s --",message));
        System.out.println();
    }

    // TODO use log framework here
    private static void log(String message) {
        System.out.println(message);
    }

    public static void main(String[] args) {
        try {
            App app = new App();
            if (args.length!=2) {
                System.err.println("Command is required");
            }
            String command = args[0];
            String environment = args[1];

            switch (command) {
                case "LIST": {
                    app.listStacks();
                    break;
                }
                case "CREATE_BOOTSTRAP": {
                    app.createBootstrap(environment);
                    break;
                }

            }
        } catch (Exception e) {
            screenMessage("*** ERROR *** "+e.getMessage());
        }
    }
}
