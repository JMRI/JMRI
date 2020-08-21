package jmri.jmrit.display.switchboardEditor;

import jmri.jmrit.display.EditorFrameOperator;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;
import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        if (!GraphicsEnvironment.isHeadless()) {
            swe = new SwitchboardEditor("Bean Switch Test Layout");
        }
    }

    @AfterEach
    public void tearDown() {
        if (swe != null) {
            new EditorFrameOperator(swe.getTargetFrame()).closeFrameWithConfirmations();
            swe = null;
        }
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
