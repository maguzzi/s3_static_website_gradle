package it.marcoaguzzi.staticwebsite;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO fix file reading!!
public class Utils {

    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    public static String readFileContent(String pathString) throws Exception {
        if (pathString.startsWith(".") || pathString.startsWith("/")) {
            return readFileContentFromFile(pathString);
        } else {
            return readFileContentFromJar(pathString);
        }
    }

    private static String readFileContentFromJar(String pathString) throws Exception {
        logger.info("reading content from jar: {}", Utils.class.getClassLoader().getResource(pathString));
        InputStream stream = Utils.class.getClassLoader().getResourceAsStream(pathString);
        return readByteFromURL(stream);
    }

    private static String readFileContentFromFile(String pathString) throws Exception {
        Path path = Paths.get(pathString);
        logger.info("reading content from file: {}", path.toAbsolutePath());
        return readByteFromURL(path.toUri().toURL().openStream());
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

    private static String readByteFromURL(InputStream is) throws Exception {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
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

    public static Properties readPropertiesFile(String content) throws Exception {
        Properties properties = new Properties();
        properties.load(new StringReader(content));
        return properties;
    }
}
