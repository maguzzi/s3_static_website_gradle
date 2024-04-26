package it.marcoaguzzi.staticwebsite.commands;

import java.util.Map;

import it.marcoaguzzi.staticwebsite.commands.cloudformation.OutputEntry;

public interface Command {
    default void setInputs(Map<String,Object> inputs){};
    Map<String,OutputEntry> execute() throws Exception;
}
