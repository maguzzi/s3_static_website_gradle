package org.example.commands.cloudformation;

import org.example.commands.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.cloudformation.model.DescribeStacksRequest;
import software.amazon.awssdk.services.cloudformation.model.DescribeStacksResponse;

public class GetOutputFromStack implements Command {

    private static final Logger logger = LoggerFactory.getLogger(GetOutputFromStack.class);


    private CloudFormationClient cloudFormationClient;
    private String stackName;

    public GetOutputFromStack(CloudFormationClient cloudFormationClient,String stackName) {
        this.cloudFormationClient = cloudFormationClient;
        this.stackName = stackName;
    }

    @Override
    public void execute() throws Exception {
        DescribeStacksRequest describeStacksRequest = DescribeStacksRequest.builder().stackName(stackName).build();
        DescribeStacksResponse describeStacksResponse = cloudFormationClient.describeStacks(describeStacksRequest);          
        logger.info(describeStacksResponse.toString());
    }
    
}
