package jmri.jmrix.dccpp.network;

import jmri.util.JUnitUtil;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.junit.Assert;

/**
 * DCCppEthernetAdapterTest.java
 *
 * Description: tests for the jmri.jmrix.dccpp.network.DCCppEthernetAdapter
 * class
 *
 * @author Paul Bender
 * @author Mark Underwood Copyright (C) 2015
 */
public class DCCppEthernetAdapterTest {

    @Test
    public void testCtor() {
        DCCppEthernetAdapter a = new DCCppEthernetAdapter();
        Assert.assertNotNull(a);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
