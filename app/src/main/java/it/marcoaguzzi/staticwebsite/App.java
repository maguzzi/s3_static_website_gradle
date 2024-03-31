package it.marcoaguzzi.staticwebsite;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.s3.S3Client;
import org.slf4j.LoggerFactory;

import it.marcoaguzzi.staticwebsite.commands.Command;
import it.marcoaguzzi.staticwebsite.commands.CommandFactory;
import it.marcoaguzzi.staticwebsite.commands.cloudformation.GetRoute53InfoCommand;
import it.marcoaguzzi.staticwebsite.commands.cloudformation.OutputEntry;
import it.marcoaguzzi.staticwebsite.commands.misc.ZipArtifactCommand;
import it.marcoaguzzi.staticwebsite.commands.s3.S3Params;
import it.marcoaguzzi.staticwebsite.commands.s3.UploadFileToBucketCommand;

import static it.marcoaguzzi.staticwebsite.commands.cloudformation.CreateDistributionStackCommand.*;
import static it.marcoaguzzi.staticwebsite.commands.misc.PackageTemplateCommand.PACKAGED_TEMPLATE_PATH;
import static it.marcoaguzzi.staticwebsite.commands.misc.PackageTemplateCommand.S3_PATH_TO_REPLACE;
import static it.marcoaguzzi.staticwebsite.commands.s3.UploadFileToBucketCommand.REMOTE_FILE_URL;
import static it.marcoaguzzi.staticwebsite.commands.s3.UploadFileToBucketCommand.S3_PARAMS;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
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

    public static final String S3_STATIC_WEBSITE_BUCKET = "s3-static-website-content";

    public static final String COMPILED_TEMPLATE_BUCKET_KEY = "CompiledTemplateBucket";
    public static final String ARTIFACT_S3_BUCKET = "ArtifactS3Bucket";
    public static final String ZIP_DATE = "ZipDate";

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    private CloudFormationClient cloudFormationClient;
    private S3Client s3Client;

    private static String pseudoRandomTimestampString;
    private static String zipDate;
    private static StaticWebsiteInfo staticWebsiteInfo;


    public static String getEnvironment() {
        return staticWebsiteInfo.getEnvironment();
    }

    public static String getWebsiteName() {
        return staticWebsiteInfo.getWebsiteName();
    }

    public App(String[] args, CloudFormationClient cloudFormationClient, S3Client s3Client) throws Exception {
        if (args == null || args.length != 1) {
            String message = String.format("%s - Command is required", args != null ? Arrays.asList(args) : "");
            logger.error(message);
            throw new Exception(message);
        }

        staticWebsiteInfo = StaticWebsiteInfo.fromWebsiteProperty();
        setupPseudoRandomTimestampString();

        this.cloudFormationClient = cloudFormationClient;
        this.s3Client = s3Client;

        String command = args[0];
        
        logger.info("Command: {} environment: {}", command, staticWebsiteInfo.getEnvironment());

        Command listStacksCommand = CommandFactory.createList(this);
        Command bootstrapStackCommand = CommandFactory.createBootstrapStack(this);
        Command getOutputFromBootstrapStack = CommandFactory.createGetOutputFromBootstrapStack(this);
        Command compileTemplateCommand = CommandFactory.createPackageTemplateCommand(this);
        Command zipArtifactCommand = CommandFactory.createZipArtifactCommand(this);
        Command getRoute53InfoCommand = CommandFactory.createGetRoute53InfoCommand(this);

        switch (command) {

            case "LIST": {
                listStacksCommand.execute();
                break;
            }

            case "DNS_INFO": {
                Map<String,Object> inputs = new HashMap<>();
                inputs.put(GetRoute53InfoCommand.STACK_NAME,"s3-static-website-distribution-stack-dev");
                getRoute53InfoCommand.setInputs(inputs);
                getRoute53InfoCommand.execute();
                break;
            }

            case "DISTRIBUTION": {

                bootstrapStackCommand.execute();

                Map<String, OutputEntry> outputMap = new HashMap<>();

                outputMap.putAll(getOutputFromBootstrapStack.execute());
                outputMapToString(outputMap);

                outputMap.putAll(zipArtifactCommand.execute());
                outputMapToString(outputMap);

                outputMap.putAll(uploadLambdaCode(outputMap));
                outputMapToString(outputMap);

                outputMap.putAll(uploadLambdaTemplate(outputMap));
                outputMapToString(outputMap);

                HashMap<String, Object> compileTemplateInputs = new HashMap<String, Object>();
                compileTemplateInputs.put(S3_PATH_TO_REPLACE, outputMap.get(REMOTE_FILE_URL));
                compileTemplateCommand.setInputs(compileTemplateInputs);
                outputMap.putAll(compileTemplateCommand.execute());
                outputMapToString(outputMap);
                
                Map<String, Object> distributionInput = new HashMap<String, Object>();
                distributionInput.put(DOMAIN_NAME_PARAMETER, staticWebsiteInfo.getWebsiteDomain());
                distributionInput.put(ALTERNATIVE_DOMAIN_NAME_PARAMETER,
                        staticWebsiteInfo.getWebsiteAlternativeDomain());
                distributionInput.put(S3_BUCKET_FULL_NAME_PARAMETER,
                        String.format("%s-%s-%s", S3_STATIC_WEBSITE_BUCKET, staticWebsiteInfo.getEnvironment() ,pseudoRandomTimestampString));
                distributionInput.put(BOOTSTRAP_ARTIFACT_S3_BUCKET_NAME_EXPORT_NAME,
                        outputMap.get(ARTIFACT_S3_BUCKET).getExportName());
                distributionInput.put(ZIP_DATE, zipDate);
                distributionInput.put(PACKAGED_TEMPLATE_PATH,outputMap.get(PACKAGED_TEMPLATE_PATH));

                Command distributionStackCommand = CommandFactory.createDistributionStack(this,outputMap);
                distributionStackCommand.setInputs(distributionInput);
                inputMapToString(distributionInput);
                distributionStackCommand.execute();
                break;

            }

        }
    }

    private Map<String, OutputEntry> uploadLambdaCode(Map<String, OutputEntry> previousOutput) throws Exception {
        Command uploadLambdaNestedStackSourceCodeFileToBucket = new UploadFileToBucketCommand(s3Client);
        HashMap<String, Object> inputs = new HashMap<String, Object>();
        File outputPath = new File(previousOutput.get(ZipArtifactCommand.ARTIFACT_COMPRESSED_PATH).getValue());
        S3Params s3Params = new S3Params(previousOutput.get(ARTIFACT_S3_BUCKET).getValue(), outputPath.getName(),
                outputPath.getAbsolutePath());
        inputs.put(S3_PARAMS, s3Params);
        uploadLambdaNestedStackSourceCodeFileToBucket.setInputs(inputs);
        return uploadLambdaNestedStackSourceCodeFileToBucket.execute();
    }

    private Map<String, OutputEntry> uploadLambdaTemplate(Map<String, OutputEntry> previousOutput) throws Exception {
        String path = "./distribution/lambda-edge/lambda-edge.yaml";
        Command uploadLambdaNestedStackTemplateFileToBucket = new UploadFileToBucketCommand(s3Client);
        HashMap<String, Object> inputs = new HashMap<String, Object>();
        S3Params s3Params = new S3Params(previousOutput.get(COMPILED_TEMPLATE_BUCKET_KEY).getValue(),
                new SimpleDateFormat("YYYYMMddHHmmss").format(new Date()) + "_nested_lambda_stack.template", path);
        inputs.put(S3_PARAMS, s3Params);
        uploadLambdaNestedStackTemplateFileToBucket.setInputs(inputs);
        return uploadLambdaNestedStackTemplateFileToBucket.execute();
    }

    private void setupPseudoRandomTimestampString() throws Exception {
        Path websitesetupPath = Paths.get("./.websitesetup");
        if (!Files.exists(websitesetupPath)) {
            logger.warn("websitesetup file does not exists. Creating");
            Files.createFile(websitesetupPath);
        }
        Properties propertiesFile = Utils.readPropertiesFile(websitesetupPath);
        String oldPRTS = propertiesFile.getProperty(App.PSEUDO_RANDOM_TIMESTAMP_STRING_KEY, "");
        String oldZipDate = propertiesFile.getProperty(App.ZIP_DATE, "");
        if ("".equals(oldPRTS)) {
            pseudoRandomTimestampString = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS").format(LocalDateTime.now());
            zipDate = pseudoRandomTimestampString.substring(0,8);
            propertiesFile.setProperty(App.PSEUDO_RANDOM_TIMESTAMP_STRING_KEY, pseudoRandomTimestampString);
            propertiesFile.setProperty(App.ZIP_DATE, zipDate);
            OutputStream newOutputStream = Files.newOutputStream(websitesetupPath);
            propertiesFile.store(newOutputStream, "Automatically generated. Do not edit!");
            newOutputStream.close();
            logger.info("Setup new pseudoRandomTimestampString to " + pseudoRandomTimestampString);
            logger.info("Setup new zipDate to " + zipDate);
        } else {
            pseudoRandomTimestampString = oldPRTS;
            zipDate = oldZipDate;
            logger.info("PseudoRandomTimestampString already set to " + pseudoRandomTimestampString);
            logger.info("ZipDate already set to " + zipDate);

        }
    }

    private void outputMapToString(Map<String, OutputEntry> result) {
        result.entrySet().forEach(
                e -> logger.info("{} -> {} ({})", e.getKey(), e.getValue().getValue(), e.getValue().getExportName()));
    }

    private void inputMapToString(Map<String, Object> map) {
        map.entrySet().forEach(
                e -> logger.info("{} -> {}", e.getKey(), e.getValue()));
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
