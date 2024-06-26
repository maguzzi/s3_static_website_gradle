package it.marcoaguzzi.staticwebsite.commands.cloudformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.marcoaguzzi.staticwebsite.App;
import it.marcoaguzzi.staticwebsite.Utils;
import it.marcoaguzzi.staticwebsite.commands.Command;
import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.cloudformation.model.Capability;
import software.amazon.awssdk.services.cloudformation.model.CreateStackRequest;
import software.amazon.awssdk.services.cloudformation.model.Parameter;
import software.amazon.awssdk.services.cloudformation.model.Tag;

public class CreateStackCommand implements Command {

    private static final Logger logger = LoggerFactory.getLogger(CreateStackCommand.class);

    private final CloudFormationClient cloudFormationClient;
    protected StackInfo stackInfo;
    protected List<Parameter> parameters;
    protected List<Capability> capabilities = new ArrayList<>();
    private boolean waitForCompletion;
    private String stackFullName;

    public CreateStackCommand(CloudFormationClient cloudFormationClient, StackInfo stackInfo,
            Boolean waitForCompletion) {
        this.cloudFormationClient = cloudFormationClient;
        this.parameters = new ArrayList<Parameter>();
        parameters.add(parameter(App.ENVIRONMENT_PARAMETER_KEY, stackInfo.getEnvironmentString()));
        parameters.add(parameter(App.WEBSITE_NAME_PARAMETER_KEY, stackInfo.getWebsiteName()));
        this.stackInfo = stackInfo;
        this.waitForCompletion = waitForCompletion;
    }

    public void addCapability(Capability capability) {
        capabilities.add(capability);
    }

    protected Parameter parameter(String key, String value) {
        return Parameter
                .builder()
                .parameterKey(key)
                .parameterValue(value)
                .build();
    }

    @Override
    public void setInputs(Map<String, Object> inputs) {
        logger.debug("setInput start");
        parameters.add(parameter(App.PSEUDO_RANDOM_TIMESTAMP_STRING_KEY,
                (String) inputs.get(App.PSEUDO_RANDOM_TIMESTAMP_STRING_KEY)));
        logger.debug("setInput end");
    }

    @Override
    public Map<String, OutputEntry> execute() throws Exception {
        App.screenMessage(String.format("%s - %s CREATION START", stackInfo.getStackName(),
                stackInfo.getEnvironmentString()));

        String templateBody = Utils.readFileContent(stackInfo.getTemplatePath());

        stackFullName = stackInfo.getStackName() + "-" + stackInfo.getEnvironmentString();

        logger.debug("parameters: {}", parameters);

        // TODO what if the parameter is not there?
        CreateStackRequest request = CreateStackRequest.builder()
                .stackName(stackFullName)
                .templateBody(templateBody)
                .parameters(parameters)
                .capabilities(capabilities)
                .tags(
                        Tag.builder().key(App.S3_STATIC_WEBSITE_TAG)
                                .value(stackInfo.getWebsiteName()).build(),
                        Tag.builder().key(App.S3_STATIC_WEBSITE_ENVIRONMENT_TAG)
                                .value(stackInfo.getEnvironmentString()).build(),
                        Tag.builder().key(App.S3_STATIC_WEBSITE_TIMESTAMP_TAG)
                                .value(parameters.stream()
                                        .filter(it -> it.parameterKey().equals(App.PSEUDO_RANDOM_TIMESTAMP_STRING_KEY))
                                        .findFirst().get().parameterValue())
                                .build())
                .build();

        cloudFormationClient.createStack(request);

        if (waitForCompletion) {
            waitForCompletion(cloudFormationClient,stackFullName);
        }

        App.screenMessage(
                String.format("%s - %s CREATION END", stackInfo.getStackName(), stackInfo.getEnvironmentString()));

        // TODO put stack name here
        Map<String, OutputEntry> result = new HashMap<>(); 

        return result;
    }

    //TODO does not work outside of here!
    public static void waitForCompletion(CloudFormationClient cloudFormationClient,String stackFullName) throws Exception {
        StackCompleteChecker stackCompleteChecker = new StackCompleteChecker(cloudFormationClient, stackFullName);
            stackCompleteChecker.check(new Function<String, Void>() {
                @Override
                public Void apply(String stackId) {
                    logger.info("Stack creation for stack id {} terminated.", stackId);
                    return null;
                }
            });
    }

}
