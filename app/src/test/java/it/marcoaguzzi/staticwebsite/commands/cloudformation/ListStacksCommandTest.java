package it.marcoaguzzi.staticwebsite.commands.cloudformation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.marcoaguzzi.staticwebsite.App;
import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.cloudformation.model.DescribeStacksRequest;
import software.amazon.awssdk.services.cloudformation.model.DescribeStacksResponse;
import software.amazon.awssdk.services.cloudformation.model.ListStacksRequest;
import software.amazon.awssdk.services.cloudformation.model.ListStacksResponse;
import software.amazon.awssdk.services.cloudformation.model.Stack;
import software.amazon.awssdk.services.cloudformation.model.StackStatus;
import software.amazon.awssdk.services.cloudformation.model.StackSummary;
import software.amazon.awssdk.services.cloudformation.model.Tag;

public class ListStacksCommandTest {

    // private static final Logger logger = LoggerFactory.getLogger(ListStacksCommandTest.class);

    private static final String STACK_NAME = "StackName";
    private static final String ENVIRONMENT = "env";
    private static final String WEBSITE_NAME = "My beautiful website";


    @Test
    public void testAppListEnvShowStacksWithTag() throws Exception {

        CloudFormationClient cloudFormationClientMock = mock(CloudFormationClient.class);

        ListStacksResponse listStacksResponse = ListStacksResponse
                .builder()
                .stackSummaries(StackSummary.builder().stackName(STACK_NAME).stackStatus(StackStatus.CREATE_COMPLETE).build())
                .build();
        when(cloudFormationClientMock.listStacks(any(ListStacksRequest.class))).thenReturn(listStacksResponse);

        DescribeStacksResponse describeStacksResponse = DescribeStacksResponse
                .builder()
                .stacks(testStack(STACK_NAME, testTags(WEBSITE_NAME,ENVIRONMENT)))
                .build();
        when(cloudFormationClientMock.describeStacks(any(DescribeStacksRequest.class)))
                .thenReturn(describeStacksResponse);

    ListStacksCommand listStacksCommand = new ListStacksCommand(cloudFormationClientMock);

    Map<String, OutputEntry> result = listStacksCommand.execute();
    assertEquals(1, result.size());

    }

    private List<Tag> testTags(String websiteName,String env) {
        return Arrays.asList(
            testTag(App.S3_STATIC_WEBSITE_TAG, websiteName),
            testTag(App.S3_STATIC_WEBSITE_ENVIRONMENT_TAG, env)
        );
    }

    private Tag testTag(String key, String value) {
        return Tag.builder().key(key).value(value).build();
    }

    private Stack testStack(String stackName, List<Tag> tags) {
        return Stack
                .builder()
                .stackName(stackName)
                .stackStatus(StackStatus.CREATE_COMPLETE)
                .tags(tags)
                .build();
    }
}
