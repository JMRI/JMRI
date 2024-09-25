package jmri.jmrix.sprog.update;

import jmri.jmrix.sprog.SprogSystemConnectionMemo;
import jmri.jmrix.sprog.SprogTrafficControlScaffold;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
@jmri.util.junit.annotations.DisabledIfHeadless
public class SprogVersionFrameTest extends jmri.util.JmriJFrameTestBase {

    private SprogTrafficControlScaffold stcs = null;
    private SprogSystemConnectionMemo m = null;

    @Test
    @Override
    public void testAccessibleContent() {
        Thread t = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("SprogVersionTitle"), "OK");
        super.testAccessibleContent();
        JUnitUtil.waitThreadTerminated(t.getName());
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        m = new jmri.jmrix.sprog.SprogSystemConnectionMemo(jmri.jmrix.sprog.SprogConstants.SprogMode.OPS);
        stcs = new SprogTrafficControlScaffold(m);
        m.setSprogTrafficController(stcs);
        m.configureCommandStation();
        frame = new SprogVersionFrame(m);
    }

    @AfterEach
    @Override
    public void tearDown() {
        m.getSlotThread().interrupt();
        JUnitUtil.waitThreadTerminated(m.getSlotThread().getName());
        stcs.dispose();
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly

        JUnitUtil.waitFor(50); // mini pause while Dialog operator fully completes
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SprogVersionFrameTest.class);

}
