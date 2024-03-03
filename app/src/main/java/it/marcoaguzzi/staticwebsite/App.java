package it.marcoaguzzi.staticwebsite;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.s3.S3Client;

import org.slf4j.LoggerFactory;

import it.marcoaguzzi.staticwebsite.commands.Command;
import it.marcoaguzzi.staticwebsite.commands.CommandFactory;
import it.marcoaguzzi.staticwebsite.commands.CommandUtil;
import it.marcoaguzzi.staticwebsite.commands.cloudformation.OutputEntry;
import it.marcoaguzzi.staticwebsite.commands.misc.ZipArtifactCommand;
import it.marcoaguzzi.staticwebsite.commands.s3.S3Params;

import static it.marcoaguzzi.staticwebsite.commands.CommandUtil.*;
import static it.marcoaguzzi.staticwebsite.commands.cloudformation.CreateDistributionStackCommand.*;
import static it.marcoaguzzi.staticwebsite.commands.misc.PackageTemplateCommand.S3_PATH_TO_REPLACE;
import static it.marcoaguzzi.staticwebsite.commands.s3.UploadFileToBucketCommand.REMOTE_FILE_URL;
import static it.marcoaguzzi.staticwebsite.commands.s3.UploadFileToBucketCommand.S3_PARAMS;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;

public class App {

    private static final Region region = Region.US_EAST_1;

    public static final String ENVIRONMENT_PARAMETER_KEY = "Environment";
    public static final String WEBSITE_NAME_PARAMETER_KEY = "WebsiteName";

    public static final String S3_STATIC_WEBSITE_ENVIRONMENT_TAG = "s3_static_website_environment";
    public static final String S3_STATIC_WEBSITE_TAG = "s3_static_website";

    public static final String S3_STATIC_WEBSITE_BUCKET = "s3-static-website";

    public static final String COMPILED_TEMPLATE_BUCKET_KEY = "CompiledTemplateBucket";

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    private CloudFormationClient cloudFormationClient;
    private S3Client s3Client;

    private String environment;

    public App(String[] args, CloudFormationClient cloudFormationClient, S3Client s3Client) throws Exception {
        if (args == null || args.length != 2) {
            logger.error("Command is required");
            throw new Exception("Command is required");
        }

        this.cloudFormationClient = cloudFormationClient;
        this.s3Client = s3Client;

        String command = args[0];
        environment = args[1];

        logger.info("Command: {} environment: {}", command, environment);

        Command listStacksCommand = CommandFactory.createList(this);
        Command bootstrapStackCommand = CommandFactory.createBootstrapStack(this);
        Command distributionStackCommand = CommandFactory.createDistributionStack(this);
        Command getOutputFromStack = CommandFactory.createGetOutputFromBootstrapStack(this);
        Command uploadLambdaNestedStackTemplateFileToBucket = CommandFactory
                .createUploadLambdaNestedStackTemplateFileToBucket(this);
        Command uploadLambdaNestedStackSourceCodeFileToBucket = CommandFactory
                .createUploadLambdaNestedStackSourceCodeFileToBucket(this);
        Command compileTemplateCommand = CommandFactory.createPackageTemplateCommand(this);
        Command zipArtifactCommand = CommandFactory.createZipArtifactCommand(this);

        switch (command) {
            case "LIST": {
                listStacksCommand.execute();
                break;
            }
            case "BOOTSTRAP": {
                bootstrapStackCommand.execute();
                break;
            }
            case "DISTRIBUTION": {
                Map<String, OutputEntry> outputFromStackResult = getOutputFromStack.execute();
                mapToString(outputFromStackResult);

                Map<String, OutputEntry> outputFromZipArtifactCommand = zipArtifactCommand.execute();
                mapToString(outputFromZipArtifactCommand);

                HashMap<String, Object> inputs = new HashMap<String, Object>();
                File outputPath = new File(
                        outputFromZipArtifactCommand.get(ZipArtifactCommand.ARTIFACT_COMPRESSED_PATH).getValue());
                S3Params s3Params = new S3Params(
                        CommandFactory.S3_STATIC_WEBSITE_ARTIFACT_BUCKET + "-" + this.getEnvironment(),
                        outputPath.getName(),
                        outputPath.getAbsolutePath());
                inputs.put(S3_PARAMS, s3Params);
                uploadLambdaNestedStackSourceCodeFileToBucket.setInputs(inputs);
                Map<String, OutputEntry> uploadLambdaArtifactResult = uploadLambdaNestedStackSourceCodeFileToBucket
                        .execute();
                mapToString(uploadLambdaArtifactResult);

                Map<String, OutputEntry> uploadLambdaTemplateResult = uploadLambdaNestedStackTemplateFileToBucket
                        .execute();
                mapToString(uploadLambdaTemplateResult);

                HashMap<String, Object> compileTemplateInputs = new HashMap<String, Object>();
                compileTemplateInputs.put(S3_PATH_TO_REPLACE, uploadLambdaTemplateResult.get(REMOTE_FILE_URL));
                compileTemplateCommand.setInputs(compileTemplateInputs);
                Map<String, OutputEntry> result4 = compileTemplateCommand.execute();
                mapToString(result4);

                Map<String, Object> distributionInput = new HashMap<String, Object>();
                distributionInput.put(DOMAIN_NAME_PARAMETER, "dev.marcoaguzzi.cloudns.ph");
                distributionInput.put(ALTERNATIVE_DOMAIN_NAME_PARAMETER, "");
                distributionInput.put(S3_BUCKET_NAME_PARAMETER,
                        String.format("%s-%s-%s", S3_STATIC_WEBSITE_BUCKET, CommandUtil.dateToSecond(), environment));
                distributionInput.put(BOOTSTRAP_ARTIFACT_S3_BUCKET_NAME_EXPORT_NAME,
                        outputFromStackResult.get(COMPILED_TEMPLATE_BUCKET_KEY).getExportName());
                distributionInput.put(ZIP_DATE, dateToDay());
                distributionStackCommand.setInputs(distributionInput);
                distributionStackCommand.execute();
                break;

            }

        }
    }

    private void mapToString(Map<String, OutputEntry> result) {
        result.entrySet().forEach(
                e -> logger.info("{} -> {} ({})", e.getKey(), e.getValue().getValue(), e.getValue().getExportName()));
    }

    public static void main(String[] args) {
        try {
            new App(args, CloudFormationClient.builder().region(region).build(), S3Client.create());
        } catch (Exception e) {
            screenMessage("*** ERROR *** " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String getEnvironment() {
        return this.environment;
    }

    public CloudFormationClient getCloudFormationClient() {
        return this.cloudFormationClient;
    }

    public S3Client getS3Client() {
        return this.s3Client;
    }

    public static final void screenMessage(String message) {
        logger.info("");
        logger.info(" -- {} --", message);
        logger.info("");
    }

    // TODO cache
    public static String getWebsiteName() {
        File f = new File("./website.properties");
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(f));
            return properties.getProperty("name");
        } catch (Exception e) {
            logger.error("Can't read property file {}", f.getAbsolutePath(), e);
            throw new RuntimeException(e);
        }
    }
}
