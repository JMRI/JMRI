package jmri.jmrix.cmri.serial.packetgen;

import java.awt.GraphicsEnvironment;

import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

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
            frame = new SerialPacketGenFrame(new CMRISystemConnectionMemo());
        }
    }

    @AfterEach
    @Override
    public void tearDown() {
        jmri.util.JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        super.tearDown();
    }
}
