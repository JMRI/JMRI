package jmri.jmrix.rfid;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * RfidConnectionTypeListTest.java
 *
 * Test for the jmri.jmrix.rfid.RfidConnectionTypeList class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class RfidConnectionTypeListTest {

    @Test
    public void testCtor() {
        RfidConnectionTypeList c = new RfidConnectionTypeList();
        Assert.assertNotNull(c);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
