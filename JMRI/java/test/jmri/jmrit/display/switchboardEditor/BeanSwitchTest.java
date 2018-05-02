package jmri.jmrit.display.switchboardEditor;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import java.awt.GraphicsEnvironment;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class BeanSwitchTest {

    private SwitchboardEditor swe = null;

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        BeanSwitch t = new BeanSwitch(1,null,"IT1",0,swe);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            swe = new SwitchboardEditor("Test Layout");
        }
    }

    @After
    public void tearDown() {
        if (swe != null) {
            jmri.util.JUnitUtil.dispose(swe);
            swe = null;
        }
        jmri.util.JUnitUtil.tearDown();
    }

}
