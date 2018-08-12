package jmri.implementation;

import jmri.DccLocoAddress;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
abstract public class AbstractConsistManagerTestBase {

    protected jmri.ConsistManager cm = null;

    // implementing classes should set cm to a valid value in setUp and 
    // cleanup in tearDown.
    @Before
    abstract public void setUp();
    @After
    abstract public void tearDown();


    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",cm);
    }

    @Test
    public void testGetConsist(){
        // getConsist with a valid address should always return
        // a consist.
        DccLocoAddress addr = new DccLocoAddress(5,false);
        Assert.assertNotNull("add consist",cm.getConsist(addr));
        Assert.assertEquals("list has 1 entry",1,cm.getConsistList().size());
    }

    @Test
    public void testGetConsistListEmpty(){
        // by default, there should be no consists
        Assert.assertNotNull("list exists",cm.getConsistList());
        Assert.assertTrue("empty list",cm.getConsistList().isEmpty());
    }

    @Test
    public void testDelConsist(){
        DccLocoAddress addr = new DccLocoAddress(5,false);
        cm.getConsist(addr);
        int size = cm.getConsistList().size();
        cm.delConsist(addr);
        Assert.assertEquals("list has (size-1) entries",size-1,cm.getConsistList().size());
    }

    @Test
    public void testIsCommandStationConsistPossible(){
       // default is false, override if necessary
       Assert.assertFalse("CS Consist Possible",cm.isCommandStationConsistPossible());
    }

    @Test
    public void tesCsConsistNeedsSeperateAddress(){
       Assume.assumeTrue(cm.isCommandStationConsistPossible());
       // default is false, override if necessary
       Assert.assertFalse("CS Consist Needs Seperate Address",cm.csConsistNeedsSeperateAddress());
    }

    // private final static Logger log = LoggerFactory.getLogger(AbstractConsistManagerTestBase.class);

}
