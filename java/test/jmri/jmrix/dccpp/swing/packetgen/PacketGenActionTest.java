package jmri.jmrix.dccpp.swing.packetgen;

import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Test simple functioning of PacketGenAction
 *
 * @author Paul Bender Copyright (C) 2016
 */
@DisabledIfHeadless
public class PacketGenActionTest {

    
    private jmri.jmrix.dccpp.DCCppSystemConnectionMemo memo = null;

    @Test
    public void testDccPpPacketGenMemoCtor() {
        PacketGenAction action = new PacketGenAction(memo);
        Assertions.assertNotNull( action, "exists");
    }

    @Test
    public void testPacketGenActionPerformed() {
        PacketGenAction action = new PacketGenAction(); // default CTor
        ThreadingUtil.runOnGUI(() -> action.actionPerformed(null));

        JFrameOperator jfo = new JFrameOperator(Bundle.getMessage("PacketGenFrameTitle") + " (D)");
        Assertions.assertNotNull(jfo);

        JUnitUtil.dispose(jfo.getWindow());
        jfo.waitClosed();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.jmrix.dccpp.DCCppInterfaceScaffold t = new jmri.jmrix.dccpp.DCCppInterfaceScaffold(new jmri.jmrix.dccpp.DCCppCommandStation());
        memo = new jmri.jmrix.dccpp.DCCppSystemConnectionMemo(t);

        jmri.InstanceManager.store(memo, jmri.jmrix.dccpp.DCCppSystemConnectionMemo.class);

    }

    @AfterEach
    public void tearDown() {
        memo.getDCCppTrafficController().terminateThreads();
        memo.dispose();
        memo = null;
        JUnitUtil.tearDown();
    }
}
