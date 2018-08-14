package jmri.jmrit.display.palette;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author	Bob Jacobsen
 */
public class ItemPaletteTest {

    // allows creation in lamba expressions
    private ItemPalette ip = null;

    @Test
    public void testShow() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        jmri.util.ThreadingUtil.runOnGUI(() -> {
            ip = ItemPalette.getDefault("Test ItemPalette",  new ControlPanelEditor("EdItemPalette"));
            ip.pack();
            ip.setVisible(true);
        });
        JUnitUtil.dispose(ip);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        ip = null;
        JUnitUtil.tearDown();
    }

}
