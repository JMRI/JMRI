package jmri.jmrix.rfid.generic.standalone;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * StandaloneTrafficControllerTest.java
 *
 * Description:	tests for the StandaloneTrafficController class
 *
 * @author	Paul Bender Copyright (C) 2012,2016
 */
public class StandaloneTrafficControllerTest {

    StandaloneTrafficController tc = null;

    @Test
    public void testCtor(){
        Assert.assertNotNull(tc);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        tc = new StandaloneTrafficController(new StandaloneSystemConnectionMemo(){
        });
    }

    @After
    public void tearDown() {
        tc = null;
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }
}
