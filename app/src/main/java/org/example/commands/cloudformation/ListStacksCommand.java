package org.example.commands.cloudformation;

import java.util.List;
import java.util.stream.Collectors;

import org.example.App;
import org.example.commands.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.cloudformation.model.DescribeStacksRequest;
import software.amazon.awssdk.services.cloudformation.model.DescribeStacksResponse;
import software.amazon.awssdk.services.cloudformation.model.ListStacksRequest;
import software.amazon.awssdk.services.cloudformation.model.ListStacksResponse;
import software.amazon.awssdk.services.cloudformation.model.StackSummary;
import software.amazon.awssdk.services.cloudformation.model.Stack;

public class ListStacksCommand implements Command {

    private static final Logger logger = LoggerFactory.getLogger(ListStacksCommand.class);

    private CloudFormationClient cloudFormationClient;

    public ListStacksCommand(CloudFormationClient cloudFormationClient) {
        this.cloudFormationClient = cloudFormationClient;
    }

    @Override
    public void execute() throws Exception {

        App.screenMessage("LIST STACK START");

        ListStacksResponse listStacksResponse = cloudFormationClient.listStacks(
        ListStacksRequest.builder()
            .stackStatusFiltersWithStrings("CREATE_COMPLETE","UPDATE_COMPLETE")
            .build()
        );

        for (StackSummary stackSummary : listStacksResponse.stackSummaries()) {
            logger.trace("Checking stack {}",stackSummary.stackName());
            DescribeStacksRequest describeStacksRequest = DescribeStacksRequest.builder().stackName(stackSummary.stackName()).build();
            DescribeStacksResponse describeStacksResponse = cloudFormationClient.describeStacks(describeStacksRequest);    
            List<Stack> stacks = describeStacksResponse.stacks().stream().filter(i->i.tags().stream().anyMatch(j->j.key().equals(App.S3_STATIC_WEBSITE_ENVIRONMENT_TAG))).collect(Collectors.toList());
            stacks.forEach(it->logger.info("{} ({}) - {}", it.stackName(),it.stackStatusAsString(),it.tags()));
        }

        App.screenMessage("LIST STACK END");
    }

}