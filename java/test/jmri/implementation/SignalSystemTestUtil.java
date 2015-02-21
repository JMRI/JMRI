// SignalSystemTestUtil.java
package jmri.implementation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Static utilities for testing signal system code
 *
 * @author	Bob Jacobsen Copyright 2014
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
        InputStream in = null;
        OutputStream out = null;
        try {
            new File(path).mkdir(); // might already exist
            new File(path + File.separator + "signals").mkdir();  // already exists if using signals
            new File(path + File.separator + "signals" + File.separator + dummy).mkdir(); // assume doesn't exist, or at least belongs to us
            // copy aspect file
            {
                in = new FileInputStream(new File("java/test/jmri/implementation/testAspects.xml"));
                out = new FileOutputStream(new File(path + File.separator + "signals" + File.separator + dummy + File.separator + "aspects.xml"));
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
            {
                in = new FileInputStream(new File("java/test/jmri/implementation/test-appearance-one-searchlight.xml"));
                out = new FileOutputStream(new File(path + File.separator + "signals" + File.separator + dummy + File.separator + "appearance-one-searchlight.xml"));
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }

        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
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

}
