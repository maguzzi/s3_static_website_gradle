package org.example.commands;

import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.Path;

import org.example.App;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.cloudformation.model.CreateStackRequest;
import software.amazon.awssdk.services.cloudformation.model.CreateStackResponse;
import software.amazon.awssdk.services.cloudformation.model.DescribeStacksRequest;
import software.amazon.awssdk.services.cloudformation.model.DescribeStacksResponse;
import software.amazon.awssdk.services.cloudformation.model.Parameter;
import software.amazon.awssdk.services.cloudformation.model.Tag;

public class CreateStackCommand extends AbstractCommand implements Command {

    private static final Logger logger = LoggerFactory.getLogger(CreateStackCommand.class);

    private static final String CREATE_COMPLETE = "CREATE_COMPLETE";

    private final String environmentString;
    private final String stackName;
    private final String templatePathString;

    public CreateStackCommand(CloudFormationClient cloudFormationClient,String environmentString,String templatePathString,String stackName) {
        super(cloudFormationClient);

        this.environmentString = environmentString;
        this.stackName = stackName;
        this.templatePathString = templatePathString;
    }

    @Override
    public void execute() throws Exception {
        App.screenMessage(String.format("%s - %s CREATION START",stackName,environmentString));

        // TODO file inside the jar
        Path templatePath = Paths.get(templatePathString);

        logger.info("reading template from: {}",templatePath.toAbsolutePath());

        String templateBody = new String(Files.readAllBytes(templatePath));

        Parameter environment = Parameter
            .builder()
            .parameterKey(App.ENVIRONMENT_PARAMETER_KEY)
            .parameterValue(environmentString)
            .build();

        String stackFullName = stackName+"-"+environmentString;

        CreateStackRequest request = CreateStackRequest.builder()
            .stackName(stackFullName)
            .templateBody(templateBody)
            .parameters(environment)
            .tags(Tag.builder().key(App.S3_STATIC_WEBSITE_ENVIRONMENT_TAG).value(environmentString).build())
            .build();

        CreateStackResponse response = cloudFormationClient.createStack(request);

        boolean creationComplete = checkIfStackIsCompleted(stackFullName);
        while (!creationComplete) {
            logger.info("Stack {} not yet completed, wait",stackFullName);
            creationComplete = checkIfStackIsCompleted(stackFullName);
        } 
        
        logger.info("Created stack {}",response.stackId());
        
        App.screenMessage(String.format("%s - %s CREATION END",stackName,environmentString));
    }

    private boolean checkIfStackIsCompleted(String stackFullName) {
        DescribeStacksRequest describeStacksRequest = DescribeStacksRequest.builder().stackName(stackFullName).build();
        DescribeStacksResponse describeStacksResponse = cloudFormationClient.describeStacks(describeStacksRequest);    
        return describeStacksResponse.stacks().stream().allMatch(it->it.stackStatusAsString().equals(CREATE_COMPLETE));
    }

}
