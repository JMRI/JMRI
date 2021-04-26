package jmri.jmrix.acela.packetgen;

import java.awt.GraphicsEnvironment;

import jmri.jmrix.acela.AcelaSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of AcelaPacketGenFrame
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class AcelaPacketGenFrameTest extends jmri.util.JmriJFrameTestBase {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        if(!GraphicsEnvironment.isHeadless()){
           frame = new AcelaPacketGenFrame(new AcelaSystemConnectionMemo());
        }
    }

    @AfterEach
    @Override
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        super.tearDown();
    }
}
