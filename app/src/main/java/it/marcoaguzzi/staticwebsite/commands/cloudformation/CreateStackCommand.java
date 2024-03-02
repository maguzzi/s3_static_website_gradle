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
import it.marcoaguzzi.staticwebsite.commands.CommandUtil;
import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.cloudformation.model.CreateStackRequest;
import software.amazon.awssdk.services.cloudformation.model.Parameter;
import software.amazon.awssdk.services.cloudformation.model.Tag;

public class CreateStackCommand implements Command {

    private static final Logger logger = LoggerFactory.getLogger(CreateStackCommand.class);

    private final CloudFormationClient cloudFormationClient;
    protected StackInfo stackParams;
    protected List<Parameter> parameters;
    
    public CreateStackCommand(CloudFormationClient cloudFormationClient,StackInfo stackParams) {
        this.cloudFormationClient = cloudFormationClient;
        this.parameters = new ArrayList<Parameter>();
        this.stackParams = stackParams;
    }

    public void setInputs(Map<String,Object> inputs){
        Parameter environment = Parameter
            .builder()
            .parameterKey(App.ENVIRONMENT_PARAMETER_KEY)
            .parameterValue(stackParams.getEnvironmentString())
            .build();

        parameters.add(environment);
    }

    @Override
    public Map<String,OutputEntry> execute() throws Exception {
        App.screenMessage(String.format("%s - %s CREATION START",stackParams.getStackName(),stackParams.getEnvironmentString()));

        String templateBody = CommandUtil.readFileContent(stackParams.getTemplatePath());

        String stackFullName = stackParams.getStackName()+"-"+stackParams.getEnvironmentString();

        CreateStackRequest request = CreateStackRequest.builder()
            .stackName(stackFullName)
            .templateBody(templateBody)
            .parameters(parameters)
            .tags(Tag.builder().key(App.S3_STATIC_WEBSITE_ENVIRONMENT_TAG).value(stackParams.getEnvironmentString()).build())
            .build();

        cloudFormationClient.createStack(request);

        StackCompleteChecker stackCompleteChecker = new StackCompleteChecker(cloudFormationClient, stackFullName);
        stackCompleteChecker.check(new Function<String,Void>() {
            @Override
            public Void apply(String stackId) {
                logger.info("Stack creation for stack id {} terminated.",stackId);
                return null;
            }
        });

        App.screenMessage(String.format("%s - %s CREATION END",stackParams.getStackName(),stackParams.getEnvironmentString()));

        Map<String,OutputEntry> result = new HashMap<>();

        return result;
    }

}
