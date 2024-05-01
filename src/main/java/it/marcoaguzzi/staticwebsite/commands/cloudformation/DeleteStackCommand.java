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
import software.amazon.awssdk.services.cloudformation.model.DescribeStacksRequest;
import software.amazon.awssdk.services.cloudformation.model.DescribeStacksResponse;
import software.amazon.awssdk.services.cloudformation.model.Stack;
import software.amazon.awssdk.services.cloudfront.CloudFrontClient;
import software.amazon.awssdk.services.cloudfront.model.CachePolicyConfig;
import software.amazon.awssdk.services.cloudfront.model.CachePolicyList;
import software.amazon.awssdk.services.cloudfront.model.DefaultCacheBehavior;
import software.amazon.awssdk.services.cloudfront.model.DeleteFunctionRequest;
import software.amazon.awssdk.services.cloudfront.model.Distribution;
import software.amazon.awssdk.services.cloudfront.model.DistributionConfig;
import software.amazon.awssdk.services.cloudfront.model.FunctionAssociations;
import software.amazon.awssdk.services.cloudfront.model.GetCachePolicyConfigRequest;
import software.amazon.awssdk.services.cloudfront.model.GetCachePolicyRequest;
import software.amazon.awssdk.services.cloudfront.model.GetDistributionRequest;
import software.amazon.awssdk.services.cloudfront.model.GetDistributionResponse;
import software.amazon.awssdk.services.cloudfront.model.LambdaFunctionAssociations;
import software.amazon.awssdk.services.cloudfront.model.ListCachePoliciesRequest;
import software.amazon.awssdk.services.cloudfront.model.ListCachePoliciesResponse;
import software.amazon.awssdk.services.cloudfront.model.ListFunctionsRequest;
import software.amazon.awssdk.services.cloudfront.model.UpdateCachePolicyRequest;
import software.amazon.awssdk.services.cloudfront.model.UpdateDistributionRequest;
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
        logger.warn("This command is still WIP");
        return new HashMap<>();
    }

    private void removeLambdaFunctionAssociation() throws Exception {
        List<Stack> stacks = cloudFormationClient.describeStacks(
                DescribeStacksRequest
                        .builder()
                        .stackName(CommandFactory.DISTRIBUTION_STACK_NAME + "-" + App.getEnvironment())
                        .build())
                .stacks();

        // TODO should be one and one stack only
        if (stacks.size() != 1) {
            throw new Exception("There should be only one stack for " + CommandFactory.DISTRIBUTION_STACK_NAME + "-"
                    + App.getEnvironment());
        }

        Optional<String> cloudfrontDistributionOpt = stacks.get(0).outputs().stream()
                .filter(it -> "CloudFrontDistributionId".equals(it.outputKey())).map(it -> it.outputValue())
                .findFirst();
        if (!cloudfrontDistributionOpt.isPresent()) {
            throw new Exception("can't find distribution id");
        } else {
            CloudFrontClient cloudFrontClient = CloudFrontClient.builder().build();

            GetDistributionResponse getDistributionResponse = cloudFrontClient
                    .getDistribution(GetDistributionRequest.builder().id(cloudfrontDistributionOpt.get()).build());

            Distribution distribution = getDistributionResponse.distribution();
            String eTag = getDistributionResponse.eTag();

            logger.info("Cloudfront distribution ID {} - {}", cloudfrontDistributionOpt.get(),
                    distribution.domainName());

            logger.trace("distribution config as is: {}", distribution.distributionConfig());

            DistributionConfig distributionConfigToBe = distribution
                    .distributionConfig()
                    .copy(b -> b.defaultCacheBehavior(
                            distribution.distributionConfig().defaultCacheBehavior()
                                    .copy(d -> d.lambdaFunctionAssociations(
                                            LambdaFunctionAssociations.builder().quantity(0).build()))));

            logger.trace("distribution config to be: {}", distributionConfigToBe);

            logger.trace("{}", distributionConfigToBe.equals(distribution.distributionConfig()));

            logger.info("updating distribution to remove lambda function associations {} - {}",
                    cloudfrontDistributionOpt.get(), eTag);
            cloudFrontClient.updateDistribution(UpdateDistributionRequest
                    .builder()
                    .id(cloudfrontDistributionOpt.get())
                    .ifMatch(eTag)
                    .distributionConfig(distributionConfigToBe)
                    .build());
            logger.info("done");
        }
    }

    // TODO enable it after more stable
    private void s3BucketStrictControl(List<String> bucketNames) throws Exception {

        List<String> bucketOnAccount = s3Client.listBuckets(ListBucketsRequest.builder().build()).buckets().stream()
                .map(it -> it.name()).collect(Collectors.toList());

        if (!bucketOnAccount.containsAll(bucketNames)) {
            logger.debug("Bucket names\n{}", bucketNames.stream().collect(Collectors.joining("\n")));
            logger.debug("Bucket on account\n{}", bucketOnAccount.stream().collect(Collectors.joining("\n")));
            String message = "Can't find some or all of the bucket of the s3 static website config, please check your AWS config";
            logger.error(message);
            throw new Exception(message);
        }

    }
}
