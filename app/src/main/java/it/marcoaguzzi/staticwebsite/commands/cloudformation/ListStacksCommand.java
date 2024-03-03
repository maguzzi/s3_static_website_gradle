package it.marcoaguzzi.staticwebsite.commands.cloudformation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            if (!Arrays.asList(StackStatus.CREATE_COMPLETE,StackStatus.UPDATE_COMPLETE).contains(stackSummary.stackStatus())) {
                logger.debug("Stack {} in status {}. Skipping...", stackSummary.stackName(),stackSummary.stackStatus());
                continue;
            }
            DescribeStacksRequest describeStacksRequest = DescribeStacksRequest.builder()
                    .stackName(stackSummary.stackName()).build();
            DescribeStacksResponse describeStacksResponse = cloudFormationClient.describeStacks(describeStacksRequest);
            List<Stack> stacks = describeStacksResponse.stacks().stream()
                    .filter(it -> TagChecker.stackContainsTag(it.stackName(),it.tags()))
                    .collect(Collectors.toList());
            stacks.forEach(it -> {
                outputMap.put(LISTED_STACKS, new OutputEntry(it.stackName(), it.stackStatusAsString()));
                logger.info("{} ({}) - {}", it.stackName(), it.stackStatusAsString(), it.tags());
            });

        }
        App.screenMessage("LIST STACK END");
        return outputMap;
    }
}