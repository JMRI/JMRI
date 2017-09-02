package jmri.jmrix.marklin;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(MarklinThrottleTest.class.getName());

}
