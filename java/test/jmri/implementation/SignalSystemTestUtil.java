package jmri.implementation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import jmri.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Static utilities for testing signal system code
 *
 * @author	Bob Jacobsen Copyright 2014, 2015
 */
public class SignalSystemTestUtil {

    // Where in user space the "signals" file tree should live
    static final File PATH = new File(FileUtil.getUserFilesPath(), "resources");

    // name of a dummy signal system being used for testing
    static final File DUMMY = new File(new File(PATH, "signals"), "JUnitTestSignals"); // something that won't exist

    static public void createMockSystem() throws IOException {
        // creates mock (no appearances) system
        // in the user area.
        try {
            FileUtil.createDirectory(DUMMY);
            {
                Path inPath = new File(new File(FileUtil.getProgramPath(), "java/test/jmri/implementation"), "testAspects.xml").toPath();
                Path outPath = new File(DUMMY, "aspects.xml").toPath();
                Files.copy(inPath, outPath);
            }
            {
                Path inPath = new File(new File(FileUtil.getProgramPath(), "java/test/jmri/implementation"), "test-appearance-one-searchlight.xml").toPath();
                Path outPath = new File(DUMMY, "appearance-one-searchlight.xml").toPath();
                Files.copy(inPath, outPath);
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
        return DUMMY.getName();
    }

    static public void deleteMockSystem() throws IOException {
        FileUtil.delete(DUMMY);
    }

    static protected Logger log = LoggerFactory.getLogger(SignalSystemTestUtil.class.getName());
}
