package jmri.jmrix.bidib;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import jmri.util.JUnitUtil;

/**
 * Tests for the BiDiBSensorManager class
 * 
 * @author  Eckart Meyer  Copyright (C) 2020
 */
public class BiDiBSensorManagerTest extends jmri.managers.AbstractSensorMgrTestBase {
    
    BiDiBSystemConnectionMemo memo;
        
    @Override
    public String getSystemName(int i) {
        return "BS" + i;
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new BiDiBSystemConnectionMemo();
        memo.setBiDiBTrafficController(new TestBiDiBTrafficController(new BiDiBInterfaceScaffold()));
        l = new BiDiBSensorManager(memo);
    }
    
    @AfterEach
    public void tearDown() {
        l.dispose();
        l = null;
        JUnitUtil.tearDown();
    }
}
