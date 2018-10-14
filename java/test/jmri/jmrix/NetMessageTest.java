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
public class NetMessageTest {

    @Test
    public void testCTor() {
        NetMessage t = new NetMessage(5){
           @Override
           public boolean checkParity(){
               return false;
           }
           @Override
           public void setParity(){
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

    // private final static Logger log = LoggerFactory.getLogger(NetMessageTest.class);

}
