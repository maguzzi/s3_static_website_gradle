package it.marcoaguzzi.staticwebsite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.cloudformation.model.ListStacksRequest;
import software.amazon.awssdk.services.cloudformation.model.ListStacksResponse;
import software.amazon.awssdk.services.s3.S3Client;

public class AppTest {

    private static final Logger logger = LoggerFactory.getLogger(AppTest.class);

    @Test
    public void testAppNoParams() {
        try {
            new App(null,null,null);
            fail("Should ask for command");
        } catch (Exception e) {
            assertEquals("Command is required",e.getMessage());
        }
    }

    @Test
    public void testAppNoEnv() {
        try {
            new App(new String[]{"COMMAND"},null,null);
            fail("Should ask for command");
        } catch (Exception e) {
            assertEquals("Command is required",e.getMessage());
        }
    }

    @Test
    public void testAppListEnvNoStacks() throws Exception {

        CloudFormationClient cloudFormationClientMock = mock(CloudFormationClient.class);
        S3Client s3ClientMock = mock(S3Client.class);

        ListStacksResponse listStacksResponse = ListStacksResponse
        .builder()
        .build();

        when(cloudFormationClientMock.listStacks(any(ListStacksRequest.class))).thenReturn(listStacksResponse);

        new App(new String[]{"LIST","dev"},cloudFormationClientMock,s3ClientMock);
        
    }
}
