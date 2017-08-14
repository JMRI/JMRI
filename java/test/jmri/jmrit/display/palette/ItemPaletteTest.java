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

    @Test
    public void testShow() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ItemPalette ip = new ItemPalette("Test ItemPalette", null);
        jmri.util.ThreadingUtil.runOnGUI(() -> {
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
        apps.tests.Log4JFixture.tearDown();
    }

}
