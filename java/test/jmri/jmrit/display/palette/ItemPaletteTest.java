package jmri.jmrit.display.palette;

import java.awt.GraphicsEnvironment;

import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Bob Jacobsen
 */
public class ItemPaletteTest {

    // allows creation in lambda expressions
    private ItemPalette ip = null;

    @Test
    public void testShow() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        ControlPanelEditor editor = new ControlPanelEditor("EdItemPalette");
        jmri.util.ThreadingUtil.runOnGUI(() -> {
            ip = ItemPalette.getDefault("Test ItemPalette", editor);
            assert ip != null;
            ip.pack();
            ip.setVisible(true);
        });
        JUnitUtil.dispose(ip);
        JUnitUtil.dispose(editor);
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
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
