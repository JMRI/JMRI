package jmri;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the NamedBean interface
 *
 * @author	Bob Jacobsen Copyright (C) 2017
 */
public class NamedBeanTest {

    // Note: This shows that BadUserNameException doesn't (yet) have to be caught or declared
    // Eventually that will go away, and that'll be OK
    @Test
    public void testNormalizePassThrough() {
        String testString = "  foo ";
        String normalForm = NamedBean.normalizeUserName(testString);
        //note: normalizeUserName now .trim()'s;
        Assert.assertEquals("foo", normalForm);
    }

    @Before
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.tearDown();
    }
}
