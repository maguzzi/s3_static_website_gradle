package org.example.commands.s3;

import java.nio.file.Paths;

import org.example.App;
import org.example.commands.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

public class UploadFileToBucketCommand implements Command {

    private static final Logger logger = LoggerFactory.getLogger(UploadFileToBucketCommand.class);

    private static final String REMOTE_FILE_PATH="REMOTE_FILE_PATH";

    private S3Client s3Client;
    private S3Params s3Params;

    public UploadFileToBucketCommand(S3Client s3Client,S3Params s3Params) {
        this.s3Client = s3Client;
        this.s3Params = s3Params;
    }

    @Override
    public Map<String,String> execute() throws Exception {
        App.screenMessage("UPLOAD FILE TO BUCKET START");
        logger.debug("{} --> {}/{}",s3Params.getInputPath(),s3Params.getS3Bucket(),s3Params.getS3Key());
        PutObjectResponse putObjectResponse = s3Client.putObject(PutObjectRequest.builder()
        .bucket(s3Params.getS3Bucket())
        .key(s3Params.getS3Key())
        .build(),
        RequestBody.fromFile(Paths.get(s3Params.getInputPath())));
        logger.trace(putObjectResponse.toString());
        App.screenMessage("UPLOAD FILE TO BUCKET END");
        Map<String,String> outputMap = new HashMap<String,String>();
        outputMap.put(REMOTE_FILE_PATH,s3Params.getS3Bucket()+"/"+s3Params.getS3Key());
        return outputMap;
    }

}