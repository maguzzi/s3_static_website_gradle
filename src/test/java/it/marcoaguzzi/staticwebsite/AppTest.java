package it.marcoaguzzi.staticwebsite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;

public class AppTest {

    @Test
    public void testAppNoCommand() {
        try {
            new App(null, null, null);
            fail("Should ask for command");
        } catch (Exception e) {
            assertEquals(" - Command is required", e.getMessage());
        }
    }

    @Test
    public void testAppWithCommand() throws Exception{
        new App(new String[] { "COMMAND" }, null, null);
    }

}
