package it.marcoaguzzi.staticwebsite;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {

    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    public static String readFileContent(String pathString) throws Exception {
        URL url = Utils.class.getClassLoader().getResource(pathString);
        logger.info("reading content from: {}", url);
        return readByteFromURL(url);
    }

    public static String zipFile(String sourceFile, String zipFileName) throws Exception {

        Path tmpDir = Files.createTempDirectory("cloudformation_tmp");
        File fileToZip = new File(sourceFile);
        File zipFile = new File(tmpDir.toAbsolutePath() + File.separator + zipFileName);

        try (
                InputStream fis = Utils.class.getClassLoader().getResourceAsStream(sourceFile);
                FileOutputStream fos = new FileOutputStream(zipFile);
                ZipOutputStream zipOut = new ZipOutputStream(fos);) {

            ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
            zipOut.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            return zipFile.getAbsolutePath();
        } catch (IOException ioe) {
            throw new Exception(ioe);
        }
    }

    private static String readByteFromURL(URL url) throws Exception {
        try (InputStream is = url.openStream();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ) {
            
            byte[] bytes = new byte[1024];
            int length;
            while ((length = is.read(bytes)) >= 0) {
                bos.write(bytes, 0, length);
            }
            return bos.toString();
        } catch (IOException ioe) {
            throw new Exception(ioe);
        }
    }

    public static Properties readPropertiesFile(Path path) throws Exception {
        Properties properties = new Properties();
        InputStream newInputStream = Files.newInputStream(path);
        properties.load(new InputStreamReader(newInputStream));
        newInputStream.close();
        return properties;
    }
}
