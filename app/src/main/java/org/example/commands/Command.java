package org.example.commands;

import java.util.Map;

public interface Command {
    default void setInputs(Map<String,Object> inputs){};
    Map<String,String> execute() throws Exception;
}
