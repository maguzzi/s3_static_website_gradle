package it.marcoaguzzi.staticwebsite.commands.cloudformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.marcoaguzzi.staticwebsite.App;
import it.marcoaguzzi.staticwebsite.commands.Command;
import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.cloudformation.model.CreateStackRequest;
import software.amazon.awssdk.services.cloudformation.model.Parameter;
import software.amazon.awssdk.services.cloudformation.model.Tag;

public class CreateStackCommand implements Command {

    private static final Logger logger = LoggerFactory.getLogger(CreateStackCommand.class);

    private final CloudFormationClient cloudFormationClient;
    protected StackInfo stackInfo;
    protected List<Parameter> parameters;

    public CreateStackCommand(CloudFormationClient cloudFormationClient, StackInfo stackInfo) {
        this.cloudFormationClient = cloudFormationClient;
        this.parameters = new ArrayList<Parameter>();
        parameters.add(parameter(App.ENVIRONMENT_PARAMETER_KEY, stackInfo.getEnvironmentString()));
        parameters.add(parameter(App.WEBSITE_NAME_PARAMETER_KEY, stackInfo.getWebsiteName()));
        parameters.add(parameter(App.PSEUDO_RANDOM_TIMESTAMP_STRING_KEY, stackInfo.getPsedoRandomTimestampString()));
        this.stackInfo = stackInfo;
    }

    private Parameter parameter(String key, String value) {
        return Parameter
                .builder()
                .parameterKey(key)
                .parameterValue(value)
                .build();
    }

    @Override
    public Map<String, OutputEntry> execute() throws Exception {
        App.screenMessage(String.format("%s - %s CREATION START", stackInfo.getStackName(),
                stackInfo.getEnvironmentString()));

        String templateBody = Utils.readFileContent(stackInfo.getTemplatePath());

        String stackFullName = stackInfo.getStackName() + "-" + stackInfo.getEnvironmentString();

        logger.debug("parameters: {}",parameters);

        CreateStackRequest request = CreateStackRequest.builder()
                .stackName(stackFullName)
                .templateBody(templateBody)
                .parameters(parameters)
                .tags(
                        Tag.builder().key(App.S3_STATIC_WEBSITE_TAG)
                                .value(stackInfo.getWebsiteName()).build(),
                        Tag.builder().key(App.S3_STATIC_WEBSITE_ENVIRONMENT_TAG)
                                .value(stackInfo.getEnvironmentString()).build(),
                        Tag.builder().key(App.S3_STATIC_WEBSITE_TIMESTAMP_TAG)
                                .value(stackInfo.getPsedoRandomTimestampString()).build())
                .build();

        cloudFormationClient.createStack(request);

        StackCompleteChecker stackCompleteChecker = new StackCompleteChecker(cloudFormationClient, stackFullName);
        stackCompleteChecker.check(new Function<String, Void>() {
            @Override
            public Void apply(String stackId) {
                logger.info("Stack creation for stack id {} terminated.", stackId);
                return null;
            }
        });

        App.screenMessage(
                String.format("%s - %s CREATION END", stackInfo.getStackName(), stackInfo.getEnvironmentString()));

        Map<String, OutputEntry> result = new HashMap<>();

        return result;
    }

}
