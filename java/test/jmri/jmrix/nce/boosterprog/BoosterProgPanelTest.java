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
public class BoosterProgPanelTest {

    private NceSystemConnectionMemo memo = null;

    @Test
    public void testCtor() {
        BoosterProgPanel action = new BoosterProgPanel();
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testInitComponents() throws Exception {
        BoosterProgPanel action = new BoosterProgPanel();
        // this test currently only verifies there is no exception thrown.
        action.initComponents(memo);
        // also check that dispose doesn't cause an exception
        action.dispose();
    }

    @Test
    public void testInitContext() throws Exception {
        BoosterProgPanel action = new BoosterProgPanel();
        // this test currently only verifies there is no exception thrown.
        action.initContext(memo);
        // also check that dispose doesn't cause an exception
        action.dispose();
    }

    @Test
    public void testHelpTarget() {
        BoosterProgPanel action = new BoosterProgPanel();
        Assert.assertEquals("help target","package.jmri.jmrix.nce.boosterprog.BoosterProgPanel",action.getHelpTarget());
    }

    @Test
    public void testGetTitle(){
        BoosterProgPanel action = new BoosterProgPanel();
        Assert.assertEquals("Title","NCE_: Booster Programming",action.getTitle());
    }

    @Test
    public void testGetTitleAfterInit() throws Exception {
        BoosterProgPanel action = new BoosterProgPanel();
        action.initComponents(memo);
        Assert.assertEquals("Title","NCE: Booster Programming",action.getTitle());
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        memo = new NceSystemConnectionMemo();
        memo.setNceTrafficController(new NceTrafficController());
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
