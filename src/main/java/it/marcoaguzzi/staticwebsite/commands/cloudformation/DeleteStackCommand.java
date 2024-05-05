package it.marcoaguzzi.staticwebsite.commands.cloudformation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.marcoaguzzi.staticwebsite.App;
import it.marcoaguzzi.staticwebsite.commands.CommandFactory;
import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.cloudformation.model.DeleteStackRequest;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.route53.model.Change;
import software.amazon.awssdk.services.route53.model.ChangeAction;
import software.amazon.awssdk.services.route53.model.ChangeBatch;
import software.amazon.awssdk.services.route53.model.ChangeResourceRecordSetsRequest;
import software.amazon.awssdk.services.route53.model.ListResourceRecordSetsRequest;
import software.amazon.awssdk.services.route53.model.ListResourceRecordSetsResponse;
import software.amazon.awssdk.services.route53.model.RRType;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;

public class DeleteStackCommand extends GetRoute53InfoCommand {

    private static final Logger logger = LoggerFactory.getLogger(DeleteStackCommand.class);

    private S3Client s3Client;

    public DeleteStackCommand(CloudFormationClient cloudFormationClient, Route53Client route53Client,
            S3Client s3Client) {
        super(cloudFormationClient, route53Client);
        this.s3Client = s3Client;
    }

    @Override
    public Map<String, OutputEntry> execute() throws Exception {
        List<String> bucketNames = Arrays.asList(
                "s3-static-website-compiled-template-" + App.getEnvironment() + "-"
                        + App.getPsedoRandomTimestampString(),
                "s3-static-website-content-" + App.getEnvironment() + "-" +
                        App.getPsedoRandomTimestampString(),
                "s3-static-website-lambda-artifact-" + App.getEnvironment() + "-"
                        + App.getPsedoRandomTimestampString());

        // TODO strict s3 bucket control enable after more stable
        List<String> bucketNamesToDelete = s3Client.listBuckets(ListBucketsRequest.builder().build()).buckets()
                .stream()
                .filter(it -> bucketNames.contains(it.name()))
                .map(it -> it.name())
                .collect(Collectors.toList());

        bucketNamesToDelete.stream().forEach(b -> {
            logger.info("Checking {} objects", b);
            s3Client.listObjects(ListObjectsRequest.builder().bucket(b).build())
                    .contents().forEach(o -> {
                        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(b).key(o.key()).build());
                        logger.info(o.key() + " deleted");
                    });
        });

        /*
         * Optional<String> hostedZoneID = super.getHostedZoneID();
         * if (hostedZoneID.isPresent()) {
         * ListResourceRecordSetsResponse listResourceRecordSets =
         * super.route53Client.listResourceRecordSets(
         * ListResourceRecordSetsRequest.builder().hostedZoneId(hostedZoneID.get()).
         * build());
         * listResourceRecordSets.resourceRecordSets()
         * .stream()
         * .forEach(it -> {
         * 
         * if (!RRType.NS.equals(it.type()) && !RRType.SOA.equals(it.type())) {
         * logger.warn("deleting {} - {}", it.type(), it.name());
         * 
         * ChangeResourceRecordSetsRequest changeResourceRecordSetsRequest =
         * ChangeResourceRecordSetsRequest
         * .builder()
         * .hostedZoneId(hostedZoneID.get())
         * .changeBatch(ChangeBatch.builder().changes(
         * Change
         * .builder()
         * .action(ChangeAction.DELETE)
         * .resourceRecordSet(it).build())
         * .build())
         * .build();
         * 
         * route53Client.changeResourceRecordSets(changeResourceRecordSetsRequest);
         * } else {
         * logger.info("leaving {} - {}", it.type(), it.name());
         * }
         * });
         * 
         * } else {
         * logger.warn("Can't find hostedZoneID, check your AWS configuration");
         * }
         * 
         * String distributionStackName = CommandFactory.DISTRIBUTION_STACK_NAME + "-" +
         * App.getEnvironment();
         * logger.info("Deleting stack {}", distributionStackName);
         * cloudFormationClient.deleteStack(DeleteStackRequest.builder().stackName(
         * distributionStackName).build());
         */

        String bootstrapStackName = "s3-static-website-bootstrap-stack-" + App.getEnvironment();
        logger.info("Deleting stack {}", bootstrapStackName);
        cloudFormationClient.deleteStack(DeleteStackRequest.builder().stackName(
                bootstrapStackName).build());

        // removeLambdaFunctionAssociation(); // this seems immediate, but not enough to
        // delete the stack
        return new HashMap<>();
    }
}
