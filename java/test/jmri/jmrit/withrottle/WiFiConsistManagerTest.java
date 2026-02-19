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
        JUnitUtil.initTimeProviderManager();
        Assert.assertThrows(NullPointerException.class, () -> new WiFiConsistManager());
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initTimeProviderManager();
        JUnitUtil.initDebugCommandStation();
        cm = new WiFiConsistManager();
    }

    @AfterEach
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
        cm = null;
    }
}
