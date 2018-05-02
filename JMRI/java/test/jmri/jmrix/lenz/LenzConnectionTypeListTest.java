package jmri.jmrix.lenz;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * LenzConnectionTypeListTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.LenzConnectionTypeList class
 *
 * @author	Paul Bender Copyright (C) 2012,2016
 */
public class LenzConnectionTypeListTest {

    @Test
    public void testCtor() {
        LenzConnectionTypeList c = new LenzConnectionTypeList();
        Assert.assertNotNull(c);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
