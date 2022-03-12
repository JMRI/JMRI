package jmri.jmrix.cmri.serial.cmrinetmetrics;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

import java.awt.GraphicsEnvironment;

/**
 * Test simple functioning of CMRInetMetricsCollector
 *
 * @author Chuck Catania Copyright (C) 2017, 2018
 */
public class CMRInetMetricsCollectorTest {

    @Test
    public void testStringCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CMRInetMetricsCollector action = new CMRInetMetricsCollector(); 
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CMRInetMetricsCollector action = new CMRInetMetricsCollector(); 
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
