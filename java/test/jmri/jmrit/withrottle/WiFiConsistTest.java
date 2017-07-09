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
        jmri.DccLocoAddress addr = new jmri.DccLocoAddress(1234,true);
        c = new WiFiConsist(addr);
    }
    
    @Override
    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }
}
