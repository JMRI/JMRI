package jmri.jmrit.operations.setup;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.GraphicsEnvironment;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class EditSwitchListTextFrameTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        EditSwitchListTextFrame t = new EditSwitchListTextFrame();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testEditSwitchListTextFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        EditSwitchListTextFrame f = new EditSwitchListTextFrame();
        f.setLocation(0, 0); // entire panel must be visible for tests to work properly
        f.initComponents();

        // TODO do more testing

        // done
        f.dispose();
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

    private final static Logger log = LoggerFactory.getLogger(EditSwitchListTextFrameTest.class.getName());

}
