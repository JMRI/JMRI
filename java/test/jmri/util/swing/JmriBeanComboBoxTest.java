package jmri.util.swing;

import jmri.*;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.GraphicsEnvironment;

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

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(JmriBeanComboBoxTest.class.getName());

}
