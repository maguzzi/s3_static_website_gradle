package it.marcoaguzzi.staticwebsite.commands.cloudformation;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.marcoaguzzi.staticwebsite.App;
import software.amazon.awssdk.services.cloudformation.model.Tag;

public class TagChecker {

    private static final Logger logger = LoggerFactory.getLogger(TagChecker.class);

    public static boolean stackContainsTag(List<Tag> tags) {
        logger.debug("Check if tags are complete: {}",tags);
        List<String> tagKeys =  tags.stream().map(it -> it.key()).collect(Collectors.toList());
        List<String> tagKeysToCheck = Arrays.asList(
            App.S3_STATIC_WEBSITE_TAG,
            App.S3_STATIC_WEBSITE_ENVIRONMENT_TAG);
        return tagKeys.containsAll(tagKeysToCheck);                
    }
}
