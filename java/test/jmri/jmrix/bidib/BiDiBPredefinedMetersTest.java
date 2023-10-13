package jmri.jmrix.bidib;

// import org.junit.Assert;
// import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import jmri.util.JUnitUtil;

/**
 * Tests for the BiDiBMultiMeter class
 * 
 * @author  Eckart Meyer  Copyright (C) 2020
 */
public class BiDiBPredefinedMetersTest {
    
    BiDiBSystemConnectionMemo memo;
    BiDiBPredefinedMeters mm;

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
        mm = new BiDiBPredefinedMeters(memo);
    }
    
    @AfterEach
    public void tearDown() {
        memo = null;
        //tc = null;
        JUnitUtil.tearDown(); 
    }

    
}
