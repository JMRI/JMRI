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
public class PanelProDanielTest {

    @Test
    public void testDaniel() {
        System.out.format("User files path: %s%n", FileUtil.getUserFilesPath());
        System.out.format("Program path: %s%n", FileUtil.getProgramPath());
        System.out.format("Property user.dir: %s%n", System.getProperty("user.dir"));
        System.out.format("xmlDir path: %s%n", XmlFile.xmlDir());
        System.out.format("DecoderFile location path: %s%n", DecoderFile.fileLocation);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
