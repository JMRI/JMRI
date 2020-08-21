package jmri.jmrix.sprog.update;

import java.awt.GraphicsEnvironment;

import jmri.jmrix.sprog.SprogSystemConnectionMemo;
import jmri.jmrix.sprog.SprogTrafficControlScaffold;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SprogIIUpdateFrameTest extends jmri.util.JmriJFrameTestBase {

    private SprogTrafficControlScaffold stcs = null;
    private SprogSystemConnectionMemo m = null;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        m = new jmri.jmrix.sprog.SprogSystemConnectionMemo(jmri.jmrix.sprog.SprogConstants.SprogMode.OPS);
        stcs = new SprogTrafficControlScaffold(m);
        m.setSprogTrafficController(stcs);
        m.configureCommandStation();
        if (!GraphicsEnvironment.isHeadless()) {
            frame = new SprogIIUpdateFrame(m);
        }
    }

    @AfterEach
    @Override
    public void tearDown() {
        if (frame!=null) ((SprogIIUpdateFrame)frame).stopTimer();
        // frame.dispose() called in super class
        m.getSlotThread().interrupt();
        JUnitUtil.waitFor(() -> {return m.getSlotThread().getState() == Thread.State.TERMINATED;}, "Slot thread failed to stop");
        m.dispose();
        stcs.dispose();
        m = null;
        stcs = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SprogIIUpdateFrameTest.class);

}
