package jmri.util.swing.multipane;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PanedInterfaceTest {

    @Test
    @DisabledIfHeadless
    public void testCTor() {
        MultiPaneWindow w = new MultiPaneWindow("Test of empty Multi Pane Window",
                "xml/config/apps/panelpro/Gui3LeftTree.xml",
                "xml/config/apps/panelpro/Gui3Menus.xml",
                "xml/config/apps/panelpro/Gui3MainToolBar.xml"
        );
        PanedInterface t = new PanedInterface(w);
        Assertions.assertNotNull( t, "exists");
        JUnitUtil.dispose(w);
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

    // private final static Logger log = LoggerFactory.getLogger(PanedInterfaceTest.class.getName());

}
