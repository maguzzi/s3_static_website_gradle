package it.marcoaguzzi.staticwebsite;

import java.nio.file.Paths;
import java.util.Properties;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class StaticWebsiteInfo {
    private String environment;
    private String websiteName;
    private String websiteDomain;
    private String websiteAlternativeDomain;

    public static StaticWebsiteInfo fromPropertyFile() throws Exception {
        Properties propertiesFile = Utils.readPropertiesFile(Paths.get("./website.properties"));
        return StaticWebsiteInfo
        .builder()
        .websiteName(propertiesFile.getProperty("name"))
        .environment(propertiesFile.getProperty("environment"))
        .websiteDomain(propertiesFile.getProperty("domain"))
        .websiteDomain(propertiesFile.getProperty("alternativeDomain",""))
        .build();
    }
}


