package jmri.jmrix.lenz;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * XNetConsistTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.XNetConsist class
 *
 * @author	Paul Bender
 */
public class XNetConsistTest {

    // infrastructure objects, populated by setUp.
    private XNetInterfaceScaffold tc = null;
    private XNetSystemConnectionMemo memo = null;

    @Test public void integerConstructorTest() {
        XNetConsist c = new XNetConsist(5, tc, memo);
        Assert.assertNotNull(c);
    }

    @Test public void dccLocoAddressConstructorTest() {
        jmri.DccLocoAddress addr = new jmri.DccLocoAddress(5,false);

        XNetConsist c = new XNetConsist(addr, tc, memo);
        Assert.assertNotNull(c);
    }

    @Test public void checkDisposeMethod(){
        XNetConsist c =  new XNetConsist(5,tc,memo);
        // verify that c has been added to the traffic controller's 
        // list of listeners.
        int listeners = tc.numListeners();
        c.dispose();
        Assert.assertEquals("dispose check",listeners -1, tc.numListeners()); 
    }

    @Test public void testGetConsistType(){
        XNetConsist c = new XNetConsist(5, tc, memo);
        Assert.assertEquals("default consist type",jmri.Consist.ADVANCED_CONSIST,c.getConsistType());
    }

    @Test public void testSetConsistTypeAdvanced(){
        XNetConsist c = new XNetConsist(5, tc, memo);
        c.setConsistType(jmri.Consist.ADVANCED_CONSIST);
        Assert.assertEquals("default consist type",jmri.Consist.ADVANCED_CONSIST,c.getConsistType());
    }

    @Test public void testSetConsistTypeCS(){
        XNetConsist c = new XNetConsist(5, tc, memo);
        c.setConsistType(jmri.Consist.CS_CONSIST);
        Assert.assertEquals("default consist type",jmri.Consist.CS_CONSIST,c.getConsistType());
    }

    @Test public void testSetConsistTypeOther(){
        XNetConsist c = new XNetConsist(5, tc, memo);
        c.setConsistType(255);
        // make sure an error message is generated.
        jmri.util.JUnitAppender.assertErrorMessage("Consist Type Not Supported");
    }

    @Test public void checkAddressAllowedGood(){
        XNetConsist c = new XNetConsist(5, tc, memo);
        Assert.assertTrue("AddressAllowed", c.isAddressAllowed(new jmri.DccLocoAddress(200,true)));
    }

    @Test public void checkAddressAllowedBad(){
        XNetConsist c = new XNetConsist(5, tc, memo);
        Assert.assertFalse("AddressAllowed", c.isAddressAllowed(new jmri.DccLocoAddress(0,false)));
    }

    @Test public void checkSizeLimitAdvanced(){
        XNetConsist c = new XNetConsist(5, tc, memo);
        c.setConsistType(jmri.Consist.ADVANCED_CONSIST);
        Assert.assertEquals("Advanced Consist Limit",-1,c.sizeLimit());   
    } 

    @Test public void checkSizeLimitCS(){
        XNetConsist c = new XNetConsist(5, tc, memo);
        c.setConsistType(jmri.Consist.CS_CONSIST);
        Assert.assertEquals("CS Consist Limit",2,c.sizeLimit());   
    } 

    @Ignore("Can't directly set consist type to something that is not supported")
    @Test public void checkSizeLimitOther(){
        XNetConsist c = new XNetConsist(5, tc, memo);
        c.setConsistType(255);
        Assert.assertEquals("Other Consist Limit",0,c.sizeLimit());   
    } 

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        tc = new XNetInterfaceScaffold(new LenzCommandStation());
        memo = new XNetSystemConnectionMemo(tc);
    }
   
    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
        tc=null;
        memo=null;
    }

}
