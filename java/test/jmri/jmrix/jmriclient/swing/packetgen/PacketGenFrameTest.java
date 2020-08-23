package jmri.jmrix.jmriclient.swing.packetgen;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of PacketGenFrame
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PacketGenFrameTest extends jmri.util.JmriJFrameTestBase {

    // private JMRIClientTrafficController tc = null;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        // tc = new JMRIClientTrafficController();
        if(!GraphicsEnvironment.isHeadless()) {
           frame = new PacketGenFrame();
        }
    } 

    @AfterEach
    @Override
    public void tearDown() {
        // tc = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        super.tearDown();
    }
}
