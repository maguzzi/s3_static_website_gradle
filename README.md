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
