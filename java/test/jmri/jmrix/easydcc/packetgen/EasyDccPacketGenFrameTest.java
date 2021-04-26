package jmri.jmrix.easydcc.packetgen;

import jmri.jmrix.easydcc.EasyDccSystemConnectionMemo;

import java.awt.GraphicsEnvironment;

import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.jmrix.easydcc.packetgen.EasyDccPacketGenFrame
 * class
 *
 * @author Bob Jacobsen
 */
public class EasyDccPacketGenFrameTest extends jmri.util.JmriJFrameTestBase {

    @BeforeEach
    @Override
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        if(!GraphicsEnvironment.isHeadless()){
           frame = new EasyDccPacketGenFrame(new EasyDccSystemConnectionMemo("E", "EasyDCC via Serial"));
        } 
    }

    @AfterEach
    @Override
    public void tearDown() {
        jmri.util.JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        super.tearDown();
    }
}
