package org.example;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import org.slf4j.LoggerFactory;
import org.example.commands.Command;
import org.example.commands.CreateStackCommand;
import org.example.commands.ListStacksCommand;
import org.slf4j.Logger;

public class App {

    private static final Region region = Region.US_EAST_1;

    
    private static final String UPDATE_COMPLETE = "UPDATE_COMPLETE";

    private static final String BOOTSTRAP_STACK_NAME = "s3-static-website-bootstrap-stack";
    private static final String DISTRIBUTION_STACK_NAME = "s3-static-website-distribution-stack";

    public static final String ENVIRONMENT_PARAMETER_KEY = "Environment";
    public static final String S3_STATIC_WEBSITE_ENVIRONMENT_TAG="s3_static_website_environment";

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    private static CloudFormationClient cloudFormationClient = CloudFormationClient.builder().region(region).build();

    private static Command createBootstrap(String environment) {
        return new CreateStackCommand(cloudFormationClient, environment, "./src/main/resources/bootstrap/bootstrap.json",BOOTSTRAP_STACK_NAME);
    }

    public static void main(String[] args) {
        try {
            if (args.length!=2) {
                logger.error("Command is required");
            }
            String command = args[0];
            String environment = args[1];

            Command listStacksCommand = new ListStacksCommand(cloudFormationClient);
            Command createStackCommand = createBootstrap(environment);

            switch (command) {
                case "LIST": {
                    listStacksCommand.execute();
                    break;
                }
                case "BOOTSTRAP": {
                    createStackCommand.execute();
                    break;
                }
                

            }
        } catch (Exception e) {
            screenMessage("*** ERROR *** "+e.getMessage());
        }
    }

    public static final void screenMessage(String message) {
        logger.info("");
        logger.info(" -- {} --",message);        
        logger.info("");
    }
}
