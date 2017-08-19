package jmri.jmrix.rps;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class RpsBlockTest {

    @Test
    public void testCTor() {
        RpsSensor s = new RpsSensor("RS(0,0,0);(1,0,0);(1,1,0);(0,1,0)");
        jmri.SignalHead sh = new jmri.implementation.VirtualSignalHead("TS1","test signal head");
        RpsBlock t = new RpsBlock(s,sh,0.0f,1.0f);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(RpsBlockTest.class.getName());

}
