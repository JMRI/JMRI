package jmri.jmrit.mastbuilder;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmrit.mastbuilder package and jmrit.mastbuilder.MastBuilder
 * class.
 * Note MastBuilder for now is only a demo.
 *
 * @author	Bob Jacobsen Copyright (C) 2010
 */
public class MastBuilderTest {

    @Test
    public void testShow() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        MastBuilderPane p = new MastBuilderPane();
        Assert.assertNotNull(p);
        JUnitUtil.dispose(p);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
