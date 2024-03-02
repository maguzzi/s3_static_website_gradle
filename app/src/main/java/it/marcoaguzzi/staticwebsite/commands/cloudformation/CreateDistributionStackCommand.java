package it.marcoaguzzi.staticwebsite.commands.cloudformation;

import java.util.Map;

import it.marcoaguzzi.staticwebsite.App;
import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.cloudformation.model.Parameter;

public class CreateDistributionStackCommand extends CreateStackCommand {

    public static final String ZIP_DATE = "ZipDate";
    public static final String DOMAIN_NAME_PARAMETER = "DomainNameParameter";
    public static final String S3_BUCKET_NAME_PARAMETER = "S3BucketNameParameter";
    public static final String ALTERNATIVE_DOMAIN_NAME_PARAMETER = "AlternativeDomainNameParameter";
    public static final String BOOTSTRAP_ARTIFACT_S3_BUCKET_NAME_EXPORT_NAME = "BootstrapArtifactS3BucketNameExportName";

    public CreateDistributionStackCommand(CloudFormationClient cloudFormationClient,StackInfo stackInfo) {
        super(cloudFormationClient,stackInfo);
    }

    public void setInputs(Map<String,Object> inputs){
        parameters.add(Parameter.builder().parameterKey(App.ENVIRONMENT_PARAMETER_KEY).parameterValue(stackParams.getEnvironmentString()).build());
        convertInputEntryToParameter(inputs,BOOTSTRAP_ARTIFACT_S3_BUCKET_NAME_EXPORT_NAME);
        convertInputEntryToParameter(inputs,ALTERNATIVE_DOMAIN_NAME_PARAMETER);
        convertInputEntryToParameter(inputs,S3_BUCKET_NAME_PARAMETER);
        convertInputEntryToParameter(inputs,DOMAIN_NAME_PARAMETER);
        convertInputEntryToParameter(inputs,ZIP_DATE);
    }

    private void convertInputEntryToParameter(Map<String, Object> inputs,String key) {
        parameters.add(Parameter.builder().parameterKey(key).parameterValue(inputs.get(key).toString()).build());
    }

}
