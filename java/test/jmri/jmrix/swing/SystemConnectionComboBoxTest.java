package jmri.jmrix.swing;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class SystemConnectionComboBoxTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SystemConnectionComboBox t = new SystemConnectionComboBox();
        Assert.assertNotNull("exists",t);
        t.dispose();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SystemConnectionComboBoxTest.class);

}
