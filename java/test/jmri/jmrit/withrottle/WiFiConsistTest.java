package jmri.jmrit.withrottle;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * Test simple functioning of WiFiConsist
 *
 * @author	Paul Bender Copyright (C) 2016,2017
 */
public class WiFiConsistTest extends jmri.implementation.AbstractConsistTestBase {

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initDebugCommandStation();
        jmri.DccLocoAddress addr = new jmri.DccLocoAddress(1234, true);
        c = new WiFiConsist(addr);
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
