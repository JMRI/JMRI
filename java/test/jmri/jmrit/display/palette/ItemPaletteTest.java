package jmri.jmrit.display.palette;

import java.awt.GraphicsEnvironment;
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
            ip = new ItemPalette("Test ItemPalette", null);
            ip.pack();
            ip.setVisible(true);
        });
        ip.dispose();
    }

    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        ip = null;
        apps.tests.Log4JFixture.tearDown();
    }

}
