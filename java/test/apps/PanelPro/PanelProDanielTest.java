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
 * This is more of an acceptance test than a unit test, loading a series
 * of connection user profiles in PanelPro.
 * <p>
 * It confirms that the entire application can start up and configure itself.
 * <p>
 * When format of user configuration (profile) files is changed, check the
 * sets in java/test/apps/PanelPro/profiles/ to match or allow for conversion
 * dialogs.
 * Also check the required TESTMAXTIME in {@link apps.LaunchJmriAppBase} to
 * prevent timeouts on app startup tests if structure of data develops.
 * 
 * @author Paul Bender Copyright (C) 2017, 2019
 * @author Bob Jacobsen Copyright (C) 2017
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
