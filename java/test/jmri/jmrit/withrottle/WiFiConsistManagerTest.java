package jmri.jmrit.withrottle;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of WiFiConsistManager
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class WiFiConsistManagerTest extends jmri.implementation.AbstractConsistManagerTestBase {

    @Test
    public void testCTorThrowsNPE() {
        JUnitUtil.resetInstanceManager();
        Assert.assertThrows(NullPointerException.class, () -> new WiFiConsistManager());
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initDebugCommandStation();
        cm = new WiFiConsistManager();
    }
    
    @AfterEach
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
        cm = null;
    }
}
