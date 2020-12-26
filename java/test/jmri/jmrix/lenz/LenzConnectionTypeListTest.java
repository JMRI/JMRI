package jmri.jmrix.lenz;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * LenzConnectionTypeListTest.java
 *
 * Test for the jmri.jmrix.lenz.LenzConnectionTypeList class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class LenzConnectionTypeListTest {

    @Test
    public void testCtor() {
        LenzConnectionTypeList c = new LenzConnectionTypeList();
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
