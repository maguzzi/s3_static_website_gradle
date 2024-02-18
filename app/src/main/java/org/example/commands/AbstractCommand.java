package org.example.commands;

import software.amazon.awssdk.services.cloudformation.CloudFormationClient;

public class AbstractCommand {

    protected CloudFormationClient cloudFormationClient;

    protected AbstractCommand(CloudFormationClient cloudFormationClient) {
        this.cloudFormationClient = cloudFormationClient;
    }

}
