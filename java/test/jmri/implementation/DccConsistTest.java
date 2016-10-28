package jmri.implementation;

import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import jmri.DccLocoAddress;


/**
 * Test simple functioning of DccConsist
 *
 * @author	Paul Copyright (C) 2011, 2016
 */
public class DccConsistTest{

    @Test public void testCtor() {
        // NmraLocoAddress constructor test.
        DccConsist c = new DccConsist(new DccLocoAddress(12, true));
        Assert.assertNotNull(c);
    }

    @Test public void testCtor2() {
        // integer constructor test.
        DccConsist c = new DccConsist(12);
        Assert.assertNotNull(c);
    }

    @Ignore("Not quite ready yet")
    @Test(expected=java.lang.NullPointerException.class) 
    public void checkDisposeMethod(){
        DccConsist c =  new DccConsist(5);
        jmri.DccLocoAddress A = new jmri.DccLocoAddress(200,true);
        jmri.DccLocoAddress B = new jmri.DccLocoAddress(250,true);
        c.restore(A,true); // use restore here, as it does not send
                           // any data to the command station
        c.restore(B,false); // use restore here, as it does not send
                           // any data to the command station
        // before dispose, this should succeed.
        Assert.assertTrue("Advanced Consist Contains",c.contains(A));
        Assert.assertTrue("Advanced Consist Contains",c.contains(B));
        c.dispose();
        // after dispose, this should fail
        Assert.assertTrue("Advanced Consist Contains",c.contains(A));
        Assert.assertTrue("Advanced Consist Contains",c.contains(B));
    }

    @Test public void testGetConsistType(){
        DccConsist c = new DccConsist(5);
        Assert.assertEquals("default consist type",jmri.Consist.ADVANCED_CONSIST,c.getConsistType());
    }

    @Test public void testSetConsistTypeAdvanced(){
        DccConsist c = new DccConsist(5);
        c.setConsistType(jmri.Consist.ADVANCED_CONSIST);
        Assert.assertEquals("default consist type",jmri.Consist.ADVANCED_CONSIST,c.getConsistType());
    }

    @Test public void testSetConsistTypeCS(){
        DccConsist c = new DccConsist(5);
        c.setConsistType(jmri.Consist.CS_CONSIST);
        // make sure an error message is generated.
        jmri.util.JUnitAppender.assertErrorMessage("Consist Type Not Supported");
    }

    @Test public void checkAddressAllowedGood(){
        DccConsist c = new DccConsist(5);
        Assert.assertTrue("AddressAllowed", c.isAddressAllowed(new jmri.DccLocoAddress(200,true)));
    }

    @Test public void checkAddressAllowedBad(){
        DccConsist c = new DccConsist(5);
        Assert.assertFalse("AddressAllowed", c.isAddressAllowed(new jmri.DccLocoAddress(0,false)));
    }

    @Test public void checkSizeLimitAdvanced(){
        DccConsist c = new DccConsist(5);
        c.setConsistType(jmri.Consist.ADVANCED_CONSIST);
        Assert.assertEquals("Advanced Consist Limit",-1,c.sizeLimit());   
    } 

    @Test public void checkContainsAdvanced(){
        DccConsist c = new DccConsist(5);
        c.setConsistType(jmri.Consist.ADVANCED_CONSIST);
        jmri.DccLocoAddress A = new jmri.DccLocoAddress(200,true);
        jmri.DccLocoAddress B = new jmri.DccLocoAddress(250,true);
        // nothing added, should be false for all.
        Assert.assertFalse("Advanced Consist Contains",c.contains(A));   
        Assert.assertFalse("Advanced Consist Contains",c.contains(B));   
        // add just A
        c.restore(A,true); // use restore here, as it does not send
                           // any data to the command station
        Assert.assertTrue("Advanced Consist Contains",c.contains(A));   
        Assert.assertFalse("Advanced Consist Contains",c.contains(B));   
        // then add B
        c.restore(B,false);
        Assert.assertTrue("Advanced Consist Contains",c.contains(A));   
        Assert.assertTrue("Advanced Consist Contains",c.contains(B));   
    }

    @Test public void checkGetLocoDirectionAdvanced(){
        DccConsist c = new DccConsist(5);
        c.setConsistType(jmri.Consist.ADVANCED_CONSIST);
        jmri.DccLocoAddress A = new jmri.DccLocoAddress(200,true);
        jmri.DccLocoAddress B = new jmri.DccLocoAddress(250,true);
        c.restore(A,true); // use restore here, as it does not send
                           // any data to the command station
        c.restore(B,false); // revese direction.
        Assert.assertTrue("Direction in Advanced Consist",c.getLocoDirection(A));   
        Assert.assertFalse("Direction in Advanced Consist",c.getLocoDirection(B));   
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
    }
   
    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
