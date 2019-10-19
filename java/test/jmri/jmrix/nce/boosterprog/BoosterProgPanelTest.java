package jmri.jmrix.nce.boosterprog;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import jmri.jmrix.nce.NceSystemConnectionMemo;
import jmri.jmrix.nce.NceTrafficController;

/**
 * Test simple functioning of BoosterProgPanel
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class BoosterProgPanelTest extends jmri.util.swing.JmriPanelTest {

    private NceSystemConnectionMemo memo = null;

    @Override
    @Test
    public void testInitComponents() throws Exception {
        // this test currently only verifies there is no exception thrown.
        ((BoosterProgPanel)panel).initComponents(memo);
        // also check that dispose doesn't cause an exception
        panel.dispose();
    }

    @Test
    public void testInitContext() throws Exception {
        // this test currently only verifies there is no exception thrown.
        ((BoosterProgPanel)panel).initContext(memo);
        // also check that dispose doesn't cause an exception
        panel.dispose();
    }


    @Test
    public void testGetTitleAfterInit() throws Exception {
        ((BoosterProgPanel)panel).initComponents(memo);
        Assert.assertEquals("Title","NCE: Booster Programming",panel.getTitle());
    }

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        memo = new NceSystemConnectionMemo();
        memo.setNceTrafficController(new NceTrafficController());
        panel = new BoosterProgPanel();
        helpTarget="package.jmri.jmrix.nce.boosterprog.BoosterProgPanel";
        title="NCE_: Booster Programming";
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }
}
