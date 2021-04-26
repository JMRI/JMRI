package jmri.jmrit.mastbuilder;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 * Tests for the jmrit.mastbuilder package and jmrit.mastbuilder.MastBuilder
 * class.
 * Note MastBuilder for now is only a demo.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 */
public class MastBuilderTest {

    @Test
    public void testShow() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        MastBuilderPane p = new MastBuilderPane();
        Assert.assertNotNull(p);
        JUnitUtil.dispose(p);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
