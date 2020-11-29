package jmri.util.swing;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ComboBoxToolTipRendererTest.class);

}
