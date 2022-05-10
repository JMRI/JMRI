package jmri.jmrix.can.cbus.simulator.moduletypes;

import jmri.jmrix.can.cbus.simulator.CbusDummyNode;
import jmri.jmrix.can.cbus.simulator.CbusSimulatedModuleProvider;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Abstract tests for classes which extend from
 * CbusSimulatedModuleProvider.
 * @author Steve Young Copyright (C) 2022
 */
public abstract class SimModuleProviderTestBase {

    protected CbusSimulatedModuleProvider provider;

    @Test
    public void testCtor() {
        Assertions.assertNotNull(provider, "exists");
    }

    @Test
    public void testSetDummyNodeParameters() {
        
        CbusDummyNode node = provider.getNewDummyNode(null, 0);
        Assertions.assertNotNull(node,"Node provided by provider");
        Assertions.assertEquals(provider.getManufacturerId(), node.getNodeParamManager().getParameter(1),
            "provider manufacturer matches manufacturer Param");
        Assertions.assertEquals(provider.getModuleId(), node.getNodeParamManager().getParameter(3),
            "provider manufacturer module id matches manufacturer module id Param");
        
        node.dispose();
    }

    @Test
    public void testGetModuleType() {
        Assertions.assertNotNull(provider.getModuleType(),"Module type exists");
        Assertions.assertFalse(provider.getModuleType().isBlank(),"Module type not blank");
    }

    @Test
    public void testGetToolTipText() {
        Assertions.assertNotNull(provider.getToolTipText(),"Module tooltip exists");
        Assertions.assertFalse(provider.getToolTipText().isBlank(),"Module tooltip not blank");
    }

    @Test
    public void testGetProviderByName() {
        CbusSimulatedModuleProvider result = CbusSimulatedModuleProvider.getProviderByName(provider.getModuleType());
        Assertions.assertNotNull(result,"provider found by name");
    }
    
    @Test
    public void testFoundInInstancesCollection() {
        boolean result = CbusSimulatedModuleProvider.getInstancesCollection().stream().anyMatch(
            obj -> obj.getModuleType().equals(provider.getModuleType()));
        Assertions.assertTrue(result,"Provider Found in Collection");
    }
    
    @Test
    public void testMatchesManuFacturerAndModuleId() {
    
        Assertions.assertFalse(provider.matchesManuAndModuleId(null),"does not match null");
        
        CbusDummyNode node = provider.getNewDummyNode(null, 0);
        Assertions.assertTrue(provider.matchesManuAndModuleId(node),"Node matches provider");
        
        node.getNodeParamManager().setParameter(1, provider.getManufacturerId()+1);
        Assertions.assertFalse(provider.matchesManuAndModuleId(node),"manufacturer does not match");
        
        node.getNodeParamManager().setParameter(1, provider.getManufacturerId());
        Assertions.assertTrue(provider.matchesManuAndModuleId(node),"Node matches manufacturer");
        
        node.getNodeParamManager().setParameter(3, provider.getModuleId()+1);
        Assertions.assertFalse(provider.matchesManuAndModuleId(node),"module id does not match");
        
    }

    
    @BeforeEach
    public void setUp(){
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        provider = null;
        JUnitUtil.tearDown();
    }

}
