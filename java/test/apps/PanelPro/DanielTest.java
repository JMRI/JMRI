package apps.PanelPro;

import jmri.jmrit.XmlFile;
import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.util.FileUtil;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Show some paths
 * 
 * @author Daniel Bergqvisr Copyright (C) 2020
 */
public class DanielTest {

    @Test
    public void testSystemOut() {
        // System.out must not be used in JMRI, but is very useful during debugging.
        System.out.format("User files path: %s%n", FileUtil.getUserFilesPath());
        System.out.format("Program path: %s%n", FileUtil.getProgramPath());
        System.out.format("Property user.dir: %s%n", System.getProperty("user.dir"));
        System.out.format("xmlDir path: %s%n", XmlFile.xmlDir());
        System.out.format("DecoderFile location path: %s%n", DecoderFile.fileLocation);
    }

    @Test
    public void testPaths() {
        log.error("User files path: " + FileUtil.getUserFilesPath());
        log.error("Program path: " + FileUtil.getProgramPath());
        log.error("Property user.dir: " + System.getProperty("user.dir"));
        log.error("xmlDir path: " + XmlFile.xmlDir());
        log.error("DecoderFile location path: " + DecoderFile.fileLocation);
    }

    @Test
    public void testException() {
        throw new RuntimeException("Daniel");
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DanielTest.class);
}
