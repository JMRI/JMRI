package jmri.jmrix.bidib;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import jmri.util.JUnitUtil;

/**
 * Tests for the BiDiBSystemConnectionMemo class
 *
 * @author  Eckart Meyer  Copyright (C) 2020
 */

public class BiDiBSystemConnectionMemoTest extends jmri.jmrix.SystemConnectionMemoTestBase<BiDiBSystemConnectionMemo> {
    
    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        scm = new BiDiBSystemConnectionMemo();
        scm.setBiDiBTrafficController(new TestBiDiBTrafficController(new BiDiBInterfaceScaffold()));
    }
    
    @Override
    @AfterEach
    public void tearDown() {
        scm = null;
        JUnitUtil.tearDown();
    }    
}
