/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.jmrix.bidib;

import org.junit.After;
import org.junit.Before;
import org.junit.Assert;

import java.util.TreeMap;

import jmri.util.JUnitUtil;

import org.bidib.jbidibc.messages.Node;


/**
 * Tests for the BiDiBNodeInitializer class
 * 
 * @author  Eckart Meyer  Copyright (C) 2023
 */
public class BiDiBNodeInitializerTest {
    
    BiDiBSystemConnectionMemo memo;
    
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        memo = new BiDiBSystemConnectionMemo();
        BiDiBTrafficController tc = new TestBiDiBTrafficController(new BiDiBInterfaceScaffold());
        memo.setBiDiBTrafficController(tc);
        TreeMap<Long, Node> nodes = new TreeMap<>();
        BiDiBNodeInitializer ni = new BiDiBNodeInitializer(tc, tc.getBidib(), nodes);
        Assert.assertNotNull(ni);
    }
    
    
    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
