$Env:LOG_LEVEL="DEBUG"
$Env:LOG_LEVEL = "INFO"

check all stack resources
Values=S3 static website project
$ aws resourcegroupstaggingapi get-resources --tag-filters "Key=s3_static_website,Values=s3 name" --query='ResourceTagMappingList[*].ResourceARN'

empty s3 bucket to delete the cloudformation stack
aws s3 rm s3://bucket-name --recursive
log:
delete: s3://s3-static-website-lambda-artifact-dev-20240317112619623/filename

list of resources with the s3 static website tag
```
aws resourcegroupstaggingapi get-resources --tag-filters "Key=s3_static_website,Values=My beautiful website"
```
output: 
```
{   
    "ResourceTagMappingList": []
}
```
change log level
```
export LOG_LEVEL=INFO
```
see resources for the s3 static website
```
$ ./gradlew run --args="LIST dev" #change the name for the param, dev doesn't mean anything
```
$ ./gradlew run --args="BOOTSTRAP dev"

> Task :app:run
18:17:35.707 [main] INFO  it.marcoaguzzi.staticwebsite.App - Command: BOOTSTRAP environment: dev
18:17:35.744 [main] INFO  it.marcoaguzzi.staticwebsite.App -
18:17:35.744 [main] INFO  it.marcoaguzzi.staticwebsite.App -  -- s3-static-website-bootstrap-stack - dev CREATION START --
18:17:35.744 [main] INFO  it.marcoaguzzi.staticwebsite.App -
18:17:35.746 [main] INFO  it.marcoaguzzi.staticwebsite.commands.CommandUtil - reading content from: D:\aws_gradle_test\app\.\src\main\resources\bootstrap\bootstrap.json
18:17:37.491 [main] INFO  it.marcoaguzzi.staticwebsite.commands.cloudformation.StackCompleteChecker - Stack s3-static-website-bootstrap-stack-dev not yet completed, wait
18:17:42.637 [main] INFO  it.marcoaguzzi.staticwebsite.commands.cloudformation.StackCompleteChecker - Stack s3-static-website-bootstrap-stack-dev not yet completed, wait
18:17:47.793 [main] INFO  it.marcoaguzzi.staticwebsite.commands.cloudformation.StackCompleteChecker - Stack s3-static-website-bootstrap-stack-dev not yet completed, wait
18:17:52.956 [main] INFO  it.marcoaguzzi.staticwebsite.commands.cloudformation.StackCompleteChecker - Stack s3-static-website-bootstrap-stack-dev not yet completed, wait
18:17:58.109 [main] INFO  it.marcoaguzzi.staticwebsite.commands.cloudformation.StackCompleteChecker - Stack s3-static-website-bootstrap-stack-dev not yet completed, wait
18:18:03.283 [main] INFO  it.marcoaguzzi.staticwebsite.commands.cloudformation.StackCompleteChecker - Stack s3-static-website-bootstrap-stack-dev not yet completed, wait
18:18:08.450 [main] INFO  it.marcoaguzzi.staticwebsite.commands.cloudformation.StackCompleteChecker - Stack s3-static-website-bootstrap-stack-dev not yet completed, wait
18:18:13.631 [main] INFO  it.marcoaguzzi.staticwebsite.commands.cloudformation.CreateStackCommand - Stack creation for stack id arn:aws:cloudformation:us-east-1:239511348388:stack/s3-static-website-bootstrap-stack-dev/159e60e0-e3b9-11ee-8f30-12d88113ece1 terminated.
18:18:13.631 [main] INFO  it.marcoaguzzi.staticwebsite.App -
18:18:13.631 [main] INFO  it.marcoaguzzi.staticwebsite.App -  -- s3-static-website-bootstrap-stack - dev CREATION END --
18:18:13.631 [main] INFO  it.marcoaguzzi.staticwebsite.App -

$ ./gradlew run --args="LIST dev" #change the name for the param, dev doesn't mean anything

$ ./gradlew run --args="LIST dev"                                                                                                                                                                             

> Task :app:run
18:19:33.061 [main] INFO  it.marcoaguzzi.staticwebsite.App - Command: LIST environment: dev
18:19:33.101 [main] INFO  it.marcoaguzzi.staticwebsite.App -
18:19:33.101 [main] INFO  it.marcoaguzzi.staticwebsite.App -  -- LIST STACK START --
18:19:33.101 [main] INFO  it.marcoaguzzi.staticwebsite.App -
18:19:34.596 [main] INFO  it.marcoaguzzi.staticwebsite.commands.cloudformation.ListStacksCommand - s3-static-website-bootstrap-stack-dev (CREATE_COMPLETE) - [Tag(Key=s3_static_website_environment, Value=dev), Tag(Key=s3_static_website, Value=S3 static website project)]
18:19:35.466 [main] INFO  it.marcoaguzzi.staticwebsite.App -
18:19:35.466 [main] INFO  it.marcoaguzzi.staticwebsite.App -  -- LIST STACK END --
18:19:35.466 [main] INFO  it.marcoaguzzi.staticwebsite.App -
>
> 
> aws resourcegroupstaggingapi get-resources --tag-filters "Key=s3_static_website,Values=S3 static website project" --query ResourceTagMappingList[*].ResourceARN

[
    "arn:aws:cloudformation:us-east-1:239511348388:stack/s3-static-website-bootstrap-stack-dev/a07d5d20-e449-11ee-a123-0ee516927a51",
    "arn:aws:s3:::s3-static-website-lambda-artifact-dev-20240317112619623",
    "arn:aws:s3:::s3-static-website-compiled-template-dev-20240317112619623"
]