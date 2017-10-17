package jmri.jmrix;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class AbstractThrottleManagerTest {

    @Test
    public void testCTor() {
        AbstractThrottleManager t = new AbstractThrottleManager(new SystemConnectionMemo("T","Test"){
           @Override
           protected java.util.ResourceBundle getActionModelResourceBundle(){
              return null;
           }
        }){
           @Override
           public void requestThrottleSetup(jmri.LocoAddress a, boolean control){
           }
           @Override
           public boolean addressTypeUnique(){
              return false;
           }
           @Override
           public boolean canBeShortAddress(int address){
              return true;
           }
           @Override
           public boolean canBeLongAddress(int address){
              return true;
           }
        };
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

    // private final static Logger log = LoggerFactory.getLogger(AbstractThrottleManagerTest.class);

}
