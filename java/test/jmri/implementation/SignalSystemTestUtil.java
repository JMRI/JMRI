// SignalSystemTestUtil.java
package jmri.implementation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Static utilities for testing signal system code
 *
 * @author	Bob Jacobsen Copyright 2014, 2015
 * @version	$Revision$
 */
public class SignalSystemTestUtil {

    // Where in user space the "signals" file tree should live
    static final String path = jmri.util.FileUtil.getUserFilesPath() + File.separator + "resources";

    // name of a dummy signal system being used for testing
    static final String dummy = "JUnitTestSignals"; // something that won't exist

    static public void createMockSystem() throws IOException {
        // creates mock (no appearances) system
        // in the user area.
        try {
            new File(path).mkdir(); // might already exist
            new File(path + File.separator + "signals").mkdir();  // already exists if using signals
            new File(path + File.separator + "signals" + File.separator + dummy).mkdir(); // assume doesn't exist, or at least belongs to us
            // copy aspect file
            {
                Path inPath =  FileSystems.getDefault().getPath("java/test/jmri/implementation", "testAspects.xml");
                Path outPath = FileSystems.getDefault().getPath(path + File.separator + "signals" + File.separator + dummy, "aspects.xml");
                Files.copy(inPath, outPath);
            }
            {
                Path inPath =  FileSystems.getDefault().getPath("java/test/jmri/implementation", "test-appearance-one-searchlight.xml");
                Path outPath = FileSystems.getDefault().getPath(path + File.separator + "signals" + File.separator + dummy, "appearance-one-searchlight.xml");
                Files.copy(inPath, outPath);
            }
        } catch (Exception e) {
            log.error("Exception during createMockSystem", e);
            throw e;
        }
        
    }

    static public String getMockUserName() {
        return "JUnit Test Signals"; // from testAspects.xml file
    }

    static public String getMockSystemName() {
        return dummy;
    }

    static public void deleteMockSystem() throws IOException {
        new File(path + File.separator + "signals" + File.separator + dummy + File.separator + "aspects.xml").delete();
        new File(path + File.separator + "signals" + File.separator + dummy + File.separator + "appearance-one-searchlight.xml").delete();
        new File(path + File.separator + "signals" + File.separator + dummy).delete();
    }

    static protected Logger log = LoggerFactory.getLogger(SignalSystemTestUtil.class.getName());
}
