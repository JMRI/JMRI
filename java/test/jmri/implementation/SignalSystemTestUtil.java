package jmri.implementation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import jmri.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Static utilities for testing signal system code.
 *
 * @author	Bob Jacobsen Copyright 2014, 2015
 */
public class SignalSystemTestUtil {

    // Where in user space the "signals" file tree should live
    private static File path = null;

    // name of a dummy signal system being used for testing
    private static File dummy = null;

    static public void createMockSystem() throws IOException {
        // creates mock (no appearances) system
        // in the user area.
        // Where in user space the "signals" file tree should live
        path = new File(FileUtil.getUserFilesPath(), "resources");
        dummy = new File(new File(path, "signals"), "JUnitTestSignals"); // something that won't exist
        try {
            FileUtil.createDirectory(dummy);
            {
                Path inPath = new File(new File(FileUtil.getProgramPath(), "java/test/jmri/implementation"), "testAspects.xml").toPath();
                Path outPath = new File(dummy, "aspects.xml").toPath();
                Files.copy(inPath, outPath, StandardCopyOption.REPLACE_EXISTING);
            }
            {
                Path inPath = new File(new File(FileUtil.getProgramPath(), "java/test/jmri/implementation"), "test-appearance-one-searchlight.xml").toPath();
                Path outPath = new File(dummy, "appearance-one-searchlight.xml").toPath();
                Files.copy(inPath, outPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            log.error("Exception during createMockSystem", e);
            throw e;
        }

    }

    static public String getMockUserName() {
        return "JUnit Test Signals"; // from testAspects.xml file
    }

    static public String getMockSystemName() {
        return dummy.getName();
    }

    static public void deleteMockSystem() throws IOException {
        FileUtil.delete(dummy);
        dummy = null;
        path = null;
    }

    private final static Logger log = LoggerFactory.getLogger(SignalSystemTestUtil.class);
}
