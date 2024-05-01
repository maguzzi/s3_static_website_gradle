package it.marcoaguzzi.staticwebsite.commands.cloudformation;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.s3.S3Client;

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
}
