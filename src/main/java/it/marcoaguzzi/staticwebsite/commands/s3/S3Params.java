package it.marcoaguzzi.staticwebsite.commands.s3;

public class S3Params {

    private String s3Bucket;
    private String s3Key;
    private String inputPath;

    public S3Params(String s3Bucket,String s3Key,String inputPath) {
        this.s3Bucket = s3Bucket;
        this.s3Key = s3Key;
        this.inputPath = inputPath;
    }

    public String getS3Bucket() {
        return this.s3Bucket;
    }

    public String getS3Key() {
        return this.s3Key;
    }

    public String getInputPath() {
        return this.inputPath;
    }
}
