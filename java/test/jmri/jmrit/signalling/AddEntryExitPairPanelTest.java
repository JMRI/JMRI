package jmri.jmrit.signalling;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.GraphicsEnvironment;
import jmri.jmrit.display.layoutEditor.LayoutEditor;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class AddEntryExitPairPanelTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        AddEntryExitPairPanel t = new AddEntryExitPairPanel(e);
        Assert.assertNotNull("exists",t);
        e.dispose();
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

    private final static Logger log = LoggerFactory.getLogger(AddEntryExitPairPanelTest.class.getName());

}
