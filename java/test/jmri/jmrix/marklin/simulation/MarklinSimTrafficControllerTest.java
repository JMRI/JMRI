package jmri.jmrix.marklin.simulation;

import jmri.jmrix.marklin.MarklinListenerScaffold;
import jmri.jmrix.marklin.MarklinMessage;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for MarklinSimTrafficController.
 * @author Steve Young Copyright (C) 2024
 */
public class MarklinSimTrafficControllerTest extends jmri.jmrix.AbstractMRTrafficControllerTest {

    private MarklinSimTrafficController mtc = null;

    @Test
    public void testSendMarklinMessage() {
        MarklinListenerScaffold mls = new MarklinListenerScaffold();
        mtc.addMarklinListener(mls);
        mtc.sendMarklinMessage( MarklinMessage.getEnableMain(), null);
        Assertions.assertEquals(1, mls.getMarklinMessageList().size());
        Assertions.assertEquals(MarklinMessage.getEnableMain(), mls.getMarklinMessageList().get(0));
        mtc.removeMarklinListener(mls);
        mtc.sendMarklinMessage( MarklinMessage.getKillMain(), null);
        Assertions.assertEquals(1, mls.getMarklinMessageList().size());
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        mtc = new MarklinSimTrafficController();
        tc = mtc;
    }

    @AfterEach
    @Override
    public void tearDown() {
        mtc.dispose();
        mtc = null;
        tc = null;
        JUnitUtil.tearDown();
    }

}
