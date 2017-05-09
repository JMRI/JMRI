package jmri.jmrix.debugthrottle;

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
public class DebugThrottleTest {

    @Test
    public void testCTor() {
        jmri.jmrix.SystemConnectionMemo memo = new jmri.jmrix.SystemConnectionMemo("T","Test"){
           @Override
           protected java.util.ResourceBundle getActionModelResourceBundle(){
              return null;
           }
        };

        DebugThrottle t = new DebugThrottle(new jmri.DccLocoAddress(100,true),memo);
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

    private final static Logger log = LoggerFactory.getLogger(DebugThrottleTest.class.getName());

}
