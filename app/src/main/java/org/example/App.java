package org.example;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.s3.S3Client;

import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import org.example.commands.Command;
import org.example.commands.CommandFactory;
import org.slf4j.Logger;

import static org.example.commands.s3.UploadFileToBucketCommand.REMOTE_FILE_URL;
import static org.example.commands.misc.PackageTemplateCommand.S3_PATH_TO_REPLACE;;

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
        Command getOutputFromStack = CommandFactory.createGetOutputFromBootstrapStack(this);
        Command uploadLambdaNestedStackTemplateFileToBucket = CommandFactory.createUploadLambdaNestedStackTemplateFileToBucket(this);
        Command uploadLambdaNestedStackSourceCodeFileToBucket = CommandFactory.createUploadLambdaNestedStackSourceCodeFileToBucket(this);
        Command compileTemplateCommand = CommandFactory.createPackageTemplateCommand(this);
        
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
                Map<String,String> outputFromStackResult = getOutputFromStack.execute();
                mapToString(outputFromStackResult);
                
                Map<String,String> uploadLambdaTemplateResult = uploadLambdaNestedStackTemplateFileToBucket.execute();
                mapToString(uploadLambdaTemplateResult);
                


                Map<String,String> uploadLambdaArtifactResult = uploadLambdaNestedStackSourceCodeFileToBucket.execute();
                mapToString(uploadLambdaArtifactResult);
                
                HashMap<String,Object> inputs= new HashMap<String,Object>();
                inputs.put(S3_PATH_TO_REPLACE, uploadLambdaTemplateResult.get(REMOTE_FILE_URL));
                compileTemplateCommand.setInputs(inputs);
                Map<String, String> result4 = compileTemplateCommand.execute();
                mapToString(result4);
                break;


            }
            

        }
    }

    private void mapToString(Map<String,String> result) {
        result.entrySet().forEach(e -> logger.debug("{} -> {}",e.getKey(),e.getValue()));
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
