package jmri.jmrit.display.palette;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrit.display.EditorScaffold;
import jmri.util.JmriJFrame;
import java.awt.GraphicsEnvironment;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class DetectionPanelTest {

    private ItemPalette ip = null;

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        EditorScaffold es = new EditorScaffold();
        jmri.util.ThreadingUtil.runOnGUI(() -> {
            ip = new ItemPalette("Test ItemPalette", null);
            ip.pack();
        });
        TextItemPanel tip = new TextItemPanel(ip,"test",es);
        DetectionPanel t = new DetectionPanel(tip);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(DetectionPanelTest.class.getName());

}
