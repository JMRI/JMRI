package jmri.jmrix.grapevine.nodetable;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import jmri.jmrix.grapevine.GrapevineSystemConnectionMemo;
import jmri.jmrix.grapevine.SerialTrafficController;
import jmri.jmrix.grapevine.SerialTrafficControlScaffold;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class NodeTablePaneTest {

    private GrapevineSystemConnectionMemo memo = null; 

    @Test
    public void testCTor() {
        NodeTablePane t = new NodeTablePane(memo);
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testStartPollStopsEarlierCheck() {
        NodeTablePane t = new NodeTablePane(memo);
        t.initComponents();
        javax.swing.Timer first = t.timer;
        Assert.assertTrue("check started by initComponents", first.isRunning());
        t.startPoll();
        Assert.assertFalse("earlier check stopped", first.isRunning());
        Assert.assertTrue("new check running", t.timer.isRunning());
        t.dispose();
        Assert.assertNull("check stopped by dispose", t.timer);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new GrapevineSystemConnectionMemo();
        SerialTrafficController tc = new SerialTrafficControlScaffold(memo);
        memo.setTrafficController(tc);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

    // private static final Logger log = LoggerFactory.getLogger(NodeTablePaneTest.class);

}
