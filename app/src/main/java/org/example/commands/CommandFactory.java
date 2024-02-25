package org.example.commands;

import static org.example.commands.s3.UploadFileToBucketCommand.S3_PARAMS;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.example.App;
import org.example.commands.cloudformation.CreateStackCommand;
import org.example.commands.cloudformation.GetOutputFromStack;
import org.example.commands.cloudformation.ListStacksCommand;
import org.example.commands.cloudformation.StackParams;
import org.example.commands.misc.PackageTemplateCommand;
import org.example.commands.misc.ZipArtifactCommand;
import org.example.commands.s3.S3Params;
import org.example.commands.s3.UploadFileToBucketCommand;

public class CommandFactory {

    private static final String BOOTSTRAP_STACK_NAME = "s3-static-website-bootstrap-stack";
    // TODO refactor fixed s3 bucket / keys
    private static final String S3_STATIC_WEBSITE_COMPILED_TEMPLATE_BUCKET = "s3-static-website-compiled-template-bucket";
    // TODO refactor fixed s3 bucket / keys
    public static final String S3_STATIC_WEBSITE_ARTIFACT_BUCKET = "s3-static-website-lambda-artifact-bucket";

    public static Command createBootstrap(App app) {
        StackParams stackParams = new StackParams(app.getEnvironment(), "./src/main/resources/bootstrap/bootstrap.json", BOOTSTRAP_STACK_NAME);
        return new CreateStackCommand(app.getCloudFormationClient(),stackParams);
    }

    // TODO fix path
    public static Command createUploadLambdaNestedStackTemplateFileToBucket(App app) {
        String path = "./src/main/resources/distribution/lambda-edge/lambda-edge.yaml";
        S3Params s3Params = new S3Params(S3_STATIC_WEBSITE_COMPILED_TEMPLATE_BUCKET+"-"+app.getEnvironment(),
        new SimpleDateFormat("YYYYMMddHHmmss").format(new Date())+"_nested_lambda_stack.template", path);
        UploadFileToBucketCommand uploadFileToBucketCommand = new UploadFileToBucketCommand(app.getS3Client());
        Map<String,Object> inputs = new HashMap<String,Object>();
        inputs.put(S3_PARAMS, s3Params);
        uploadFileToBucketCommand.setInputs(inputs);
        return uploadFileToBucketCommand;
    }

    public static Command createZipArtifactCommand(App app) throws Exception {
        String sourcePath = "./src/main/resources/distribution/lambda-edge/index.mjs";
        String zipFile = String.format("lambda-edge-%s-%s.zip",app.getEnvironment(),new SimpleDateFormat("yyyyMMdd").format(new Date()));
        return new ZipArtifactCommand(sourcePath,zipFile);
    }

    public static Command createUploadLambdaNestedStackSourceCodeFileToBucket(App app) throws Exception {
        return new UploadFileToBucketCommand(app.getS3Client());
    }

    public static Command createGetOutputFromBootstrapStack(App app) {
        return new GetOutputFromStack(app.getCloudFormationClient(),BOOTSTRAP_STACK_NAME+"-"+app.getEnvironment());
    }

    public static Command createList(App app) {
        return new ListStacksCommand(app.getCloudFormationClient());    
    }

    public static Command createPackageTemplateCommand(App app) {
        return new PackageTemplateCommand("./src/main/resources/distribution/website-distribution.json");
    }
}
