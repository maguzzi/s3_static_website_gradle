package it.marcoaguzzi.staticwebsite.commands.cloudformation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import it.marcoaguzzi.staticwebsite.Utils;
import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.cloudformation.model.ListStackResourcesRequest;
import software.amazon.awssdk.services.cloudformation.model.ListStackResourcesResponse;
import software.amazon.awssdk.services.cloudformation.model.StackResourceSummary;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.route53.model.ListResourceRecordSetsRequest;
import software.amazon.awssdk.services.route53.model.ListResourceRecordSetsResponse;
import software.amazon.awssdk.services.route53.model.RRType;
import software.amazon.awssdk.services.route53.model.ResourceRecord;
import software.amazon.awssdk.services.route53.model.ResourceRecordSet;

public class GetRoute53InfoCommandTest {

    private CloudFormationClient cloudFormationClientMock = mock(CloudFormationClient.class);
    private Route53Client route53ClientMock = mock(Route53Client.class);

    private void testRoute53InfoCommand(Map<String,Object> inputs) throws Exception {
        when(cloudFormationClientMock.listStackResources(any(ListStackResourcesRequest.class))).thenReturn(listStackResourcesResponse());
        when(route53ClientMock.listResourceRecordSets(any(ListResourceRecordSetsRequest.class))).thenReturn(listResourceRecordSetsResponse());
        GetRoute53InfoCommand getRoute53InfoCommand = new GetRoute53InfoCommand(cloudFormationClientMock,route53ClientMock);
        getRoute53InfoCommand.setInputs(inputs);
        getRoute53InfoCommand.execute();
    }

    @Test
    public void testRoute53InfoCommandWithoutFile() throws Exception {
        Map<String, Object> inputs = new HashMap<>();
        inputs.put(GetRoute53InfoCommand.DNS_OUT_FILE,"");
        testRoute53InfoCommand(inputs);
    }

    @Test
    public void testRoute53InfoCommandWithFile() throws Exception {
        Map<String, Object> inputs = new HashMap<>();
        File tempFile = File.createTempFile("dns_info", ".tmp");
        String expected = Utils.readFileContent("dnsInfo.txt");
        inputs.put(GetRoute53InfoCommand.DNS_OUT_FILE,tempFile.getAbsolutePath());
        testRoute53InfoCommand(inputs);
        String actual = Utils.readFileContent(tempFile.getAbsolutePath());
        assertEquals(expected, actual);
    }

    private ListStackResourcesResponse listStackResourcesResponse() {
        return ListStackResourcesResponse.builder()
        .stackResourceSummaries(
            StackResourceSummary.builder()
            .resourceType("AWS::Route53::HostedZone")
            .physicalResourceId("HOSTED_ZONE_ID_TEST")
            .build()
        )
        .build();
    }

    private ListResourceRecordSetsResponse listResourceRecordSetsResponse() {
        return ListResourceRecordSetsResponse.builder()
        .resourceRecordSets(
            ResourceRecordSet.builder()
            .type(RRType.NS)
            .name("test")
            .ttl(3600L)
            .resourceRecords(Arrays.asList(ResourceRecord.builder().value("ns-12345.awsdns-00.co.uk.").build()))
            .build()
        ).build();
    }
}
