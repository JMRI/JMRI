package jmri.jmrit.mastbuilder;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 * Tests for the jmrit.mastbuilder.MastBuilderPane class.
 * Note MastBuilder for now is only a demo.
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class MastBuilderPaneTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        MastBuilderPane t = new MastBuilderPane();
        Assert.assertNotNull("exists", t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(MastBuilderPaneTest.class);

}
