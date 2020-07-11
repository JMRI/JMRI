package jmri.jmrix.marklin;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * MarklinConnectionTypeListTest.java
 *
 * Test for the jmri.jmrix.marklin.MarklinConnectionTypeList class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class MarklinConnectionTypeListTest {

    @Test
    public void testCtor() {
        MarklinConnectionTypeList c = new MarklinConnectionTypeList();
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
