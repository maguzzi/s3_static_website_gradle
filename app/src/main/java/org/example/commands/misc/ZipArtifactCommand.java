package org.example.commands.misc;

import java.util.HashMap;
import java.util.Map;

import org.example.App;
import org.example.commands.Command;
import org.example.commands.CommandUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZipArtifactCommand implements Command {

    private static final Logger logger = LoggerFactory.getLogger(ZipArtifactCommand.class);

    public static final String ARTIFACT_COMPRESSED_PATH = "ARTIFACT_COMPRESSED_PATH";

    private String sourcePath;
    private String zipFile;

    public ZipArtifactCommand(String sourcePath, String zipFile) {
        this.sourcePath = sourcePath;
        this.zipFile = zipFile;
    }

    @Override
    public Map<String, String> execute() throws Exception {
        App.screenMessage("ZIP ARTIFACT START");
        logger.debug("{} --> {}", sourcePath, zipFile);
        String compressedPath = CommandUtil.zipFile(sourcePath, zipFile);
        Map<String, String> outputMap = new HashMap<String, String>();
        outputMap.put(ARTIFACT_COMPRESSED_PATH,compressedPath);
        App.screenMessage("ZIP ARTIFACT END");
        return outputMap;
    }

}
