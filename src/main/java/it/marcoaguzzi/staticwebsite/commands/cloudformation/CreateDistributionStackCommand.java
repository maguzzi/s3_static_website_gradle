package it.marcoaguzzi.staticwebsite.commands.cloudformation;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.marcoaguzzi.staticwebsite.App;
import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.cloudformation.model.Parameter;


public class CreateDistributionStackCommand extends CreateStackCommand {

    public static final String DOMAIN_NAME_PARAMETER = "DomainNameParameter";
    public static final String S3_BUCKET_FULL_NAME_PARAMETER = "S3BucketFullNameParameter";
    public static final String ALTERNATIVE_DOMAIN_NAME_PARAMETER = "AlternativeDomainNameParameter";
    public static final String BOOTSTRAP_ARTIFACT_S3_BUCKET_NAME_EXPORT_NAME = "BootstrapArtifactS3BucketNameExportName";

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public CreateDistributionStackCommand(CloudFormationClient cloudFormationClient,StackInfo stackInfo) {
        super(cloudFormationClient,stackInfo);
    }

    public void setInputs(Map<String,Object> inputs){
        parameters.add(Parameter.builder().parameterKey(App.ENVIRONMENT_PARAMETER_KEY).parameterValue(stackInfo.getEnvironmentString()).build());
        convertInputEntryToParameter(inputs,BOOTSTRAP_ARTIFACT_S3_BUCKET_NAME_EXPORT_NAME);
        convertInputEntryToParameter(inputs,ALTERNATIVE_DOMAIN_NAME_PARAMETER);
        convertInputEntryToParameter(inputs,S3_BUCKET_FULL_NAME_PARAMETER);
        convertInputEntryToParameter(inputs,DOMAIN_NAME_PARAMETER);
        convertInputEntryToParameter(inputs,App.ZIP_DATE);
    }

    private void convertInputEntryToParameter(Map<String, Object> inputs,String key) {
        logger.debug("searching for {} ==> {}",key,inputs.get(key));
        parameters.add(Parameter.builder().parameterKey(key).parameterValue(inputs.get(key).toString()).build());
    }

}
