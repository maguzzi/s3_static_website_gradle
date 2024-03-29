package it.marcoaguzzi.staticwebsite.commands;

import static it.marcoaguzzi.staticwebsite.commands.misc.PackageTemplateCommand.PACKAGED_TEMPLATE_PATH;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import it.marcoaguzzi.staticwebsite.App;
import it.marcoaguzzi.staticwebsite.commands.cloudformation.CreateDistributionStackCommand;
import it.marcoaguzzi.staticwebsite.commands.cloudformation.CreateStackCommand;
import it.marcoaguzzi.staticwebsite.commands.cloudformation.GetOutputFromStack;
import it.marcoaguzzi.staticwebsite.commands.cloudformation.ListStacksCommand;
import it.marcoaguzzi.staticwebsite.commands.cloudformation.OutputEntry;
import it.marcoaguzzi.staticwebsite.commands.cloudformation.StackInfo;
import it.marcoaguzzi.staticwebsite.commands.misc.PackageTemplateCommand;
import it.marcoaguzzi.staticwebsite.commands.misc.ZipArtifactCommand;
import it.marcoaguzzi.staticwebsite.commands.s3.UploadFileToBucketCommand;
import software.amazon.awssdk.services.cloudformation.model.Capability;

public class CommandFactory {

    private static final String BOOTSTRAP_STACK_NAME = "s3-static-website-bootstrap-stack";
    private static final String DISTRIBUTION_STACK_NAME = "s3-static-website-distribution-stack";

    public static Command createBootstrapStack(App app) {
        StackInfo stackInfo = StackInfo
                .builder()
                .environmentString(App.getEnvironment())
                .templatePath("./bootstrap/bootstrap.json")
                .stackName(BOOTSTRAP_STACK_NAME)
                .psedoRandomTimestampString(App.getPsedoRandomTimestampString())
                .build();
        CreateStackCommand createBootstrapStackCommand = new CreateStackCommand(app.getCloudFormationClient(), stackInfo);
        createBootstrapStackCommand.setInputs(null);
        return createBootstrapStackCommand;
    }

    public static Command createDistributionStack(App app,Map<String,OutputEntry> previousOutputs) {
        StackInfo stackParams = StackInfo
        .builder().environmentString(App.getEnvironment())
                .templatePath(previousOutputs.get(PACKAGED_TEMPLATE_PATH).getValue())
                .stackName(DISTRIBUTION_STACK_NAME)
                .build();
        CreateStackCommand createDistributionStackCommand = new CreateDistributionStackCommand(app.getCloudFormationClient(), stackParams);
        createDistributionStackCommand.addCapability(Capability.CAPABILITY_NAMED_IAM);
        return createDistributionStackCommand;
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
