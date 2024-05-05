# S3 static website architecture generator tool.

## What's needed?

- Java 8+
- An AWS account, with authentication in place. As of now, I've tested it with having ACCCESS_KEY and ACCESS_SECRET as enviornment variables locally. If those are not found, the tool stops.
- A dns domain. Let's use a free service: [s3staticwebsitetest.cloudns.ch](s3staticwebsitetest.cloudns.ch)
- A config file named _website.properties_ containing the website information, like this:
```
name = Fast, secure, free-ish S3 static website
environment = dev
domain = dev.s3staticwebsitetest.cloudns.ch
```
## Tool commands
Let's set the LOG_LEVEL to INFO in order not to clog the shell, and check the existing stack in our AWS account:

### DISTRIBUTION
Expecting the aws account still without this infrastructure, this is the first command to run:

```
java -jar s3_static_website_gradle-all.jar DISTRIBUTION
```
Here's the output:
```
2024-05-01T14:14:16 [main] INFO  - AWS_REGION: us-east-1
2024-05-01T14:14:16 [main] INFO  - AWS_ACCESS_KEY_ID: AKIA****
2024-05-01T14:14:16 [main] INFO  - AWS_SECRET: *****
2024-05-01T14:14:16 [main] INFO  - AWS setup done.
2024-05-01T14:14:17 [main] INFO  - reading content from file: /home/maguzzi/demo/./website.properties
2024-05-01T14:14:17 [main] INFO  - Command: DISTRIBUTION environment: dev
2024-05-01T14:14:17 [main] WARN  - .websitesetup file does not exists. Creating
2024-05-01T14:14:17 [main] INFO  - reading content from file: /home/maguzzi/demo/./.websitesetup
2024-05-01T14:14:17 [main] INFO  - Setup new pseudoRandomTimestampString to 20240501141417491
2024-05-01T14:14:17 [main] INFO  - Setup new zipDate to 20240501
2024-05-01T14:14:17 [main] INFO  - 
2024-05-01T14:14:17 [main] INFO  -  -- s3-static-website-bootstrap-stack - dev CREATION START --
2024-05-01T14:14:17 [main] INFO  - 
2024-05-01T14:14:17 [main] INFO  - reading content from jar: jar:file:/home/maguzzi/demo/s3_static_website_gradle-all.jar!/bootstrap/bootstrap.json
2024-05-01T14:14:18 [main] INFO  - Stack s3-static-website-bootstrap-stack-dev not yet completed, wait
2024-05-01T14:14:23 [main] INFO  - Stack s3-static-website-bootstrap-stack-dev not yet completed, wait
...
```
It states:
- The environment variable for the AWS configuration are in place
- The *website.properties* file is in place
- The *.websitesetup* file does not exist, and is created at run time.

*pseudoRandomTimestampString* is used to have unique names for the s3 buckets, while *zipDate* to identify the artifact for the lambda.
Now the *bootstrap* stack is being created, so that the S3 buckets will be in place when needed for:
- packaging the templates
- upload the lambda artifact
- host the website content

The last two lines state that the tool is waiting for the completion of the *bootstrap* stack.
Once that the first stack has been successfully created, the tool states it and outputs the export key for the s3 buckets that will be needed in the *distribution* stacks:
```
Stack creation for stack id arn:aws:cloudformation:us-east-1:****:stack/s3-static-website-bootstrap-stack-dev/***** terminated.
2024-05-01T14:14:54 [main] INFO  - 
2024-05-01T14:14:54 [main] INFO  -  -- s3-static-website-bootstrap-stack - dev CREATION END --
2024-05-01T14:14:54 [main] INFO  - 
2024-05-01T14:14:54 [main] INFO  - ArtifactS3Bucket -> s3-static-website-lambda-artifact-dev-20240501141417491 (s3-static-website-bootstrap-stack-dev-LambdaArtifactBucket-Export-dev)
2024-05-01T14:14:54 [main] INFO  - CompiledTemplateBucket -> s3-static-website-compiled-template-dev-20240501141417491 (s3-static-website-bootstrap-stack-dev-CompiledTemplateBucket-Export-dev)
```
It then continues to prepare the files that will be needed for the distribution stack (let's view a trimmed version of the log):
```
2024-05-01T14:14:54 [main] INFO  -  -- ZIP ARTIFACT START --
...
2024-05-01T14:14:54 [main] INFO  -  -- ZIP ARTIFACT END --
2024-05-01T14:14:54 [main] INFO  - 
2024-05-01T14:14:54 [main] INFO  - ARTIFACT_COMPRESSED_PATH -> /tmp/cloudformation_tmp10381003167251864437/lambda-edge-dev-20240501.zip (-)
...
2024-05-01T14:14:54 [main] INFO  -  -- UPLOAD FILE TO BUCKET START --
...
2024-05-01T14:14:55 [main] INFO  - URL: https://s3-static-website-lambda-artifact-dev-20240501141417491.s3.amazonaws.com/lambda-edge-dev-20240501.zip
2024-05-01T14:14:55 [main] INFO  -  -- UPLOAD FILE TO BUCKET END --
...
2024-05-01T14:14:56 [main] INFO  -  -- PACKAGE TEMPLATE START --
2024-05-01T14:14:56 [main] INFO  - reading content from jar: jar:file:/home/maguzzi/demo/s3_static_website_gradle-all.jar!/distribution/website-distribution.json
2024-05-01T14:14:56 [main] INFO  -  -- PACKAGE TEMPLATE END --
2024-05-01T14:14:56 [main] INFO  - 
...
2024-05-01T14:14:56 [main] INFO  -  -- s3-static-website-distribution-stack - dev CREATION START --
2024-05-01T14:14:56 [main] INFO  - 
2024-05-01T14:14:56 [main] INFO  - reading content from file: /tmp/202405011414567687239912532179902_compiled_template.json
2024-05-01T14:14:56 [main] INFO  - 
2024-05-01T14:14:56 [main] INFO  -  -- s3-static-website-distribution-stack - dev CREATION END --
2024-05-01T14:14:56 [main] INFO  - 
```
In the log it can be seen that zip files for artifacts are loaded onto S3, and then the template with the sub-stack reference is packaged and put into a temporary folder before the create-stack command is issued.
Since we're using a free domain, the tool stops here because cloudformation can't complete its creation without configuring the domain provider (cloudns in this case). So let's check the DNS information that has to be provided to the cloudns:

### DNS_INFO

```
java -jar s3_static_website_gradle-all.jar DNS_INFO dns-info.txt
```
and the output:
```
2024-05-01T14:15:39 [main] INFO  - AWS_REGION: us-east-1
2024-05-01T14:15:39 [main] INFO  - AWS_ACCESS_KEY_ID: AK********
2024-05-01T14:15:39 [main] INFO  - AWS_SECRET: *****
2024-05-01T14:15:39 [main] INFO  - AWS setup done.
2024-05-01T14:15:40 [main] INFO  - reading content from file: /home/maguzzi/demo/./website.properties
2024-05-01T14:15:40 [main] INFO  - Command: DNS_INFO environment: dev
2024-05-01T14:15:40 [main] INFO  - reading content from file: /home/maguzzi/demo/./.websitesetup
2024-05-01T14:15:40 [main] INFO  - PseudoRandomTimestampString already set to 20240501141417491
2024-05-01T14:15:40 [main] INFO  - ZipDate already set to 20240501
2024-05-01T14:15:40 [main] INFO  - 
2024-05-01T14:15:40 [main] INFO  -  -- ROUTE 53 INFO START --
2024-05-01T14:15:40 [main] INFO  - 
2024-05-01T14:15:41 [main] INFO  - Got hosted zone Id Optional[Z******] for stack s3-static-website-distribution-stack-dev
2024-05-01T14:15:41 [main] INFO  - hostedZoneId: Optional[Z*****]
2024-05-01T14:15:41 [main] INFO  - Name: dev.s3staticwebsitetest.cloudns.ch. TTL: 172800
2024-05-01T14:15:41 [main] INFO  - ns-1333.awsdns-38.org.
2024-05-01T14:15:41 [main] INFO  - ns-1572.awsdns-04.co.uk.
2024-05-01T14:15:41 [main] INFO  - ns-844.awsdns-41.net.
2024-05-01T14:15:41 [main] INFO  - ns-458.awsdns-57.com.
2024-05-01T14:15:41 [main] INFO  - 
2024-05-01T14:15:41 [main] INFO  -  -- ROUTE 53 INFO END --
2024-05-01T14:15:41 [main] INFO  - 
```
The tool conventiently outputs the DNS information (last 4 lines) in the file *dns-info.txt* (as specified on the command line).
It can be uploaded as-is on the cloudns web ui.
Once that the DNS provider has propagated the DNS records provided by AWS, the tool can be started again to check if the distribution stack has been completed.

### CHECK

```
java -jar s3_static_website_gradle-all.jar CHECK
```
The output is pretty straightforward:
```
2024-05-01T14:24:11 [main] INFO  - AWS_REGION: us-east-1
2024-05-01T14:24:11 [main] INFO  - AWS_ACCESS_KEY_ID: AKIA********
2024-05-01T14:24:11 [main] INFO  - AWS_SECRET: *****
2024-05-01T14:24:11 [main] INFO  - AWS setup done.
2024-05-01T14:24:12 [main] INFO  - reading content from file: /home/maguzzi/demo/./website.properties
2024-05-01T14:24:12 [main] INFO  - Command: CHECK environment: dev
2024-05-01T14:24:13 [main] INFO  - Stack s3-static-website-distribution-stack-dev not yet completed, wait
...
2024-05-01T14:26:32 [main] INFO  - Stack s3-static-website-distribution-stack-dev not yet completed, wait
2024-05-01T14:26:42 [main] INFO  - Stack creation for stack id arn:aws:cloudformation:us-east-1:****:stack/s3-static-website-distribution-stack-dev/****** terminated.
```
Now we can list the stacks that have been created, along with their tags. As you can see, the random string that has been setup in the beginning has been propagated onto all the stacks, along with the S3 buckets and all the taggable resources.

# LIST

```
java -jar s3_static_website_gradle-all.jar LIST
```
Here's the output:
```
2024-05-01T14:29:59 [main] INFO  - AWS_REGION: us-east-1
2024-05-01T14:29:59 [main] INFO  - AWS_ACCESS_KEY_ID: AKIA*****
2024-05-01T14:29:59 [main] INFO  - AWS_SECRET: *****
2024-05-01T14:29:59 [main] INFO  - AWS setup done.
2024-05-01T14:30:00 [main] INFO  - reading content from file: /home/maguzzi/demo/./website.properties
2024-05-01T14:30:00 [main] INFO  - Command: LIST environment: dev
2024-05-01T14:30:00 [main] INFO  - reading content from file: /home/maguzzi/demo/./.websitesetup
2024-05-01T14:30:00 [main] INFO  - PseudoRandomTimestampString already set to 20240501141417491
2024-05-01T14:30:00 [main] INFO  - ZipDate already set to 20240501
2024-05-01T14:30:00 [main] INFO  - 
2024-05-01T14:30:00 [main] INFO  -  -- LIST STACK START --
2024-05-01T14:30:00 [main] INFO  - 
2024-05-01T14:30:01 [main] INFO  - s3-static-website-distribution-stack-dev-LambdaEdgeCloudFrontStack-FZ****** (CREATE_COMPLETE) - [Tag(Key=s3_static_website_environment, Value=dev), Tag(Key=s3_static_website, Value=S3 static website test), Tag(Key=s3_static_website_timestamp_tag, Value=20240501141417491)]
2024-05-01T14:30:01 [main] INFO  - s3-static-website-distribution-stack-dev (CREATE_COMPLETE) - [Tag(Key=s3_static_website_environment, Value=dev), Tag(Key=s3_static_website, Value=S3 static website test), Tag(Key=s3_static_website_timestamp_tag, Value=20240501141417491)]
2024-05-01T14:30:01 [main] INFO  - s3-static-website-bootstrap-stack-dev (CREATE_COMPLETE) - [Tag(Key=s3_static_website_environment, Value=dev), Tag(Key=s3_static_website, Value=S3 static website test), Tag(Key=s3_static_website_timestamp_tag, Value=20240501141417491)]
2024-05-01T14:30:02 [main] INFO  - 
2024-05-01T14:30:02 [main] INFO  -  -- LIST STACK END --
2024-05-01T14:30:02 [main] INFO  - 
```
At this point, the website has no content. The only thing left to do is to upload a random *index.html* on the S3 bucket to see if all the process worked fine. I've let the step out of the tool because the website content is intented to be created and uploaded by a pipeline.
You can catch the random string of the s3 bucket in the log:
```
aws s3 cp index.html s3 bucket
```
And then you can point the browser to http://dev.s3staticwebsitetest.cloudns.ch and see that it worked!

# What's next?
In the next post, we'll integrate the CICD pipeline that uploads the website content, along with some consideration about how to delete the stack without using the UI. Cloudformation and Cloudfront force some contraints on how the resources should be deleted, so it's worth spending some time on it.
