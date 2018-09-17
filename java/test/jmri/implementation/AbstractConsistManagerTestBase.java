package jmri.implementation;

import java.util.concurrent.atomic.AtomicBoolean;
import jmri.ConsistListener;
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
    
    // Some tests may not work correctly. For example, JsonConsistManagerTest
    // doesn't work correctly if variable 'manager' is null.
    protected boolean ignoreTests = false;

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
    public void testConsists() {
        Assume.assumeFalse(ignoreTests);
        
        DccLocoAddress locoAddress_12 = new DccLocoAddress(12, false);
        DccLocoAddress locoAddress_34 = new DccLocoAddress(34, false);
        
        Assert.assertNotNull("AbstractConsistManager constructor return", cm);
        // Test create new consist
        Assert.assertTrue("consist address is 12", cm.getConsist(locoAddress_12).getConsistAddress().getNumber() == 12);
        // Test getting an existing consist
        Assert.assertTrue("consist address is 12", cm.getConsist(locoAddress_12).getConsistAddress().getNumber() == 12);
        // Add another consist
        Assert.assertTrue("consist address is 34", cm.getConsist(locoAddress_34).getConsistAddress().getNumber() == 34);
        // Get list
        Assert.assertTrue("consist list has two elements", cm.getConsistList().size() == 2);
    }
    
    @Test
    public void testListener() {
        AtomicBoolean listenerHasTrigged = new AtomicBoolean(false);
        
        // Test notify listeners
        cm.addConsistListListener(() -> {
            listenerHasTrigged.set(true);
        });
        cm.notifyConsistListChanged();
        Assert.assertTrue("listener has trigged", listenerHasTrigged.get());
    }
    
    @Test
    public void testDecodeErrorCode() {
        Assume.assumeFalse(ignoreTests);
        
        // Test decodeErrorCode
        Assert.assertEquals("Not Implemented ", cm.decodeErrorCode(ConsistListener.NotImplemented));
        Assert.assertEquals("Operation Completed Successfully ", cm.decodeErrorCode(ConsistListener.OPERATION_SUCCESS));
        Assert.assertEquals("Consist Error ", cm.decodeErrorCode(ConsistListener.CONSIST_ERROR));
        Assert.assertEquals("Address not controled by this device.", cm.decodeErrorCode(ConsistListener.LOCO_NOT_OPERATED));
        Assert.assertEquals("Locomotive already consisted", cm.decodeErrorCode(ConsistListener.ALREADY_CONSISTED));
        Assert.assertEquals("Locomotive Not Consisted ", cm.decodeErrorCode(ConsistListener.NOT_CONSISTED));
        Assert.assertEquals("Speed Not Zero ", cm.decodeErrorCode(ConsistListener.NONZERO_SPEED));
        Assert.assertEquals("Address Not Conist Address ", cm.decodeErrorCode(ConsistListener.NOT_CONSIST_ADDR));
        Assert.assertEquals("Delete Error ", cm.decodeErrorCode(ConsistListener.DELETE_ERROR));
        Assert.assertEquals("Stack Full ", cm.decodeErrorCode(ConsistListener.STACK_FULL));
        Assert.assertEquals("Unknown Status Code: 61440", cm.decodeErrorCode(0xF000));
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
    public void testCsConsistNeedsSeperateAddress(){
        Assume.assumeTrue(cm.isCommandStationConsistPossible());
        // default is false, override if necessary
        Assert.assertFalse("CS Consist Needs Seperate Address",cm.csConsistNeedsSeperateAddress()); 
    }

    @Test
    public void testIsCommandStationConsistPossible(){
       // default is false, override if necessary
       Assert.assertFalse("CS Consist Possible",cm.isCommandStationConsistPossible());
    }

    // private final static Logger log = LoggerFactory.getLogger(AbstractConsistManagerTestBase.class);

}
