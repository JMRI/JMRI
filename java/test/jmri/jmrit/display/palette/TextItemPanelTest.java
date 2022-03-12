package jmri.jmrit.display.palette;

import java.awt.GraphicsEnvironment;

import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TextItemPanelTest {

    // allow creation in lambda expression
    private ItemPalette ip = null;

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ControlPanelEditor es = new ControlPanelEditor("EdTextItem");
        jmri.util.ThreadingUtil.runOnGUI(() -> {
            ip = ItemPalette.getDefault("Test ItemPalette", es);
            ip.pack();
        });
        TextItemPanel t = new TextItemPanel(ip, "test");
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(ip);
        JUnitUtil.dispose(es);
   }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        ip = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TextItemPanelTest.class);
}
