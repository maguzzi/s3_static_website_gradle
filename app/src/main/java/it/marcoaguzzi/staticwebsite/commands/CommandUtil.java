package it.marcoaguzzi.staticwebsite.commands;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandUtil {

    private static final Logger logger = LoggerFactory.getLogger(CommandUtil.class);

    // TODO path inside the jar
    public static String readFileContent(String pathString) throws IOException {
        Path path = Paths.get(pathString);
        logger.info("reading content from: {}", path.toAbsolutePath());
        return new String(Files.readAllBytes(path));
    }

    // TODO better with try-with-resources
    public static String zipFile(String sourceFile, String zipFileName) throws Exception {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        ZipOutputStream zipOut = null;
        Path tmpDir = null;
        try {
            tmpDir = Files.createTempDirectory("cloudformation_tmp");
            File zipFile = new File(tmpDir.toAbsolutePath()+File.separator+zipFileName);
            fos = new FileOutputStream(zipFile);
            zipOut = new ZipOutputStream(fos);

            File fileToZip = new File(sourceFile);
            fis = new FileInputStream(fileToZip);
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
        } finally {
            if (zipOut != null) {
                zipOut.close();
            }
            if (fis != null) {
                fis.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
    }

    public static String dateToDay() {
        return new SimpleDateFormat("yyyyMMdd").format(new Date());
    }

    public static String dateToSecond() {
        return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    }

}