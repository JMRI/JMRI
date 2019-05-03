package jmri.jmrix.cmri.serial.cmrinetmetrics;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import java.awt.GraphicsEnvironment;

/**
 * Test simple functioning of CMRInetMetricsData
 *
 * @author	Chuck Catania Copyright (C) 2017, 2018
 */
public class CMRInetMetricsDataTest {

    @Test
    public void testMemoCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CMRInetMetricsData action = new CMRInetMetricsData(); 
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
