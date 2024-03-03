package it.marcoaguzzi.staticwebsite.commands.cloudformation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.marcoaguzzi.staticwebsite.App;
import it.marcoaguzzi.staticwebsite.commands.Command;
import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.cloudformation.model.DescribeStacksRequest;
import software.amazon.awssdk.services.cloudformation.model.DescribeStacksResponse;
import software.amazon.awssdk.services.cloudformation.model.ListStacksRequest;
import software.amazon.awssdk.services.cloudformation.model.ListStacksResponse;
import software.amazon.awssdk.services.cloudformation.model.StackSummary;
import software.amazon.awssdk.services.cloudformation.model.Tag;
import software.amazon.awssdk.services.cloudformation.model.Stack;
import software.amazon.awssdk.services.cloudformation.model.StackStatus;

public class ListStacksCommand implements Command {

    private static final Logger logger = LoggerFactory.getLogger(ListStacksCommand.class);

    private static final String LISTED_STACKS = "LISTED_STACKS";

    private CloudFormationClient cloudFormationClient;

    public ListStacksCommand(CloudFormationClient cloudFormationClient) {
        this.cloudFormationClient = cloudFormationClient;
    }

    @Override
    public Map<String, OutputEntry> execute() throws Exception {

        App.screenMessage("LIST STACK START");

        ListStacksResponse listStacksResponse = cloudFormationClient.listStacks(ListStacksRequest.builder().build());

        Map<String, OutputEntry> outputMap = new HashMap<>();

        for (StackSummary stackSummary : listStacksResponse.stackSummaries()) {
            logger.trace("Checking stack {}", stackSummary.stackName());
            DescribeStacksRequest describeStacksRequest = DescribeStacksRequest.builder()
                    .stackName(stackSummary.stackName()).build();
            DescribeStacksResponse describeStacksResponse = cloudFormationClient.describeStacks(describeStacksRequest);
            List<Stack> stacks = describeStacksResponse.stacks().stream()
                    .filter(it -> Arrays.asList(StackStatus.CREATE_COMPLETE, StackStatus.UPDATE_COMPLETE)
                            .contains(it.stackStatus()))
                    .filter(new AllTagsPresenceInStackPredicate())
                    .collect(Collectors.toList());
            stacks.forEach(it -> {
                outputMap.put(LISTED_STACKS, new OutputEntry(it.stackName(), it.stackStatusAsString()));
                logger.info("{} ({}) - {}", it.stackName(), it.stackStatusAsString(), it.tags());
            });

        }
        App.screenMessage("LIST STACK END");
        return outputMap;
    }

    private class AllTagsPresenceInStackPredicate implements Predicate<Stack> {
        @Override
        public boolean test(Stack stack) {
            return stack.tags().stream()
                    .map(it -> it.key())
                    .collect(Collectors.toList())
                    .containsAll(Arrays.asList(
                            App.S3_STATIC_WEBSITE_TAG,
                            App.S3_STATIC_WEBSITE_ENVIRONMENT_TAG));
        }

    }
}