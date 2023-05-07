package jmri.jmrix.cmri.serial.cmrinetmetrics;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Test simple functioning of CMRInetMetricsData
 *
 * @author Chuck Catania Copyright (C) 2017, 2018
 */
public class CMRInetMetricsDataTest {

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testCMRInetMetricsDataMemoCtor() {
        CMRInetMetricsData action = new CMRInetMetricsData(); 
        Assertions.assertNotNull(action, "exists" );
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
