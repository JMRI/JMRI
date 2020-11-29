package jmri.jmrix.cmri.serial.cmrinetmetrics;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

import java.awt.GraphicsEnvironment;

/**
 * Test simple functioning of CMRInetMetricsData
 *
 * @author Chuck Catania Copyright (C) 2017, 2018
 */
public class CMRInetMetricsDataTest {

    @Test
    public void testMemoCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CMRInetMetricsData action = new CMRInetMetricsData(); 
        Assert.assertNotNull("exists", action);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
