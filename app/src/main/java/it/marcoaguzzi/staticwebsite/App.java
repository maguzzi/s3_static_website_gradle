package it.marcoaguzzi.staticwebsite;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.s3.S3Client;

import org.slf4j.LoggerFactory;

import it.marcoaguzzi.staticwebsite.commands.Command;
import it.marcoaguzzi.staticwebsite.commands.CommandFactory;
import it.marcoaguzzi.staticwebsite.commands.cloudformation.OutputEntry;
import it.marcoaguzzi.staticwebsite.commands.misc.ZipArtifactCommand;
import it.marcoaguzzi.staticwebsite.commands.s3.S3Params;

import static it.marcoaguzzi.staticwebsite.commands.cloudformation.CreateDistributionStackCommand.*;
import static it.marcoaguzzi.staticwebsite.commands.misc.PackageTemplateCommand.S3_PATH_TO_REPLACE;
import static it.marcoaguzzi.staticwebsite.commands.s3.UploadFileToBucketCommand.REMOTE_FILE_URL;
import static it.marcoaguzzi.staticwebsite.commands.s3.UploadFileToBucketCommand.S3_PARAMS;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;

public class App {

    private static final Region region = Region.US_EAST_1;

    public static final String ENVIRONMENT_PARAMETER_KEY = "Environment";
    public static final String WEBSITE_NAME_PARAMETER_KEY = "WebsiteName";
    public static final String PSEUDO_RANDOM_TIMESTAMP_STRING_KEY = "PseudoRandomTimestampString";

    public static final String S3_STATIC_WEBSITE_ENVIRONMENT_TAG = "s3_static_website_environment";
    public static final String S3_STATIC_WEBSITE_TIMESTAMP_TAG = "s3_static_website_timestamp_tag";
    public static final String S3_STATIC_WEBSITE_TAG = "s3_static_website";

    public static final String S3_STATIC_WEBSITE_BUCKET = "s3-static-website";

    public static final String COMPILED_TEMPLATE_BUCKET_KEY = "CompiledTemplateBucket";

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    private CloudFormationClient cloudFormationClient;
    private S3Client s3Client;

    private static String pseudoRandomTimestampString;
    private static StaticWebsiteInfo staticWebsiteInfo;
    
    public static String getEnvironment() {
        return staticWebsiteInfo.getEnvironment();
    }

    public static String getWebsiteName() {
        return staticWebsiteInfo.getWebsiteName();
    }

    public App(String[] args, CloudFormationClient cloudFormationClient, S3Client s3Client) throws Exception {
        if (args == null || args.length != 2) {
            String message = String.format("%s - Command is required", args != null ? Arrays.asList(args) : "");
            logger.error(message);
            throw new Exception(message);
        }

        readWebsitePropertiesFile();
        setupPseudoRandomTimestampString();

        this.cloudFormationClient = cloudFormationClient;
        this.s3Client = s3Client;

        String command = args[0];
        String environment = args[1];

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
                        CommandFactory.S3_STATIC_WEBSITE_ARTIFACT_BUCKET + "-" + App.getEnvironment(),
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
                distributionInput.put(DOMAIN_NAME_PARAMETER, staticWebsiteInfo.getWebsiteDomain());
                distributionInput.put(ALTERNATIVE_DOMAIN_NAME_PARAMETER, staticWebsiteInfo.getWebsiteAlternativeDomain());
                distributionInput.put(S3_BUCKET_NAME_PARAMETER,
                        String.format("%s-%s-%s", S3_STATIC_WEBSITE_BUCKET, Utils.dateToSecond(), environment));
                distributionInput.put(BOOTSTRAP_ARTIFACT_S3_BUCKET_NAME_EXPORT_NAME,
                        outputFromStackResult.get(COMPILED_TEMPLATE_BUCKET_KEY).getExportName());
                distributionInput.put(ZIP_DATE, dateToDay());
                distributionStackCommand.setInputs(distributionInput);
                distributionStackCommand.execute();
                break;

            }

        }
    }

  

    private void setupPseudoRandomTimestampString() throws Exception {
        Path websitesetupPath = Paths.get("./.websitesetup");
        if (!Files.exists(websitesetupPath)) {
            logger.warn("websitesetup file does not exists. Creating");
            Files.createFile(websitesetupPath);
        }
        Properties propertiesFile = Utils.readPropertiesFile(websitesetupPath);
        String property = propertiesFile.getProperty(App.PSEUDO_RANDOM_TIMESTAMP_STRING_KEY, "");
        if ("".equals(property)) {
            pseudoRandomTimestampString = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS").format(LocalDateTime.now());
            propertiesFile.setProperty(App.PSEUDO_RANDOM_TIMESTAMP_STRING_KEY, pseudoRandomTimestampString);
            OutputStream newOutputStream = Files.newOutputStream(websitesetupPath);
            propertiesFile.store(newOutputStream,"Automatically generated. Do not edit!");
            newOutputStream.close();
            logger.info("Setup new pseudoRandomTimestampString to "+pseudoRandomTimestampString);
        } else {
            pseudoRandomTimestampString = property;
            logger.info("PseudoRandomTimestampString already set to "+pseudoRandomTimestampString);
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

    public static String getPsedoRandomTimestampString() {
        return pseudoRandomTimestampString;
    }

    
}
