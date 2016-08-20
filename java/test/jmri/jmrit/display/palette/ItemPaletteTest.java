package jmri.jmrit.display.palette;

import java.beans.PropertyChangeEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 * @author	Bob Jacobsen
 */
public class ItemPaletteTest extends jmri.util.SwingTestCase {

    ItemPalette ip;
    
    public void testShow() {
        jmri.util.ThreadingUtil.runOnGUI(() -> {
            ip = new ItemPalette("Test ItemPalette", null);
            ip.pack();
        });
        ip.setVisible(true);
    }


    // from here down is testing infrastructure
    public ItemPaletteTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", ItemPaletteTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(ItemPaletteTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

	// static private Logger log = LoggerFactory.getLogger(ItemPaletteTest.class.getName());
}
