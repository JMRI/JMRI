package jmri.jmrix.dcc4pc.swing.monitor;

import apps.tests.Log4JFixture;
import jmri.util.JUnitUtil;
import jmri.InstanceManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import java.awt.GraphicsEnvironment;

/**
 * Test simple functioning of Dcc4PcMonPane
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class Dcc4PcMonPaneTest {

    @Test
    public void testMemoCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Dcc4PcMonPane action = new Dcc4PcMonPane();
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
        Log4JFixture.tearDown();
    }
}
