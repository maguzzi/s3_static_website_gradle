package it.marcoaguzzi.staticwebsite.commands;

import static it.marcoaguzzi.staticwebsite.commands.s3.UploadFileToBucketCommand.S3_PARAMS;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import it.marcoaguzzi.staticwebsite.App;
import it.marcoaguzzi.staticwebsite.commands.cloudformation.CreateDistributionStackCommand;
import it.marcoaguzzi.staticwebsite.commands.cloudformation.CreateStackCommand;
import it.marcoaguzzi.staticwebsite.commands.cloudformation.GetOutputFromStack;
import it.marcoaguzzi.staticwebsite.commands.cloudformation.ListStacksCommand;
import it.marcoaguzzi.staticwebsite.commands.cloudformation.StackInfo;
import it.marcoaguzzi.staticwebsite.commands.misc.PackageTemplateCommand;
import it.marcoaguzzi.staticwebsite.commands.misc.ZipArtifactCommand;
import it.marcoaguzzi.staticwebsite.commands.s3.S3Params;
import it.marcoaguzzi.staticwebsite.commands.s3.UploadFileToBucketCommand;

public class CommandFactory {

    private static final String BOOTSTRAP_STACK_NAME = "s3-static-website-bootstrap-stack";
    private static final String DISTRIBUTION_STACK_NAME = "s3-static-website-distribution-stack";

    // TODO refactor fixed s3 bucket / keys
    private static final String S3_STATIC_WEBSITE_COMPILED_TEMPLATE_BUCKET = "s3-static-website-compiled-template-bucket";
    // TODO refactor fixed s3 bucket / keys
    public static final String S3_STATIC_WEBSITE_ARTIFACT_BUCKET = "s3-static-website-lambda-artifact-bucket";

    public static Command createBootstrapStack(App app) {
        StackInfo stackInfo = StackInfo
                .builder()
                .environmentString(App.getEnvironment())
                .templatePath("./src/main/resources/bootstrap/bootstrap.json")
                .stackName(BOOTSTRAP_STACK_NAME)
                .psedoRandomTimestampString(App.getPsedoRandomTimestampString())
                .build();
        CreateStackCommand createBootstrapStackCommand = new CreateStackCommand(app.getCloudFormationClient(), stackInfo);
        createBootstrapStackCommand.setInputs(null);
        return createBootstrapStackCommand;
    }

    public static Command createDistributionStack(App app) {
        StackInfo stackParams = StackInfo
        .builder().environmentString(App.getEnvironment())
                .templatePath("./src/main/resources/distribution/website-distribution.json")
                .stackName(DISTRIBUTION_STACK_NAME)
                .build();
        return new CreateDistributionStackCommand(app.getCloudFormationClient(), stackParams);
    }

    // TODO fix path
    public static Command createUploadLambdaNestedStackTemplateFileToBucket(App app) {
        String path = "./src/main/resources/distribution/lambda-edge/lambda-edge.yaml";
        S3Params s3Params = new S3Params(S3_STATIC_WEBSITE_COMPILED_TEMPLATE_BUCKET + "-" + App.getEnvironment(),
                new SimpleDateFormat("YYYYMMddHHmmss").format(new Date()) + "_nested_lambda_stack.template", path);
        UploadFileToBucketCommand uploadFileToBucketCommand = new UploadFileToBucketCommand(app.getS3Client());
        Map<String, Object> inputs = new HashMap<String, Object>();
        inputs.put(S3_PARAMS, s3Params);
        uploadFileToBucketCommand.setInputs(inputs);
        return uploadFileToBucketCommand;
    }

    public static Command createZipArtifactCommand(App app) throws Exception {
        String sourcePath = "./src/main/resources/distribution/lambda-edge/index.mjs";
        String zipFile = String.format("lambda-edge-%s-%s.zip", App.getEnvironment(),
                new SimpleDateFormat("yyyyMMdd").format(new Date()));
        return new ZipArtifactCommand(sourcePath, zipFile);
    }

    public static Command createUploadLambdaNestedStackSourceCodeFileToBucket(App app) throws Exception {
        return new UploadFileToBucketCommand(app.getS3Client());
    }

    public static Command createGetOutputFromBootstrapStack(App app) {
        return new GetOutputFromStack(app.getCloudFormationClient(), BOOTSTRAP_STACK_NAME + "-" + App.getEnvironment());
    }

    public static Command createList(App app) {
        return new ListStacksCommand(app.getCloudFormationClient());
    }

    public static Command createPackageTemplateCommand(App app) {
        return new PackageTemplateCommand("./src/main/resources/distribution/website-distribution.json");
    }
}
