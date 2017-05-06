package jmri.jmrix.marklin;

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
public class MarklinThrottleTest {

    @Test
    public void testCTor() {
        MarklinTrafficController tc = new MarklinTrafficController(){
           @Override
           public void sendMarklinMessage(MarklinMessage m, MarklinListener reply) {
           }
        };
        MarklinSystemConnectionMemo c = new MarklinSystemConnectionMemo(tc){
          @Override
          public MarklinThrottleManager getThrottleManager() {
             return new MarklinThrottleManager(this);
          }
        };
        MarklinThrottle t = new MarklinThrottle(c,new jmri.DccLocoAddress(42,false));
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

    private final static Logger log = LoggerFactory.getLogger(MarklinThrottleTest.class.getName());

}
