package it.marcoaguzzi.staticwebsite.commands.cloudformation;

import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.cloudformation.model.DescribeStacksRequest;
import software.amazon.awssdk.services.cloudformation.model.DescribeStacksResponse;

public class StackCompleteChecker {

    private static final String CREATE_COMPLETE = "CREATE_COMPLETE";

    private static final Logger logger = LoggerFactory.getLogger(StackCompleteChecker.class);

    private CloudFormationClient cloudFormationClient;
    private DescribeStacksRequest describeStacksRequest;
    private String stackName;

    public StackCompleteChecker(CloudFormationClient cloudFormationClient,String stackName) {
        this.cloudFormationClient = cloudFormationClient;
        this.describeStacksRequest = DescribeStacksRequest.builder().stackName(stackName).build();
        this.stackName = stackName;
    }

    // TODO timeout check
    // substituite object with bean
    public void check(Function<String,Void> handler) throws Exception {
        Object[] creationComplete = checkIfStackIsCompleted(stackName);
        while (!((Boolean)creationComplete[0])) {
            logger.info("Stack {} not yet completed, wait",stackName);
            creationComplete = checkIfStackIsCompleted(stackName);
            Thread.sleep(5000);
        } 
        handler.apply((String)creationComplete[1]);
    }

    private Object[] checkIfStackIsCompleted(String stackFullName) {
        DescribeStacksResponse describeStacksResponse = cloudFormationClient.describeStacks(describeStacksRequest);    
        return new Object[]{describeStacksResponse.stacks().stream().allMatch(it->it.stackStatusAsString().equals(CREATE_COMPLETE)),describeStacksResponse.stacks().get(0).stackId()};
    }         
         
}
