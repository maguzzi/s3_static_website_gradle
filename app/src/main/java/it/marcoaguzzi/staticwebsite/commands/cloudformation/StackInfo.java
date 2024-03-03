package it.marcoaguzzi.staticwebsite.commands.cloudformation;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Builder.Default;

@Getter
@Builder
public class StackInfo {
    @NonNull
    private String environmentString;
    @NonNull
    private String templatePath;
    @NonNull
    private String stackName;
    @Default
    private String websiteName = "my beautiful website"; // TODO read it from conf file
}

