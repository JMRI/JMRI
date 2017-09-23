package jmri.jmrix.can.swing.send;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of CanSendPane
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class CanSendPaneTest {

    jmri.jmrix.can.CanSystemConnectionMemo memo = null;
    jmri.jmrix.can.TrafficController tc = null;

    @Test
    public void testCtor() {
        CanSendPane pane = new CanSendPane();
        Assert.assertNotNull("exists", pane);
    }

    @Test
    public void testInitComponents() throws Exception{
        CanSendPane pane = new CanSendPane();
        // for now, just makes ure there isn't an exception.
        pane.initComponents(memo);
    }

    @Test
    public void testInitContext() throws Exception {
        CanSendPane pane = new CanSendPane();
        // for now, just makes ure there isn't an exception.
        pane.initContext(memo);
    }

    @Test
    public void testGetHelpTarget(){
        CanSendPane pane = new CanSendPane();
        Assert.assertEquals("help target","package.jmri.jmrix.can.swing.send.CanSendFrame",pane.getHelpTarget());
    }

    @Test
    public void testGetTitle(){
        CanSendPane pane = new CanSendPane();
        Assert.assertEquals("title","Send CAN Frame",pane.getTitle());
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        memo = new jmri.jmrix.can.CanSystemConnectionMemo();
        tc = new jmri.jmrix.can.TrafficControllerScaffold();
        memo.setTrafficController(tc);
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }


}
