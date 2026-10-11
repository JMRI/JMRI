package jmri.jmrix.bidib;

import jmri.InstanceManager;
import jmri.MeterManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;

import org.junit.jupiter.api.*;

import jmri.util.JUnitUtil;

/**
 * Tests for the BiDiBPredefinedMeters class
 * 
 * @author  Eckart Meyer  Copyright (C) 2020
 */
public class BiDiBPredefinedMetersTest {
    
    private BiDiBSystemConnectionMemo memo;
    private BiDiBPredefinedMeters mm;

    @Test
    public void testBiDiBPredefinedMeterCtor() {
        Assertions.assertNotNull(mm);
    }

//    @Test
//    public void testMeterName() {
//        //TODO
//        //Assert.assertEquals("BiDiB", mm.getHardwareMeterName());
//    }

    
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new BiDiBSystemConnectionMemo();
        memo.setBiDiBTrafficController(new TestBiDiBTrafficController(new BiDiBInterfaceScaffold()));
        MeterManager itm = new jmri.jmrix.internal.InternalMeterManager(new InternalSystemConnectionMemo("J", "Juliet"));
        InstanceManager.store(itm, MeterManager.class);
        mm = new BiDiBPredefinedMeters(memo);
    }
    
    @AfterEach
    public void tearDown() {
        memo = null;
        mm = null;
        JUnitUtil.tearDown(); 
    }

    
}
