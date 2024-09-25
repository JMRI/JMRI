package jmri.jmrix.sprog.console;

import jmri.jmrix.sprog.SprogSystemConnectionMemo;
import jmri.jmrix.sprog.SprogTrafficControlScaffold;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of SprogConsoleFrame.
 *
 * @author Paul Bender Copyright (C) 2016
 */
@jmri.util.junit.annotations.DisabledIfHeadless
public class SprogConsoleFrameTest extends jmri.util.JmriJFrameTestBase {

    private SprogTrafficControlScaffold stcs = null;
    private SprogSystemConnectionMemo m = null;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        m = new SprogSystemConnectionMemo(jmri.jmrix.sprog.SprogConstants.SprogMode.OPS);
        stcs = new SprogTrafficControlScaffold(m);
        m.setSprogTrafficController(stcs);
        m.configureCommandStation();
        frame = new SprogConsoleFrame(m);

    }

    @Test
    @Override
    public void testShowAndClose(){
        Thread t = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("SprogConsoleTitle"), "OK");
        super.testShowAndClose();
        JUnitUtil.waitThreadTerminated(t.getName());
    }

    @Test
    @Override
    public void testAccessibleContent() {
        Thread t = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("SprogConsoleTitle"), "OK");
        super.testAccessibleContent();
        JUnitUtil.waitThreadTerminated(t.getName());
    }

    @AfterEach
    @Override
    public void tearDown() {
        m.getSlotThread().interrupt();
        JUnitUtil.waitThreadTerminated(m.getSlotThread().getName());
        stcs.dispose();

        JUnitUtil.waitFor(50); // dialog operator may still be closing
        super.tearDown();
    }

}
