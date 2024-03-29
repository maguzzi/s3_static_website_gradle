package it.marcoaguzzi.staticwebsite;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class UtilsTest {
 
    @Test
    public void testReadFileFromResource() throws Exception {
        String fileContent = new Utils().readFileContent("./utiltest.txt");    
        assertEquals("test",fileContent);
    }

}
