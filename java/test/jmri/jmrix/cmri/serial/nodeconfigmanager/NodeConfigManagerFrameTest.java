package jmri.jmrix.cmri.serial.nodeconfigmanager;

import apps.tests.Log4JFixture;
import jmri.util.JUnitUtil;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import java.awt.GraphicsEnvironment;

/**
 * Test simple functioning of NodeConfigFrame
 * Copied from NodeConfig
 *
 * @author	Paul Bender Copyright (C) 2016
 * @author	Chuck Catania Copyright (C) 2017
 */
public class NodeConfigManagerFrameTest {

    @Test
    public void testMemoCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        NodeConfigManagerFrame action = new NodeConfigManagerFrame(new CMRISystemConnectionMemo());
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
