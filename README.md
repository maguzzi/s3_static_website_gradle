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
