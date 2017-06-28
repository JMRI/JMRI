package jmri.jmrix.powerline.swing.packetgen;

import apps.tests.Log4JFixture;
import jmri.util.JUnitUtil;
import jmri.jmrix.powerline.SerialTrafficControlScaffold;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import java.awt.GraphicsEnvironment;

/**
 * Test simple functioning of SerialPacketGenAction
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class SerialPacketGenActionTest {

    @Test
    public void testStringMemoCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SerialPacketGenAction action = new SerialPacketGenAction("PowerLine test Action", new SerialTrafficControlScaffold());
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testTCCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SerialPacketGenAction action = new SerialPacketGenAction( new SerialTrafficControlScaffold());
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
