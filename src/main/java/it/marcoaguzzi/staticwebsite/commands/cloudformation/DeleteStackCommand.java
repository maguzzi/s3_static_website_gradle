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
                "s3-static-website-content-" + App.getEnvironment() + "-" + App.getPsedoRandomTimestampString(),
                "s3-static-website-lambda-artifact-" + App.getEnvironment() + "-"
                        + App.getPsedoRandomTimestampString());

        List<String> bucketOnAccount = s3Client.listBuckets(ListBucketsRequest.builder().build()).buckets().stream()
                .map(it -> it.name()).collect(Collectors.toList());

        if (!bucketOnAccount.containsAll(bucketNames)) {
            logger.debug("Bucket names\n{}", bucketNames.stream().collect(Collectors.joining("\n")));
            logger.debug("Bucket on account\n{}", bucketOnAccount.stream().collect(Collectors.joining("\n")));
            String message = "Can't find some or all of the bucket of the s3 static website config, please check your AWS config";
            logger.error(message);
            throw new Exception(message);
        }

        bucketNames.stream().forEach(b -> {
            s3Client.listObjects(ListObjectsRequest.builder().bucket(b).build())
                    .contents().forEach(o -> {
                        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(b).key(o.key()).build());
                        logger.info(o.key() + " deleted");
                    });
        });

        Optional<String> hostedZoneID = super.getHostedZoneID();
        if (hostedZoneID.isPresent()) {
            ListResourceRecordSetsResponse listResourceRecordSets = super.route53Client.listResourceRecordSets(
                    ListResourceRecordSetsRequest.builder().hostedZoneId(hostedZoneID.get()).build());
            listResourceRecordSets.resourceRecordSets()
                    .stream()
                    .forEach(it -> {

                        if (!RRType.NS.equals(it.type()) && !RRType.SOA.equals(it.type())) {
                            logger.warn("deleting {} - {}", it.type(), it.name());

                            ChangeResourceRecordSetsRequest changeResourceRecordSetsRequest = ChangeResourceRecordSetsRequest
                                    .builder()
                                    .hostedZoneId(hostedZoneID.get())
                                    .changeBatch(ChangeBatch.builder().changes(
                                            Change
                                                    .builder()
                                                    .action(ChangeAction.DELETE)
                                                    .resourceRecordSet(it).build())
                                            .build())
                                    .build();

                            route53Client.changeResourceRecordSets(changeResourceRecordSetsRequest);
                        } else {
                            logger.info("leaving {} - {}", it.type(), it.name());
                        }
                    });

        } else {
            String message = "Can't find hostedZoneID, check your AWS configuration";
            logger.error(message);
            throw new Exception(message);
        }

        // TODO fix stack name
        String stackName = "s3-static-website-bootstrap-stack-dev";
        logger.info("Deleting stack {}",stackName);
        cloudFormationClient.deleteStack(DeleteStackRequest.builder().stackName(stackName).build());

        return new HashMap<>();
    }

}
