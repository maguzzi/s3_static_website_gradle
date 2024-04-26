package it.marcoaguzzi.staticwebsite.commands.cloudformation;

import static it.marcoaguzzi.staticwebsite.commands.cloudformation.StackCompleteAndId.fromDescribeStackResponse;

import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.cloudformation.model.DescribeStacksRequest;
import software.amazon.awssdk.services.cloudformation.model.DescribeStacksResponse;
import software.amazon.awssdk.services.cloudformation.model.StackStatus;

public class StackCompleteChecker {

    private static final Logger logger = LoggerFactory.getLogger(StackCompleteChecker.class);

    private CloudFormationClient cloudFormationClient;
    private DescribeStacksRequest describeStacksRequest;
    private String stackName;

    public StackCompleteChecker(CloudFormationClient cloudFormationClient, String stackName) {
        this.cloudFormationClient = cloudFormationClient;
        this.describeStacksRequest = DescribeStacksRequest.builder().stackName(stackName).build();
        this.stackName = stackName;
    }

    // TODO timeout check
    public void check(Function<String, Void> handler) throws Exception {
        StackCompleteAndId stackCompleteAndId = fromDescribeStackResponse(cloudFormationClient.describeStacks(describeStacksRequest));
        while (!stackCompleteAndId.getComplete()) {
            logger.info("Stack {} not yet completed, wait", stackName);
            stackCompleteAndId = fromDescribeStackResponse(cloudFormationClient.describeStacks(describeStacksRequest));
            Thread.sleep(5000);
        }
        handler.apply(stackCompleteAndId.getId());
    }

}

class StackCompleteAndId {

    private Boolean complete;
    private String id;

    public static StackCompleteAndId fromDescribeStackResponse(DescribeStacksResponse describeStacksResponse) {
        return new StackCompleteAndId(
                describeStacksResponse.stacks().stream()
                        .allMatch(it -> it.stackStatus().equals(StackStatus.CREATE_COMPLETE)),
                describeStacksResponse.stacks().iterator().next().stackId());
    }

    private StackCompleteAndId(Boolean complete, String id) {
        this.complete = complete;
        this.id = id;
    }

    public Boolean getComplete() {
        return complete;
    }

    public String getId() {
        return id;
    }
}