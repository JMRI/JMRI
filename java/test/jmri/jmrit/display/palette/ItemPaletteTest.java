package jmri.jmrit.display.palette;

import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 *
 * @author Bob Jacobsen
 */
public class ItemPaletteTest {

    // allows creation in lambda expressions
    private ItemPalette ip = null;

    @Test
    @DisabledIfHeadless
    public void testShow() {

        ControlPanelEditor editor = new ControlPanelEditor("EdItemPalette");
        jmri.util.ThreadingUtil.runOnGUI(() -> {
            ip = ItemPalette.getDefault("Test ItemPalette", editor);
            Assertions.assertNotNull(ip);
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
        JUnitUtil.tearDown();
    }

}
