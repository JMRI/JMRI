package jmri.jmrix.bidib;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import jmri.util.JUnitUtil;

/**
 * Tests for the BiDiBTurnout class
 * 
 * @author  Eckart Meyer  Copyright (C) 2020
 */
public class BiDiBTurnoutTest extends jmri.implementation.AbstractTurnoutTestBase {
    
    BiDiBSystemConnectionMemo memo;
    
    @Override
    public int numListeners() {
        return 0;
    }

    @Override
    public void checkClosedMsgSent() {
    }

    @Override
    public void checkThrownMsgSent() {
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new BiDiBSystemConnectionMemo();
        memo.setBiDiBTrafficController(new TestBiDiBTrafficController(new BiDiBInterfaceScaffold()));
        t = new BiDiBTurnout("BT42", new BiDiBTurnoutManager(memo));
    }
    
    @Override
    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
