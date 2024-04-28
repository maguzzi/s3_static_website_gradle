package it.marcoaguzzi.staticwebsite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;

public class AppTest {

    private static final String WEBSITE_PROPERTIES_IN_CLASSPATH = "website_sample.properties";

    @Test
    public void testAppNoCommand() {
        try {
            new App(null, null, null,null,WEBSITE_PROPERTIES_IN_CLASSPATH);
            fail("Should ask for command");
        } catch (Exception e) {
            assertEquals(" - Command is required", e.getMessage());
        }
    }

    @Test
    public void testAppWithCommand() throws Exception{
        new App(new String[] { "COMMAND" }, null, null,null,WEBSITE_PROPERTIES_IN_CLASSPATH);
    }

}
