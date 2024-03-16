package it.marcoaguzzi.staticwebsite.commands.cloudformation;

import it.marcoaguzzi.staticwebsite.App;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Builder.Default;

@Getter
@Builder
public class StackInfo {
    @Default
    private String environmentString = App.getEnvironment();
    @NonNull
    private String templatePath;
    @NonNull
    private String stackName;
    @Default
    private String websiteName = App.getWebsiteName();
    @Default
    private String psedoRandomTimestampString = App.getPsedoRandomTimestampString();
}

