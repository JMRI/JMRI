package jmri.jmrix.tams.swing.monitor;

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
 * Test simple functioning of TamsMonPane
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class TamsMonPaneTest {


    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        TamsMonPane action = new TamsMonPane();
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
