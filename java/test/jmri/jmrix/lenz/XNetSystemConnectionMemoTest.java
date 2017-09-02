package jmri.jmrix.lenz;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * XNetSystemConnectionMemoTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.XNetSystemConnectionMemo class
 *
 * @author	Paul Bender
 */
public class XNetSystemConnectionMemoTest extends TestCase {

    public void testCtor() {
        // infrastructure objects
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());

        XNetSystemConnectionMemo t = new XNetSystemConnectionMemo(tc);
        Assert.assertNotNull(t);
        Assert.assertNotNull(t.getXNetTrafficController());
        // While we are constructing the memo, we should also set the 
        // SystemMemo parameter in the traffic controller.
        Assert.assertNotNull(tc.getSystemConnectionMemo());
    }

    public void testXNetTrafficControllerSetCtor() {
        // infrastructure objects
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());

        XNetSystemConnectionMemo t = new XNetSystemConnectionMemo();
        Assert.assertNotNull(t);
        // the default constructor does not set the traffic controller
        Assert.assertNull(t.getXNetTrafficController());
        // so we need to do this ourselves.
        t.setXNetTrafficController(tc);
        Assert.assertNotNull(t.getXNetTrafficController());
        // and while we're doing that, we should also set the SystemMemo 
        // parameter in the traffic controller.
        Assert.assertNotNull(tc.getSystemConnectionMemo());
    }

    public void testProivdesConsistManagerMultiMaus() {
        // infrastructure objects
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation(){
          @Override
          public int getCommandStationType(){
              return(0x10); // MultiMaus
          }
        });

        XNetSystemConnectionMemo t = new XNetSystemConnectionMemo();
        t.setXNetTrafficController(tc);
        t.setCommandStation(tc.getCommandStation());
        Assert.assertFalse(t.provides(jmri.ConsistManager.class));
    }

    public void testProivdesConsistManagerLZV100() {
        // infrastructure objects
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation(){
          @Override
          public int getCommandStationType(){
              return(0x00); // LZV100
          }
        });

        XNetSystemConnectionMemo t = new XNetSystemConnectionMemo();
        t.setXNetTrafficController(tc);
        t.setCommandStation(tc.getCommandStation());
        Assert.assertTrue(t.provides(jmri.ConsistManager.class));
    }

    // from here down is testing infrastructure
    public XNetSystemConnectionMemoTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", XNetSystemConnectionMemoTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(XNetSystemConnectionMemoTest.class);
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
