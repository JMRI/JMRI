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

    @Test
    public void testInitComponents() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        NodeConfigManagerFrame frame = new NodeConfigManagerFrame(new CMRISystemConnectionMemo());
        // test to make sure initCompoents doesn't throw an exception.
        frame.initComponents();
        // close
        frame.dispose();
    }



    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
