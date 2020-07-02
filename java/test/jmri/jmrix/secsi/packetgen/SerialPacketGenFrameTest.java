package jmri.jmrix.secsi.packetgen;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import jmri.jmrix.secsi.SecsiSystemConnectionMemo;

/**
 * Test simple functioning of SerialPacketGenFrame
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SerialPacketGenFrameTest extends jmri.util.JmriJFrameTestBase {

    private SecsiSystemConnectionMemo memo = null;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        memo = new SecsiSystemConnectionMemo();
        if (!GraphicsEnvironment.isHeadless()) {
            frame = new SerialPacketGenFrame(memo);
        }
    }

    @AfterEach
    @Override
    public void tearDown() {
        memo = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        super.tearDown();
    }
}
