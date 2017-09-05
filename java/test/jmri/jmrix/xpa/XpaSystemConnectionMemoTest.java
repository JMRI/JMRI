package jmri.jmrix.xpa;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * Description:	tests for the jmri.jmrix.xpa.XpaSystemConnectionMemo class
 * <P>
 * @author	Paul Bender
 */
public class XpaSystemConnectionMemoTest extends TestCase {

    public void testCtor() {
        XpaSystemConnectionMemo t = new XpaSystemConnectionMemo();
        Assert.assertNotNull(t);
    }

    public void testGetandSetXpaTrafficController(){
        XpaSystemConnectionMemo t = new XpaSystemConnectionMemo(); 
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


    // from here down is testing infrastructure
    public XpaSystemConnectionMemoTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", XpaSystemConnectionMemoTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(XpaSystemConnectionMemoTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        JUnitUtil.setUp();
    }

    @Override
    protected void tearDown() {
        JUnitUtil.tearDown();
    }

}
