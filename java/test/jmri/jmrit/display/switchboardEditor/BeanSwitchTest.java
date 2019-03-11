package jmri.jmrit.display.switchboardEditor;

import org.junit.*;
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
        jmri.util.JUnitUtil.resetProfileManager();
        if (!GraphicsEnvironment.isHeadless()) {
            swe = new SwitchboardEditor("Bean Switch Test Layout");
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
