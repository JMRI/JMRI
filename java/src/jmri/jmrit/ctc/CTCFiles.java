package jmri.jmrit.ctc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.NoSuchFileException;
import java.nio.file.StandardCopyOption;
import jmri.util.FileUtil;

/**
 * CTC File utility

 * @author Dave Sand Copyright (C) 2019
 */

public class CTCFiles {

    /**
     * Verify that the standard file path is valid.
     * Create the ctc directory if needed.
     * @param fileName The name of the file
     * @return the requested file object or null if the path is not valid.
     */
    public static File getFile(String fileName) {
        // Verify that preference:ctc exists
        File chkdir = new File(getFileLocation());
        if (!chkdir.exists()) {
            if (!chkdir.mkdir()) {
                log.error("Create preference:ctc failed");  // NOI18N
                return null;
            }
        }
        return new File(getFullName(fileName));
    }

    public static String getFileLocation() {
        return new File(FileUtil.getUserFilesPath(), "ctc").getAbsolutePath();  // NOI18N
    }

    /**
     * Create the full file name with path
     * @param fileName The name of the file.
     * @return the full path and name.
     */
    public static String getFullName(String fileName) {
        return new File(getFileLocation(), fileName).getAbsolutePath();
    }

    public static boolean fileExists(String fileName) {
        File file = getFile(fileName);
        return (file == null ? false : file.exists());
    }

    public static boolean renameFile(String oldFileName, String newFileName) {
        File oldFile = getFile(oldFileName);
        File newFile = getFile(newFileName);
        if (newFile.exists()) {
            log.error("Rename file {} failed: new file {} already exists", oldFileName,  newFileName);
            return false;
        }
        return oldFile.renameTo(newFile);
    }

    public static boolean deleteFile(String fileName) {
        boolean result = true;
        try {
            File file = getFile(fileName);
            Files.delete(file.toPath());
        } catch (NoSuchFileException nf) {
            result = true;  // No file is OK
        } catch (Exception ex) {
            log.info("deleteFile: ex", ex);
            result = false;
        }
// o use Files.delete(java.nio.file.Path) or Files.deleteIfExists(java.nio.file.Path) for
        return result;
    }

    public static Path copyFile(String sourceFileName, String destFileName, boolean replace) throws IOException {
        File sourceFile = getFile(sourceFileName);
        File destFile = getFile(destFileName);
        if (destFile.exists() && !replace) {
            log.error("Rename file {} failed: new file {} already exists", sourceFileName,  destFileName);
            return null;
        }
        return Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    public static String addExtensionIfMissing(String path, String missingExtension) {
        String filenameOnly = getFilenameOnly(path);
        if (filenameOnly.indexOf('.') >= 0) return path;
        return path + missingExtension;
    }

    public static String changeExtensionTo(String path, String newExtension) {
        return addExtensionIfMissing(removeFileExtension(path), newExtension);
    }

    public static String removeFileExtension(String filename) {
        final int lastIndexOf = filename.lastIndexOf('.');
        return lastIndexOf >= 1 ? filename.substring(0, lastIndexOf) : filename;
    }

    public static String getFilenameOnly(String path) {
        // Paths.get(path) can return null per the Paths documentation
        Path file = Paths.get(path);
        if (file != null){
            Object fileName = file.getFileName();
            if (fileName!=null) {
                return fileName.toString();
            }
        }
        return "";
    }


    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CTCFiles.class);
}
