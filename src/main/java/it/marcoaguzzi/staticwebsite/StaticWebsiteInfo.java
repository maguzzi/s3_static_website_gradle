package it.marcoaguzzi.staticwebsite;

import java.nio.file.*;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class StaticWebsiteInfo {

    private static final Logger logger = LoggerFactory.getLogger(StaticWebsiteInfo.class);

    private String environment;
    private String websiteName;
    private String websiteDomain;
    private String websiteAlternativeDomain;

    public static StaticWebsiteInfo fromWebsiteProperty() throws Exception {
        Path path = Paths.get("./website.properties");
        try {
            Properties propertiesFile = Utils.readPropertiesFile(path);
            return StaticWebsiteInfo
                    .builder()
                    .websiteName(propertiesFile.getProperty("name"))
                    .environment(propertiesFile.getProperty("environment"))
                    .websiteDomain(propertiesFile.getProperty("domain"))
                    .websiteAlternativeDomain(propertiesFile.getProperty("alternativeDomain", ""))
                    .build();
        } catch (NoSuchFileException nsfe) {
            logger.error("");
            logger.error("{}/website.properties file not found. Please create such file with this content:",Paths.get(".").toRealPath());
            logger.error("name = Sample project name");
            logger.error("environment = sampleEnvironent");
            logger.error("domain = yourdomain.example");
            throw nsfe;
        }
    }
}
