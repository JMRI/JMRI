package jmri.jmrix.dccpp.swing.exrail;

import jmri.InstanceManager;
import jmri.jmrix.dccpp.DCCppCommandStation;
import jmri.jmrix.dccpp.DCCppInterfaceScaffold;
import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Tests for DCCppExrailAction.
 *
 * @author Chad Francis Copyright (C) 2026
 */
@DisabledIfHeadless
public class DCCppExrailActionTest {

    private DCCppSystemConnectionMemo _memo;
    private DCCppInterfaceScaffold _tc;

    @Test
    public void testConstructorWithMemo() {
        DCCppExrailAction action = new DCCppExrailAction(_memo);
        Assertions.assertNotNull(action);
    }

    @Test
    public void testConstructorDefault() {
        InstanceManager.store(_memo, DCCppSystemConnectionMemo.class);
        DCCppExrailAction action = new DCCppExrailAction();
        Assertions.assertNotNull(action);
    }

    @Test
    public void testActionPerformedOpensFrame() {
        DCCppExrailAction action = new DCCppExrailAction(_memo);
        ThreadingUtil.runOnGUI(() -> action.actionPerformed(null));
        JFrameOperator jfo = new JFrameOperator(Bundle.getMessage("ExrailFrameTitle"));
        Assertions.assertNotNull(jfo);
        JUnitUtil.dispose(jfo.getWindow());
        jfo.waitClosed();
    }

    @Test
    public void testActionSendsAutomationIDsRequest() {
        DCCppExrailAction action = new DCCppExrailAction(_memo);
        ThreadingUtil.runOnGUI(() -> action.actionPerformed(null));
        JFrameOperator jfo = new JFrameOperator(Bundle.getMessage("ExrailFrameTitle"));
        Assertions.assertFalse(_tc.outbound.isEmpty(), "should have sent <JA> on open");
        Assertions.assertTrue(_tc.outbound.get(0).isAutomationIDsMessage(), "first outbound should be <JA>");
        JUnitUtil.dispose(jfo.getWindow());
        jfo.waitClosed();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        _tc = new DCCppInterfaceScaffold(new DCCppCommandStation());
        _memo = new DCCppSystemConnectionMemo(_tc);
    }

    @AfterEach
    public void tearDown() {
        _memo.getDCCppTrafficController().terminateThreads();
        _memo.dispose();
        _memo = null;
        _tc = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
