package jmri.jmrix.nce.boosterprog;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import jmri.jmrix.nce.NceSystemConnectionMemo;
import jmri.jmrix.nce.NceTrafficController;

/**
 * Test simple functioning of BoosterProgPanel
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class BoosterProgPanelTest extends jmri.util.swing.JmriPanelTest {

    private NceSystemConnectionMemo memo = null;

    @Override
    @Test
    public void testInitComponents() {
        // this test currently only verifies there is no exception thrown.
        ((BoosterProgPanel)panel).initComponents(memo);
        // also check that dispose doesn't cause an exception
        panel.dispose();
    }

    @Test
    public void testInitContext() {
        // this test currently only verifies there is no exception thrown.
        ((BoosterProgPanel)panel).initContext(memo);
        // also check that dispose doesn't cause an exception
        panel.dispose();
    }


    @Test
    public void testGetTitleAfterInit() {
        ((BoosterProgPanel)panel).initComponents(memo);
        Assertions.assertEquals("NCE: Booster Programming",panel.getTitle(), "Title");
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new NceSystemConnectionMemo();
        memo.setNceTrafficController(new NceTrafficController());
        panel = new BoosterProgPanel();
        helpTarget="package.jmri.jmrix.nce.boosterprog.BoosterProgPanel";
        title="NCE_: Booster Programming";
    }

    @Override
    @AfterEach
    public void tearDown() {
        memo.getNceTrafficController().terminateThreads();
        memo.dispose();
        memo = null;
        JUnitUtil.tearDown();
    }
}
