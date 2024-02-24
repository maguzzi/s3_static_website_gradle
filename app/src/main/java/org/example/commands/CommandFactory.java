package org.example.commands;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.example.App;
import org.example.commands.cloudformation.CreateStackCommand;
import org.example.commands.cloudformation.GetOutputFromStack;
import org.example.commands.cloudformation.ListStacksCommand;
import org.example.commands.cloudformation.StackParams;
import org.example.commands.s3.S3Params;
import org.example.commands.s3.UploadFileToBucketCommand;

public class CommandFactory {

    private static final String BOOTSTRAP_STACK_NAME = "s3-static-website-bootstrap-stack";
    private static final String S3_STATIC_WEBSITE_COMPILED_TEMPLATE_BUCKET = "s3-static-website-compiled-template-bucket";
    private static final String S3_STATIC_WEBSITE_ARTIFACT_BUCKET = "s3-static-website-lambda-artifact-bucket";

    public static Command createBootstrap(App app) {
        StackParams stackParams = new StackParams(app.getEnvironment(), "./src/main/resources/bootstrap/bootstrap.json", BOOTSTRAP_STACK_NAME);
        return new CreateStackCommand(app.getCloudFormationClient(),stackParams);
    }

    // TODO fix path
    public static Command createUploadLambdaNestedStackTemplateFileToBucket(App app) {
        String path = "./src/main/resources/distribution/lambda-edge.yaml";
        S3Params s3Params = new S3Params(S3_STATIC_WEBSITE_COMPILED_TEMPLATE_BUCKET+"-"+app.getEnvironment(),
        new SimpleDateFormat("YYYYMMddHHmmss").format(new Date())+"_nested_lambda_stack.template", path);
        return new UploadFileToBucketCommand(app.getS3Client(), s3Params);
    }

    public static Command createUploadLambdaNestedStackSourceCodeFileToBucket(App app) {
        String path = "./src/main/resources/distribution/index.mjs";
        S3Params s3Params = new S3Params(S3_STATIC_WEBSITE_ARTIFACT_BUCKET+"-"+app.getEnvironment(),
        new SimpleDateFormat("YYYYMMddHHmmss").format(new Date())+"_index.mjs", path);
        return new UploadFileToBucketCommand(app.getS3Client(), s3Params);
    }

    public static Command createGetOutputFromBootstrapStack(App app) {
        return new GetOutputFromStack(app.getCloudFormationClient(),BOOTSTRAP_STACK_NAME+"-"+app.getEnvironment());
    }

    public static Command createList(App app) {
        return new ListStacksCommand(app.getCloudFormationClient());    
    }

    
}
