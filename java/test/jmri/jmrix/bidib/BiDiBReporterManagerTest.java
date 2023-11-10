package jmri.jmrix.bidib;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import jmri.util.JUnitUtil;

/**
 * Tests for the BiDiBReporterManager class
 * 
 * @author  Eckart Meyer  Copyright (C) 2020
 */
public class BiDiBReporterManagerTest extends jmri.managers.AbstractReporterMgrTestBase {
    
    BiDiBSystemConnectionMemo memo;
    
    @Override
    public String getSystemName(String i) {
        return "BR" + i;
    }
    
    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new BiDiBSystemConnectionMemo();
        memo.setBiDiBTrafficController(new TestBiDiBTrafficController(new BiDiBInterfaceScaffold()));
        l = new BiDiBReporterManager(memo);
    }
    
    @AfterEach
    public void tearDown() {
        l = null;
        JUnitUtil.tearDown();
    }

}
