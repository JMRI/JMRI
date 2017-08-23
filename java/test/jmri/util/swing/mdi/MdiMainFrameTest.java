package jmri.util.swing.mdi;

import java.awt.GraphicsEnvironment;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author	Bob Jacobsen Copyright 2003, 2010
 */
public class MdiMainFrameTest{

    @Test
    public void testShow() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        MdiMainFrame f = new MdiMainFrame("Test of MDI Frame",
                "java/test/jmri/util/swing/xml/Gui3LeftTree.xml",
                "java/test/jmri/util/swing/xml/Gui3Menus.xml",
                "java/test/jmri/util/swing/xml/Gui3MainToolBar.xml"
        );
        f.setSize(new java.awt.Dimension(400, 400));
        f.setVisible(true);

        // close
        f.dispose();
    }

    @Before
    public void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
