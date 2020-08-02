package jmri.jmrix.maple.packetgen;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import jmri.jmrix.maple.MapleSystemConnectionMemo;

/**
 * Test simple functioning of SerialPacketGenFrame
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SerialPacketGenFrameTest extends jmri.util.JmriJFrameTestBase {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            frame = new SerialPacketGenFrame(new MapleSystemConnectionMemo());
        }
    }

    @AfterEach
    @Override
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        super.tearDown();
    }
}
