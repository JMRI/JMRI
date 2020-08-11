package jmri.jmrix.jmriclient.swing.packetgen;

import java.awt.GraphicsEnvironment;

import jmri.jmrix.jmriclient.JMRIClientSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 * Test simple functioning of PacketGenAction
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PacketGenActionTest {

    @Test
    public void testStringMemoCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PacketGenAction action = new PacketGenAction("JMRIClient PacketGen test Action", new JMRIClientSystemConnectionMemo());
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testMemoCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PacketGenAction action = new PacketGenAction( new JMRIClientSystemConnectionMemo());
        Assert.assertNotNull("exists", action);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }
}
