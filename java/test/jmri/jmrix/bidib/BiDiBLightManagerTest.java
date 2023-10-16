package jmri.jmrix.bidib;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import jmri.util.JUnitUtil;


/**
 * Tests for the BiDiBLightManager class
 * 
 * @author  Eckart Meyer  Copyright (C) 2020
 */
public class BiDiBLightManagerTest  extends jmri.managers.AbstractLightMgrTestBase {
    
    BiDiBSystemConnectionMemo memo;
    
    @Override
    public String getSystemName(int i) {
        return "BL" + i;
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new BiDiBSystemConnectionMemo();
        memo.setBiDiBTrafficController(new TestBiDiBTrafficController(new BiDiBInterfaceScaffold()));
        l = new BiDiBLightManager(memo);
    }
    
    @AfterEach
    public void tearDown() {
        l = null;
        JUnitUtil.tearDown();
    }

}
