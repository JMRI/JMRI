package jmri.jmrix.xpa;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

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
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(XpaSystemConnectionMemoTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
