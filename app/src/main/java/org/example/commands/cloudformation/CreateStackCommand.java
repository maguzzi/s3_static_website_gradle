package org.example.commands.cloudformation;

import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.Path;

import org.example.App;
import org.example.commands.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.cloudformation.model.CreateStackRequest;
import software.amazon.awssdk.services.cloudformation.model.CreateStackResponse;
import software.amazon.awssdk.services.cloudformation.model.DescribeStacksRequest;
import software.amazon.awssdk.services.cloudformation.model.DescribeStacksResponse;
import software.amazon.awssdk.services.cloudformation.model.Parameter;
import software.amazon.awssdk.services.cloudformation.model.Tag;

public class CreateStackCommand implements Command {

    private static final Logger logger = LoggerFactory.getLogger(CreateStackCommand.class);

    private static final String CREATE_COMPLETE = "CREATE_COMPLETE";

    private StackParams stackParams;
    private final CloudFormationClient cloudFormationClient;

    public CreateStackCommand(CloudFormationClient cloudFormationClient,StackParams stackParams) {
        this.cloudFormationClient = cloudFormationClient;
        this.stackParams = stackParams;
    }

    @Override
    public Map<String,String> execute() throws Exception {
        App.screenMessage(String.format("%s - %s CREATION START",stackParams.getStackName(),stackParams.getEnvironmentString()));

        // TODO file inside the jar
        Path templatePath = Paths.get(stackParams.getTemplatePath());

        logger.info("reading template from: {}",templatePath.toAbsolutePath());

        String templateBody = new String(Files.readAllBytes(templatePath));

        Parameter environment = Parameter
            .builder()
            .parameterKey(App.ENVIRONMENT_PARAMETER_KEY)
            .parameterValue(stackParams.getEnvironmentString())
            .build();

        String stackFullName = stackParams.getStackName()+"-"+stackParams.getEnvironmentString();

        CreateStackRequest request = CreateStackRequest.builder()
            .stackName(stackFullName)
            .templateBody(templateBody)
            .parameters(environment)
            .tags(Tag.builder().key(App.S3_STATIC_WEBSITE_ENVIRONMENT_TAG).value(stackParams.getEnvironmentString()).build())
            .build();

        CreateStackResponse response = cloudFormationClient.createStack(request);

        // TODO timeout check
        boolean creationComplete = checkIfStackIsCompleted(stackFullName);
        while (!creationComplete) {
            logger.info("Stack {} not yet completed, wait",stackFullName);
            creationComplete = checkIfStackIsCompleted(stackFullName);
            Thread.sleep(5000);
        } 
        
        logger.info("Created stack {}",response.stackId());
        
        App.screenMessage(String.format("%s - %s CREATION END",stackParams.getStackName(),stackParams.getEnvironmentString()));

        Map<String,String> result = new HashMap<String,String>();

        return result;
    }

    private boolean checkIfStackIsCompleted(String stackFullName) {
        DescribeStacksRequest describeStacksRequest = DescribeStacksRequest.builder().stackName(stackFullName).build();
        DescribeStacksResponse describeStacksResponse = cloudFormationClient.describeStacks(describeStacksRequest);    
        return describeStacksResponse.stacks().stream().allMatch(it->it.stackStatusAsString().equals(CREATE_COMPLETE));
    }

}
