package jmri.jmrix.bidib;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import jmri.util.JUnitUtil;

/**
 * Tests for the BiDiBTurnoutManager class
 * 
 * @author  Eckart Meyer  Copyright (C) 2020
 */
public class BiDiBTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTestBase {
    
    BiDiBSystemConnectionMemo memo;
            
    @Override
    public String getSystemName(int n) {
        return "BT" + n;
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new BiDiBSystemConnectionMemo();
        memo.setBiDiBTrafficController(new TestBiDiBTrafficController(new BiDiBInterfaceScaffold()));
        l = new BiDiBTurnoutManager(memo);
    }
    
    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
