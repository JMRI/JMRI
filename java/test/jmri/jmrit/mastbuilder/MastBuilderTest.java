package jmri.jmrit.mastbuilder;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmrit.mastbuilder package & jmrit.mastbuilder.MastBuilder
 * class.
 *
 * @author	Bob Jacobsen Copyright (C) 2010
 */
public class MastBuilderTest {

    @Test
    public void testShow() {
        MastBuilderPane p = new MastBuilderPane();
        Assert.assertNotNull(p);
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
