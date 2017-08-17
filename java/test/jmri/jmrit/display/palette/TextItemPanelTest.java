package jmri.jmrit.display.palette;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.display.EditorScaffold;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

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
        EditorScaffold es = new EditorScaffold();
        jmri.util.ThreadingUtil.runOnGUI(() -> {
            ip = new ItemPalette("Test ItemPalette", null);
            ip.pack();
        });
        TextItemPanel t = new TextItemPanel(ip, "test", es);
        Assert.assertNotNull("exists", t);
        ip.dispose();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        ip = null;
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TextItemPanelTest.class.getName());
}
