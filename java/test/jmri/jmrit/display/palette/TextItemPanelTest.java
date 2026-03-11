package jmri.jmrit.display.palette;

import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TextItemPanelTest {

    // allow creation in lambda expression
    private ItemPalette ip = null;

    @Test
    @DisabledIfHeadless
    public void testCTor() {
        ControlPanelEditor es = new ControlPanelEditor("EdTextItem");
        jmri.util.ThreadingUtil.runOnGUI(() -> {
            ip = ItemPalette.getDefault("Test ItemPalette", es);
            ip.pack();
        });
        TextItemPanel t = new TextItemPanel(ip, "test");
        Assertions.assertNotNull(t, "exists");
        JUnitUtil.dispose(ip);
        JUnitUtil.dispose(es);
   }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        ip = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TextItemPanelTest.class);
}
