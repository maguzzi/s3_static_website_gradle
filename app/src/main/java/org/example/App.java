package org.example;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.cloudformation.model.*;

import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.nio.file.Files;


public class App {

    private static final Region region = Region.US_EAST_1;

    private static final String BOOTSTRAP_STACK_NAME = "s3-static-website-bootstrap-stack";

    private static final String S3_STATIC_WEBSITE_ENVIRONMENT_TAG="s3_static_website_environment";

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    private static CloudFormationClient cloudFormationClient = CloudFormationClient.builder().region(region).build();

    
    public void createBootstrap(String environmentString) throws Exception {
        screenMessage("BOOSTRAP STACK CREATION START");

        // TODO file inside the jar
        Path templatePath = Paths.get("./src/main/resources/bootstrap/bootstrap.json");

        logger.info("reading template from: {}",templatePath.toAbsolutePath());

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

    public void createDistribution(String environmentString) throws Exception {
        screenMessage("BOOSTRAP STACK CREATION START");

        // TODO file inside the jar
        Path templatePath = Paths.get("./src/main/resources/bootstrap/bootstrap.json");

        logger.debug("reading template from: {}",templatePath.toAbsolutePath());

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
            .tags(Tag.builder().key(S3_STATIC_WEBSITE_ENVIRONMENT_TAG).value(environmentString).build())
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

        ListStacksResponse listStacksResponse = cloudFormationClient.listStacks(
        ListStacksRequest.builder()
            .stackStatusFiltersWithStrings("CREATE_COMPLETE","UPDATE_COMPLETE")
            .build()
        );

        for (StackSummary stackSummary : listStacksResponse.stackSummaries()) {
            logger.trace("Checking stack {}",stackSummary.stackName());
            DescribeStacksRequest describeStacksRequest = DescribeStacksRequest.builder().stackName(stackSummary.stackName()).build();
            DescribeStacksResponse describeStacksResponse = cloudFormationClient.describeStacks(describeStacksRequest);    
            List<Stack> stacks = describeStacksResponse.stacks().stream().filter(i->i.tags().stream().anyMatch(j->j.key().equals(S3_STATIC_WEBSITE_ENVIRONMENT_TAG))).collect(Collectors.toList());
            stacks.forEach(it->logger.info("{} ({}) - {}", it.stackName(),it.stackStatusAsString(),it.tags()));
        }

        screenMessage("LIST STACK END");
    }

    private static void screenMessage(String message) {
        logger.info("");
        logger.info(" -- {} --",message);        
        logger.info("");
    }


    public static void main(String[] args) {
        try {
            App app = new App();
            if (args.length!=2) {
                logger.error("Command is required");
            }
            String command = args[0];
            String environment = args[1];

            switch (command) {
                case "LIST": {
                    app.listStacks();
                    break;
                }
                case "BOOTSTRAP": {
                    app.createBootstrap(environment);
                    break;
                }
                case "DISTRIBUTION": {
                    app.createDistribution(environment);
                    break;
                }

            }
        } catch (Exception e) {
            screenMessage("*** ERROR *** "+e.getMessage());
        }
    }
}
