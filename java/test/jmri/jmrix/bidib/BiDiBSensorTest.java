package jmri.jmrix.bidib;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import jmri.util.JUnitUtil;

/**
 * Tests for the BiDiBReporter class
 * 
 * @author  Eckart Meyer  Copyright (C) 2020
 */
public class BiDiBSensorTest extends jmri.implementation.AbstractSensorTestBase {
    
    BiDiBSystemConnectionMemo memo;
    
    @Override
    public int numListeners() {return 0;}

    @Override
    public void checkActiveMsgSent() {}

    @Override
    public void checkInactiveMsgSent() {}

    @Override
    public void checkStatusRequestMsgSent() {}
    
    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new BiDiBSystemConnectionMemo();
        memo.setBiDiBTrafficController(new TestBiDiBTrafficController(new BiDiBInterfaceScaffold()));
        t = new BiDiBSensor("BS42", new BiDiBSensorManager(memo));
    }
    
    @Override
    @AfterEach
    public void tearDown() {
        t = null;
        JUnitUtil.tearDown();
    }
}
