package org.example.commands;

import java.util.Map;

public interface Command {
    public Map<String,String> execute() throws Exception;
}
