package jmri.jmrix.dccpp.swing.messages;

import java.io.File;
import java.io.IOException;

import jmri.InstanceManager;
import jmri.jmrix.dccpp.DCCppCommandStation;
import jmri.jmrix.dccpp.DCCppInterfaceScaffold;
import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/** Tests for MessagesAction. */
@DisabledIfHeadless
public class MessagesActionTest {

    @TempDir
    File folder;

    private DCCppInterfaceScaffold tc;
    private DCCppSystemConnectionMemo memo;

    @Test
    public void testConstructorDefault() {
        InstanceManager.store(memo, DCCppSystemConnectionMemo.class);
        Assertions.assertNotNull(new MessagesAction());
    }

    @Test
    public void testActionOpensOneFrame() {
        MessagesAction action = new MessagesAction(memo);
        ThreadingUtil.runOnGUI(() -> action.actionPerformed(null));
        ThreadingUtil.runOnGUI(() -> action.actionPerformed(null));
        Assertions.assertEquals(1, JmriJFrame.getFrameList(MessagesFrame.class).size());
        Assertions.assertNotNull(MessagesFrame.findFrame(memo));
    }

    @BeforeEach
    public void setUp() throws IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(folder));
        JUnitUtil.initDefaultUserMessagePreferences();
        tc = new DCCppInterfaceScaffold(new DCCppCommandStation());
        memo = new DCCppSystemConnectionMemo(tc);
    }

    @AfterEach
    public void tearDown() {
        for (MessagesFrame f : JmriJFrame.getFrameList(MessagesFrame.class)) {
            JUnitUtil.dispose(f);
        }
        memo.getDCCppTrafficController().terminateThreads();
        memo.dispose();
        memo = null;
        tc = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
