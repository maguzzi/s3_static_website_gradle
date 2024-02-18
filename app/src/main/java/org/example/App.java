package org.example;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.s3.S3Client;

import org.slf4j.LoggerFactory;
import org.example.commands.Command;
import org.example.commands.CommandFactory;
import org.slf4j.Logger;

public class App {

    private static final Region region = Region.US_EAST_1;

    private static final String UPDATE_COMPLETE = "UPDATE_COMPLETE";

    private static final String DISTRIBUTION_STACK_NAME = "s3-static-website-distribution-stack";

    public static final String ENVIRONMENT_PARAMETER_KEY = "Environment";
    public static final String S3_STATIC_WEBSITE_ENVIRONMENT_TAG="s3_static_website_environment";

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    private CloudFormationClient cloudFormationClient = CloudFormationClient.builder().region(region).build();
    private S3Client s3Client = S3Client.create();

    private String environment;

    public App(String[] args) throws Exception {
        if (args.length!=2) {
            logger.error("Command is required");
        }

        String command = args[0];
        environment = args[1];

        Command listStacksCommand = CommandFactory.createList(this);
        Command createStackCommand = CommandFactory.createBootstrap(this);
        
        switch (command) {
            case "LIST": {
                listStacksCommand.execute();
                break;
            }
            case "BOOTSTRAP": {
                createStackCommand.execute();
                break;
            }
            case "DISTRIBUTION": {
                
                break;
            }
            

        }
    }

    public static void main(String[] args) {
        try {
            new App(args);
        } catch (Exception e) {
            screenMessage("*** ERROR *** "+e.getMessage());
        }
    }

    public String getEnvironment() {
        return this.environment;
    }

    public CloudFormationClient getCloudFormationClient() {
        return this.cloudFormationClient;
    }

    public S3Client getS3Client() {
        return this.s3Client;
    }

    public static final void screenMessage(String message) {
        logger.info("");
        logger.info(" -- {} --",message);        
        logger.info("");
    }
}
