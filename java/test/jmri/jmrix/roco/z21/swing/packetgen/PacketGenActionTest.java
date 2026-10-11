package jmri.jmrix.roco.z21.swing.packetgen;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import jmri.jmrix.roco.z21.Z21InterfaceScaffold;
import jmri.jmrix.roco.z21.Z21SystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for PacketGenAction class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/
public class PacketGenActionTest {

    private Z21SystemConnectionMemo memo = null;
    private Z21InterfaceScaffold tc = null;

    @Test
    public void constructorPacketGenActionTest(){
        assertNotNull( new PacketGenAction("Z21",memo), "PacketGenAction constructor");
    }

    @Test
    public void memoConstructorPacketGenActionTest(){
        assertNotNull( new PacketGenAction(memo), "PacketGenAction constructor");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

        JUnitUtil.initDefaultUserMessagePreferences();
        memo = new Z21SystemConnectionMemo();
        tc = new Z21InterfaceScaffold();
        memo.setTrafficController(tc);
    }

    @AfterEach
    public void tearDown(){
        memo=null;
        tc.terminateThreads();
        tc=null;
        JUnitUtil.tearDown();
    }

}
