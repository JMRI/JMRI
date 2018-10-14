package jmri.jmrix.cmri.serial.cmrinetmanager;

import jmri.util.JUnitUtil;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import java.awt.GraphicsEnvironment;

/**
 * Test simple functioning of CMRInetMetricsFrame
 *
 * @author	Chuck Catania Copyright (C) 2017, 2018
 */
public class CMRInetMetricsFrameTest {

    @Test
    public void testMemoCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CMRInetMetricsFrame action = new CMRInetMetricsFrame(new CMRISystemConnectionMemo()); 
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
