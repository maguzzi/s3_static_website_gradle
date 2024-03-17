package it.marcoaguzzi.staticwebsite.commands.misc;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.marcoaguzzi.staticwebsite.App;
import it.marcoaguzzi.staticwebsite.commands.Command;
import it.marcoaguzzi.staticwebsite.commands.cloudformation.OutputEntry;

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
    public Map<String, OutputEntry> execute() throws Exception {
        App.screenMessage("ZIP ARTIFACT START");
        logger.debug("{} --> {}", sourcePath, zipFile);
        String compressedPath = Utils.zipFile(sourcePath, zipFile);
        Map<String, OutputEntry> outputMap = new HashMap<>();
        outputMap.put(ARTIFACT_COMPRESSED_PATH,new OutputEntry(ARTIFACT_COMPRESSED_PATH, compressedPath));
        App.screenMessage("ZIP ARTIFACT END");
        return outputMap;
    }

}
