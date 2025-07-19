package jmri.jmrix.bidib;

import java.util.TreeMap;

import jmri.util.JUnitUtil;

import org.bidib.jbidibc.messages.Node;

import org.junit.jupiter.api.*;

/**
 * Tests for the BiDiBNodeInitializer class
 * 
 * @author  Eckart Meyer  Copyright (C) 2023
 */
public class BiDiBNodeInitializerTest {

    BiDiBSystemConnectionMemo memo;
    BiDiBNodeInitializer ni;

    @Test
    public void testBiDiBNodeInitializerSetup() {
        Assertions.assertNotNull(ni);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new BiDiBSystemConnectionMemo();
        BiDiBTrafficController tc = new TestBiDiBTrafficController(new BiDiBInterfaceScaffold());
        memo.setBiDiBTrafficController(tc);
        TreeMap<Long, Node> nodes = new TreeMap<>();
        ni = new BiDiBNodeInitializer(tc, tc.getBidib(), nodes);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
