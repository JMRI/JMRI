package jmri.util.swing;

import java.awt.GraphicsEnvironment;

import jmri.*;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 *
 * @author Bob Jacobsen Copyright (C) 2017
 */
public class JmriBeanComboBoxTest {

    @Test
    public void testSensorCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Manager m = InstanceManager.getDefault(jmri.SensorManager.class);
        JmriBeanComboBox t = new JmriBeanComboBox(m);
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initInternalSensorManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(JmriBeanComboBoxTest.class);

}
