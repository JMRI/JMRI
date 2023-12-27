package jmri.jmrix.bidib;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the BiDiBSignalMast class
 * 
 * @author  Eckart Meyer  Copyright (C) 2020
 */
public class BiDiBSignalMastTest {

    BiDiBSystemConnectionMemo memo;
    BiDiBSignalMast t;

    @Test
    public void testCTor() {
        Assertions.assertNotNull(t, "exists");
        Assertions.assertTrue( t.getAddr().isValid(), "valid systemname");
    }

    // TODO: more checks on systemname

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new BiDiBSystemConnectionMemo();
        memo.setBiDiBTrafficController(new TestBiDiBTrafficController(new BiDiBInterfaceScaffold()));
        t = new BiDiBSignalMast("BF$bsm:DB-HV-1969:block(Test1:13)");
    }

    @AfterEach
    public void tearDown() {
        t = null;
        JUnitUtil.tearDown();
    }

}
