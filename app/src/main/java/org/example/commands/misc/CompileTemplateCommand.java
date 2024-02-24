package org.example.commands.misc;

import java.nio.file.Paths;

import org.example.App;
import org.example.commands.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class CompileTemplateCommand implements Command {

    private static final Logger logger = LoggerFactory.getLogger(CompileTemplateCommand.class);

    private String templatePath;
    
    public CompileTemplateCommand(String templatePath) {
        this.templatePath = templatePath;
    }

    @Override
    public Map<String,String> execute() throws Exception {
        App.screenMessage("COMPILE TEMPLATE START");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.readTree(readcontent(templatePath)); ////////////////// HEREEEEEEEEEEEEEE //////////////////

        App.screenMessage("COMPILE TEMPLATE END");
        Map<String,String> outputMap = new HashMap<String,String>();
        return outputMap;
    }

}