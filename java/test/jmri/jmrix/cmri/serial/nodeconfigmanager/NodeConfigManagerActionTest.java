package jmri.jmrix.cmri.serial.nodeconfigmanager;

import java.awt.GraphicsEnvironment;

import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 * Test simple functioning of NodeConfigManagerAction
 * Copied from nodeconfig
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Chuck Catania Copyright (C) 2017
 */
public class NodeConfigManagerActionTest {

    @Test
    public void testStringCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        NodeConfigManagerAction action = new NodeConfigManagerAction("C/MRI test Action", new CMRISystemConnectionMemo());
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        NodeConfigManagerAction action = new NodeConfigManagerAction( new CMRISystemConnectionMemo());
        Assert.assertNotNull("exists", action);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {        JUnitUtil.tearDown();    }
}
