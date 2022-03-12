package jmri.util.swing.multipane;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ThreePaneTLRWindowTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThreePaneTLRWindow t = new ThreePaneTLRWindow("Test of empty Three Pane TLR Window",
                "xml/config/apps/panelpro/Gui3Menus.xml",
                "xml/config/apps/panelpro/Gui3MainToolBar.xml"
        );
        Assert.assertNotNull("exists",t);
        jmri.util.JUnitUtil.dispose(t);
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ThreePaneTLRWindowTest.class.getName());

}
