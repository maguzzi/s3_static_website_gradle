package it.marcoaguzzi.staticwebsite.commands.cloudformation;

import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.marcoaguzzi.staticwebsite.App;
import it.marcoaguzzi.staticwebsite.commands.Command;
import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.cloudformation.model.ListStackResourcesRequest;
import software.amazon.awssdk.services.cloudformation.model.ListStackResourcesResponse;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.route53.model.ListResourceRecordSetsRequest;
import software.amazon.awssdk.services.route53.model.ListResourceRecordSetsResponse;
import software.amazon.awssdk.services.route53.model.RRType;
import software.amazon.awssdk.services.route53.model.ResourceRecordSet;

public class GetRoute53InfoCommand implements Command {

    public static final String STACK_NAME = "StackName";

    private static final Logger logger = LoggerFactory.getLogger(GetRoute53InfoCommand.class);

    protected final Route53Client route53Client;
    protected final CloudFormationClient cloudFormationClient;
    protected String stackName;

    public GetRoute53InfoCommand(CloudFormationClient cloudFormationClient,Route53Client route53Client) {
        this.cloudFormationClient = cloudFormationClient;
        this.route53Client = route53Client;
    }

    protected Optional<String> getHostedZoneID() {
        ListStackResourcesRequest listStackResourcesRequest = ListStackResourcesRequest
        .builder()
        .stackName(stackName)
        .build();
        
        ListStackResourcesResponse listStackResourcesResponse = cloudFormationClient.listStackResources(listStackResourcesRequest);

        Optional<String> first = listStackResourcesResponse
            .stackResourceSummaries()
            .stream()
            .filter(s -> "AWS::Route53::HostedZone".equals(s.resourceType()))
            .map(s->s.physicalResourceId()).findFirst();

        logger.info("Got hosted zone Id {} for stack {}",first,stackName);

        return first;
    }

    @Override
    public Map<String, OutputEntry> execute() throws Exception {
        Map<String,OutputEntry> outputMap = new HashMap<>();

        App.screenMessage("ROUTE 53 INFO START");

        Optional<String> hostedZoneID = getHostedZoneID();

        if (hostedZoneID.isPresent()) {    
            logger.info("hostedZoneId: {}",hostedZoneID);
            ListResourceRecordSetsResponse listResourceRecordSets = route53Client.listResourceRecordSets(ListResourceRecordSetsRequest.builder().hostedZoneId(hostedZoneID.get()).build());
            Optional<ResourceRecordSet> rrs = listResourceRecordSets.resourceRecordSets()
                .stream()
                .filter(r -> RRType.NS.equals(r.type()))
                .findFirst();
            if (rrs.isPresent()) {
                logger.info("Name: {} TTL: {}",rrs.get().name(),rrs.get().ttl());
                rrs.get().resourceRecords().forEach(it->logger.info(it.value()));
            } else {
                logger.error("Can't find NS records!");    
            } 


        } else{
            logger.error("Can't find hostedZoneID!");
        }

        

        App.screenMessage("ROUTE 53 INFO END");
        return outputMap;
    }

    @Override
    public void setInputs(Map<String, Object> inputs) {
        stackName = (String)inputs.get(STACK_NAME);
    }

    
}