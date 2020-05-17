package jmri.jmrix.marklin;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
