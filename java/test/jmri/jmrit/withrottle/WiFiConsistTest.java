package jmri.jmrit.withrottle;

import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test simple functioning of WiFiConsist
 *
 * @author	Paul Bender Copyright (C) 2016,2017
 */
public class WiFiConsistTest extends jmri.implementation.AbstractConsistTestBase {

    @Override
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initDebugCommandStation();
        jmri.DccLocoAddress addr = new jmri.DccLocoAddress(1234,true);
        c = new WiFiConsist(addr);
    }
    
    @Override
    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }
}
