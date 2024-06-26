package it.marcoaguzzi.staticwebsite.commands.cloudformation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;

import it.marcoaguzzi.staticwebsite.App;
import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.cloudformation.model.CreateStackRequest;
import software.amazon.awssdk.services.cloudformation.model.DescribeStacksRequest;
import software.amazon.awssdk.services.cloudformation.model.DescribeStacksResponse;
import software.amazon.awssdk.services.cloudformation.model.Stack;
import software.amazon.awssdk.services.cloudformation.model.StackStatus;

public class CreateStackCommandTest {

    @Test
    public void testCreateWithTags() throws Exception {
        StackInfo stackInfo = StackInfo
                .builder()
                .environmentString("env")
                .templatePath("bootstrap/bootstrap.json")
                .stackName("stackName")
                .websiteName("website")
                .build();

        CloudFormationClient cloudFormationClient = mock(CloudFormationClient.class);

        when(cloudFormationClient.describeStacks(any(DescribeStacksRequest.class)))
                .thenReturn(stackCompletedResponse());

        CreateStackCommand createStack = new CreateStackCommand(cloudFormationClient, stackInfo,true);
        Map<String,Object> inputs = new HashMap<>();
        inputs.put(App.PSEUDO_RANDOM_TIMESTAMP_STRING_KEY, "12345");
        createStack.setInputs(inputs);
        createStack.execute();

        verify(cloudFormationClient).createStack(argThat(checkAllTagsArgumentMatcher("stackName")));

    }

    private DescribeStacksResponse stackCompletedResponse() {
        return DescribeStacksResponse
                .builder()
                .stacks(Stack.builder().stackId("stackId").stackStatus(StackStatus.CREATE_COMPLETE).build()).build();
    }

    private ArgumentMatcher<CreateStackRequest> checkAllTagsArgumentMatcher(String name) {
        return new ArgumentMatcher<CreateStackRequest>() {
            @Override
            public boolean matches(CreateStackRequest argument) {
                return TagChecker.stackContainsTag(name,argument.tags(),"website");
            }
        };
    }
}
