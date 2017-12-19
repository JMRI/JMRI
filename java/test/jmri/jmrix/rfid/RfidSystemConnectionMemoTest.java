package jmri.jmrix.rfid;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * RfidSystemConnectionMemoTest.java
 *
 * Description:	tests for the jmri.jmrix.rfid.RfidSystemConnectionMemo class
 *
 * @author	Paul Bender
 */
public class RfidSystemConnectionMemoTest extends jmri.jmrix.SystemConnectionMemoTestBase {

    @Override
    @Test
    public void testProvidesConsistManager(){
       Assert.assertFalse("Provides ConsistManager",scm.provides(jmri.ConsistManager.class));
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp(){
        JUnitUtil.setUp();
        scm=new RfidSystemConnectionMemo();
    }

    @Override
    @After
    public void tearDown(){
        JUnitUtil.tearDown();
    }

}
