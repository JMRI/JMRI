package jmri.implementation;

import java.util.concurrent.atomic.AtomicBoolean;
import jmri.Consist;
import jmri.ConsistListListener;
import jmri.ConsistListener;
import jmri.DccLocoAddress;
import jmri.LocoAddress;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for AbstractConsistManager
 */
public class AbstractConsistManagerTest {
    
    @Test
    public void testCtor() {
        
        AtomicBoolean listenerHasTrigged = new AtomicBoolean(false);
        
        DccLocoAddress locoAddress_12 = new DccLocoAddress(12, false);
        DccLocoAddress locoAddress_345 = new DccLocoAddress(345, false);
        
        MyAbstractConsistManager consistManager = new MyAbstractConsistManager();
        Assert.assertNotNull("AbstractConsistManager constructor return", consistManager);
        // Test create new consist
        Assert.assertTrue("consist address is 12", consistManager.getConsist(locoAddress_12).getConsistAddress().getNumber() == 12);
        // Test getting an existing consist
        Assert.assertTrue("consist address is 12", consistManager.getConsist(locoAddress_12).getConsistAddress().getNumber() == 12);
        // Add another consist
        Assert.assertTrue("consist address is 345", consistManager.getConsist(locoAddress_345).getConsistAddress().getNumber() == 345);
        // Get list
        Assert.assertTrue("consist list has two elements", consistManager.getConsistList().size() == 2);
        // Test update from layout
        consistManager.requestUpdateFromLayout();
        // Test shouldRequestUpdateFromLayout()
        Assert.assertTrue("shouldRequestUpdateFromLayout() returns true", consistManager.shouldRequestUpdateFromLayout());
        // Test notify listeners
        consistManager.addConsistListListener(() -> {
            listenerHasTrigged.set(true);
        });
        consistManager.notifyConsistListChanged();
        Assert.assertTrue("listener has trigged", listenerHasTrigged.get());
        
        // Test decodeErrorCode
        Assert.assertEquals("Not Implemented ", consistManager.decodeErrorCode(ConsistListener.NotImplemented));
        Assert.assertEquals("Operation Completed Successfully ", consistManager.decodeErrorCode(ConsistListener.OPERATION_SUCCESS));
        Assert.assertEquals("Consist Error ", consistManager.decodeErrorCode(ConsistListener.CONSIST_ERROR));
        Assert.assertEquals("Address not controled by this device.", consistManager.decodeErrorCode(ConsistListener.LOCO_NOT_OPERATED));
        Assert.assertEquals("Locomotive already consisted", consistManager.decodeErrorCode(ConsistListener.ALREADY_CONSISTED));
        Assert.assertEquals("Locomotive Not Consisted ", consistManager.decodeErrorCode(ConsistListener.NOT_CONSISTED));
        Assert.assertEquals("Speed Not Zero ", consistManager.decodeErrorCode(ConsistListener.NONZERO_SPEED));
        Assert.assertEquals("Address Not Conist Address ", consistManager.decodeErrorCode(ConsistListener.NOT_CONSIST_ADDR));
        Assert.assertEquals("Delete Error ", consistManager.decodeErrorCode(ConsistListener.DELETE_ERROR));
        Assert.assertEquals("Stack Full ", consistManager.decodeErrorCode(ConsistListener.STACK_FULL));
        Assert.assertEquals("Unknown Status Code: 61440", consistManager.decodeErrorCode(0xF000));
    }

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalLightManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.util.JUnitUtil.initDebugThrottleManager();
        jmri.util.JUnitUtil.initDebugProgrammerManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }
    
    
    private class MyAbstractConsistManager extends AbstractConsistManager {
        
        @Override
        protected Consist addConsist(LocoAddress address) {
            DccConsist consist = new DccConsist(address.getNumber());
            consistTable.put(address, consist);
            return consist;
        }
        
        @Override
        public boolean isCommandStationConsistPossible() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
        @Override
        public boolean csConsistNeedsSeperateAddress() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
    };
    
}
