package jmri.util.swing.multipane;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ThreePaneTLRWindowTest {

    @Test
    @DisabledIfHeadless
    public void testCTor() {
        ThreePaneTLRWindow t = new ThreePaneTLRWindow("Test of empty Three Pane TLR Window",
                "xml/config/apps/panelpro/Gui3Menus.xml",
                "xml/config/apps/panelpro/Gui3MainToolBar.xml"
        );
        Assertions.assertNotNull( t, "exists");
        JUnitUtil.dispose(t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ThreePaneTLRWindowTest.class.getName());

}
