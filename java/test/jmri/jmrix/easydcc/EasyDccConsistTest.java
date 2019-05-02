package jmri.jmrix.easydcc;

import jmri.DccLocoAddress;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.nce.EasyDccConsist class
 *
 * @author Paul Bender Copyright (C) 2016, 2017
 */

public class EasyDccConsistTest extends jmri.implementation.AbstractConsistTestBase {

    @Test public void testCtor2() {
        // NmraLocoAddress constructor test.
        EasyDccConsist c = new EasyDccConsist(new DccLocoAddress(12, true), _memo);
        Assert.assertNotNull(c);
    }

    @Override
    @Test public void testSetConsistTypeCS(){
        c.setConsistType(jmri.Consist.CS_CONSIST);
        Assert.assertEquals("default consist type",jmri.Consist.CS_CONSIST,c.getConsistType());
    }

    @Test public void checkSizeLimitCS(){
        EasyDccConsist c = new EasyDccConsist(5, _memo);
        c.setConsistType(jmri.Consist.CS_CONSIST);
        Assert.assertEquals("CS Consist Limit",8,c.sizeLimit());   
    } 

    @Test public void checkContainsCS(){
        EasyDccConsist c = new EasyDccConsist(5, _memo);
        c.setConsistType(jmri.Consist.CS_CONSIST);
        jmri.DccLocoAddress A = new jmri.DccLocoAddress(200,true);
        jmri.DccLocoAddress B = new jmri.DccLocoAddress(250,true);
        // nothing added, should be false for all.
        Assert.assertFalse("CS Consist Contains",c.contains(A));   
        Assert.assertFalse("CS Consist Contains",c.contains(B));   
        // add just A
        c.restore(A,true); // use restore here, as it does not send
                           // any data to the command station
        Assert.assertTrue("CS Consist Contains",c.contains(A));   
        Assert.assertFalse("CS Consist Contains",c.contains(B));   
        // then add B
        c.restore(B,false);
        Assert.assertTrue("CS Consist Contains",c.contains(A));   
        Assert.assertTrue("CS Consist Contains",c.contains(B));   
    }

    @Test public void checkGetLocoDirectionCS(){
        EasyDccConsist c = new EasyDccConsist(5, _memo);
        c.setConsistType(jmri.Consist.CS_CONSIST);
        jmri.DccLocoAddress A = new jmri.DccLocoAddress(200,true);
        jmri.DccLocoAddress B = new jmri.DccLocoAddress(250,true);
        c.restore(A,true); // use restore here, as it does not send
                           // any data to the command station
        c.restore(B,false); // reverse direction.
        Assert.assertTrue("Direction in CS Consist",c.getLocoDirection(A));   
        Assert.assertFalse("Direction in CS Consist",c.getLocoDirection(B));   
    }

    private EasyDccSystemConnectionMemo _memo;

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        _memo = new EasyDccSystemConnectionMemo("E", "EasyDCC Test");
        _memo.setEasyDccTrafficController(new EasyDccTrafficControlScaffold(_memo));
        jmri.InstanceManager.setDefault(jmri.CommandStation.class, new EasyDccCommandStation(_memo));
        c = new EasyDccConsist(5, _memo);
    }
   
    @After
    @Override
    public void tearDown() {
        _memo.getTrafficController().terminateThreads();
        _memo = null;
        c = null;
        JUnitUtil.tearDown();
    }

}
