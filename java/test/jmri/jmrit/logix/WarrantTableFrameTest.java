package jmri.jmrit.logix;

import java.awt.GraphicsEnvironment;
import java.io.File;

import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.util.JUnitUtil;
import jmri.util.junit.rules.RetryRule;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class WarrantTableFrameTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        WarrantTableFrame t = WarrantTableFrame.getDefault();
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(WarrantTableFrameTest.class);

}
