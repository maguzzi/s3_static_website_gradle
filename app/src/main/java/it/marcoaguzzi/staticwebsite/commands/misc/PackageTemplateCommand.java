package it.marcoaguzzi.staticwebsite.commands.misc;

import java.io.File;
import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import it.marcoaguzzi.staticwebsite.App;
import it.marcoaguzzi.staticwebsite.Utils;
import it.marcoaguzzi.staticwebsite.commands.Command;
import it.marcoaguzzi.staticwebsite.commands.cloudformation.OutputEntry;

public class PackageTemplateCommand implements Command {

    public static final String PACKAGED_TEMPLATE_PATH = "PACKAGED_TEMPLATE_PATH";
    public static final String S3_PATH_TO_REPLACE = "S3_PATH_TO_REPLACE";

    private static final Logger logger = LoggerFactory.getLogger(PackageTemplateCommand.class);

    private String templateSrcPath;
    private String s3PathToReplace;
    
    
    public PackageTemplateCommand(String templateSrcPath) {
        this.templateSrcPath = templateSrcPath;
    }

    @Override
    public void setInputs(Map<String,Object> inputs) {
        this.s3PathToReplace=((OutputEntry)inputs.get(S3_PATH_TO_REPLACE)).getValue();
    }

    @Override
    public Map<String,OutputEntry> execute() throws Exception {
        App.screenMessage("PACKAGE TEMPLATE START");
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(Utils.readFileContent(templateSrcPath)); 
        logger.trace("local template: {}",root.toString());
        logger.trace("Resources: {}",root.get("Resources").toString());
        Iterator<JsonNode> elements = root.get("Resources").elements();
        while(elements.hasNext()) {
            JsonNode node = elements.next();    
            if ("AWS::CloudFormation::Stack".equals(node.get("Type").asText())) {
                logger.debug("found nested stack {}",node.toString());
                JsonNode properties = node.get("Properties");
                logger.debug("template url: {}",properties.get("TemplateURL"));
                ((ObjectNode)properties).put("TemplateURL", s3PathToReplace);
            }
            logger.trace(node.toString());
        };
        File file = File.createTempFile(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()),"_compiled_template.json");
        objectMapper.writeValue(file, root);
        App.screenMessage("PACKAGE TEMPLATE END");
        Map<String,OutputEntry> outputMap = new HashMap<>();
        outputMap.put(PACKAGED_TEMPLATE_PATH, new OutputEntry(PACKAGED_TEMPLATE_PATH,file.getAbsolutePath()));
        return outputMap;
    }

}