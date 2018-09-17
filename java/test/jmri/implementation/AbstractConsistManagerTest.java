package jmri.implementation;

import java.util.concurrent.atomic.AtomicBoolean;
import jmri.Consist;
import jmri.ConsistListListener;
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
