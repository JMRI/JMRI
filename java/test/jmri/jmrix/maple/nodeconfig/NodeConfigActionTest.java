package jmri.jmrix.maple.nodeconfig;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.maple.MapleSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of NodeConfigAction
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class NodeConfigActionTest {

    @Test
    public void testStringCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        NodeConfigAction action = new NodeConfigAction("Maple test Action", new MapleSystemConnectionMemo());
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        NodeConfigAction action = new NodeConfigAction(new MapleSystemConnectionMemo());
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();

    }

    @After
    public void tearDown() {

        JUnitUtil.tearDown();
    }

}
