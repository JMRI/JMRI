package jmri.util.swing;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Bob Jacobsen Copyright (C) 2017	
 */
public class ComboBoxToolTipRendererTest {

    @Test
    public void testSensorCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ComboBoxToolTipRenderer t = new ComboBoxToolTipRenderer();
        Assert.assertNotNull("exists",t);
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

    // private final static Logger log = LoggerFactory.getLogger(ComboBoxToolTipRendererTest.class);

}
