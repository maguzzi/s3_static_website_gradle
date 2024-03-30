package it.marcoaguzzi.staticwebsite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class UtilsTest {
 
    @Test
    public void testReadFileFromResource() throws Exception {
        String fileContent = Utils.readFileContent("./test.txt");    
        assertEquals("test",fileContent);
    }

    @Test
    public void testZipFile() throws Exception {
        String zipFile = Utils.zipFile("./test.txt","./file.zip");    
        assertNotNull(zipFile);
        
    }

}
