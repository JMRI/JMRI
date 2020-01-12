package jmri.jmrix.xpa;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.xpa.XpaSystemConnectionMemo class.
 *
 * @author	Paul Bender
 */
public class XpaSystemConnectionMemoTest extends jmri.jmrix.SystemConnectionMemoTestBase {

    @Test
    public void testGetandSetXpaTrafficController(){
        XpaSystemConnectionMemo t = (XpaSystemConnectionMemo) scm; 
       // first, check to see that an exception is 
       // thrown when null is passed. 
       boolean exceptionThrown = false;
       try {
         t.setXpaTrafficController(null);
       } catch(java.lang.IllegalArgumentException iae){
         exceptionThrown = true;
       }
       Assert.assertTrue(exceptionThrown);

       t.setXpaTrafficController(new XpaTrafficController());

       Assert.assertNotNull("TrafficController set correctly",t.getXpaTrafficController());    

    }

    @Override
    @Test
    public void testProvidesConsistManager(){
       Assert.assertFalse("Provides ConsistManager",scm.provides(jmri.ConsistManager.class));
    }


    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        XpaTrafficController tc = new XpaTrafficControlScaffold();
        XpaSystemConnectionMemo memo = new XpaSystemConnectionMemo();
        memo.setXpaTrafficController(tc);
        scm = memo;
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
