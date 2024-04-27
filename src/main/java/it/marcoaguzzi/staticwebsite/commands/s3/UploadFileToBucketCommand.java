package it.marcoaguzzi.staticwebsite.commands.s3;

import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.marcoaguzzi.staticwebsite.App;
import it.marcoaguzzi.staticwebsite.Utils;
import it.marcoaguzzi.staticwebsite.commands.Command;
import it.marcoaguzzi.staticwebsite.commands.cloudformation.OutputEntry;

import java.util.HashMap;
import java.util.Map;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

public class UploadFileToBucketCommand implements Command {

    private static final Logger logger = LoggerFactory.getLogger(UploadFileToBucketCommand.class);

    public static final String REMOTE_FILE_URL = "REMOTE_FILE_URL";
    public static final String S3_PARAMS = "S3_PARAMS";

    private S3Client s3Client;
    private S3Params s3Params;

    public UploadFileToBucketCommand(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public void setInputs(Map<String, Object> inputs) {
        this.s3Params = (S3Params) inputs.get(S3_PARAMS);
    }

    @Override
    public Map<String,OutputEntry> execute() throws Exception {
        App.screenMessage("UPLOAD FILE TO BUCKET START");
        logger.info("{} --> {}/{}",s3Params.getInputPath(),s3Params.getS3Bucket(),s3Params.getS3Key());
        
        PutObjectResponse putObjectResponse = s3Client.putObject(PutObjectRequest.builder()
        .bucket(s3Params.getS3Bucket())
        .key(s3Params.getS3Key())
        .build(),
        RequestBody.fromString(s3Params.getInputPath().startsWith("/")?Utils.readFileContentFromFile(s3Params.getInputPath()):Utils.readFileContentFromJar(s3Params.getInputPath())));
        logger.debug(putObjectResponse.toString());
        
        URL url = s3Client.utilities().getUrl(GetUrlRequest.builder().bucket(s3Params.getS3Bucket()).key(s3Params.getS3Key()).build());

        logger.info("URL: {}",url);

        Map<String,OutputEntry> outputMap = new HashMap<>();
        outputMap.put(REMOTE_FILE_URL, new OutputEntry(REMOTE_FILE_URL,url.toString()));
        App.screenMessage("UPLOAD FILE TO BUCKET END");
        return outputMap;
    }

}