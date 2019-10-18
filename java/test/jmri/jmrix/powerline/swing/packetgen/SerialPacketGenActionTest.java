package jmri.jmrix.powerline.swing.packetgen;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.powerline.SerialTrafficControlScaffold;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

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
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }
}
