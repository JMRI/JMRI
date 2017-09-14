package jmri.jmrix.cmri.serial.nodeconfigmanager;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

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
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
