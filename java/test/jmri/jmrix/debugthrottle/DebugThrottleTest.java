package jmri.jmrix.debugthrottle;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DebugThrottleTest.class);

}
