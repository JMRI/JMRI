package jmri.jmrit.ctc;

import java.io.File;
import java.io.IOException;
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

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CTCFiles.class);
}
