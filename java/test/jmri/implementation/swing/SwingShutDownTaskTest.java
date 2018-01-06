package jmri.implementation.swing;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import java.awt.GraphicsEnvironment;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class SwingShutDownTaskTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SwingShutDownTask t = new SwingShutDownTask("SwingShutDownTask Window Check",
                "Do Something quits, click Continue Qutting to quit, Cancel Quit to continue",
                "Do Something and Stop",
                null) {
                    @Override
                    public boolean checkPromptNeeded() {
                        return false;
                    }
                };
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

}
