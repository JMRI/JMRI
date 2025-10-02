package jmri.jmrix.roco.z21.swing.packetgen;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import jmri.jmrix.roco.z21.RocoZ21CommandStation;
import jmri.jmrix.roco.z21.Z21InterfaceScaffold;
import jmri.jmrix.roco.z21.Z21SystemConnectionMemo;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Tests for PacketGenFrame class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/
public class PacketGenFrameTest {

    private Z21SystemConnectionMemo memo = null;
    private Z21InterfaceScaffold tc = null;

    @Test
    @DisabledIfHeadless
    public void memoConstructorPacketGenFrameTest(){
        assertNotNull( new PacketGenFrame(), "PacketGenFrame constructor");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

        JUnitUtil.initDefaultUserMessagePreferences();
        memo = new Z21SystemConnectionMemo();
        tc = new Z21InterfaceScaffold();
        memo.setTrafficController(tc);
        memo.setRocoZ21CommandStation(new RocoZ21CommandStation());
    }

    @AfterEach
    public void tearDown(){
        memo=null;
        tc.terminateThreads();
        tc=null;
        JUnitUtil.tearDown();
    }

}
