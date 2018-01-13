package jmri.jmrit.withrottle;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of WiFiConsistManager
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class WiFiConsistManagerTest extends jmri.implementation.AbstractConsistManagerTestBase {

    @Test(expected=NullPointerException.class)
    public void testCTorThrowsNPE() {
        JUnitUtil.setUp();
        cm = new WiFiConsistManager();
    }

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initDebugCommandStation();
        cm = new WiFiConsistManager();
    }
    
    @After
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
        cm = null;
    }
}
