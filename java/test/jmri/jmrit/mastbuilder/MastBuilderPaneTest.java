package jmri.jmrit.mastbuilder;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

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

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(MastBuilderPaneTest.class);

}
