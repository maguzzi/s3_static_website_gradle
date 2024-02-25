package org.example.commands.cloudformation;

public class StackParams {
    private String environmentString;
    private String templatePath;
    private String stackName;

    // TODO make builder
    public StackParams(String environmentString,String templatePath,String stackName) {
        this.environmentString = environmentString;
        this.templatePath = templatePath;
        this.stackName = stackName;
    }

    public String getEnvironmentString() {
        return this.environmentString;
    }
    
    public String getTemplatePath() {
        return this.templatePath;
    }

    public String getStackName() {
        return this.stackName;
    }

}

