package jmri.jmrix.openlcb;

import jmri.jmrix.can.TestTrafficController;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.BeforeAll;
import org.openlcb.MimicNodeStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class OlcbConfigurationManagerTest {

    private static OlcbSystemConnectionMemoScaffold scm;

    @Test
    public void testCTor() {
        OlcbConfigurationManager t = new OlcbConfigurationManager(scm);
        Assert.assertNotNull("exists",t);
        t.dispose();
    }

    @Test
    public void testConfigureManagers() {
        OlcbConfigurationManager t = new OlcbConfigurationManager(scm);
        // this test verifies this does not throw an exception
        t.configureManagers();
        t.dispose();
    }

    @Test
    public void testConfiguredNodeId() {
        scm.setProtocolOption(OlcbConfigurationManager.OPT_PROTOCOL_IDENT, OlcbConfigurationManager.OPT_IDENT_NODEID, "05.01.01.01.00.ff");
        OlcbConfigurationManager t = new OlcbConfigurationManager(scm);
        t.configureManagers();
        assertEquals("05.01.01.01.00.FF", t.nodeID.toString());
        t.dispose();
    }

    @Test
    public void testConfiguredUserNameAndDescription() {
        log.debug("Start name and desription test");
        scm.setProtocolOption(OlcbConfigurationManager.OPT_PROTOCOL_IDENT, OlcbConfigurationManager.OPT_IDENT_NODEID, "05.01.01.01.00.ff");
        scm.setProtocolOption(OlcbConfigurationManager.OPT_PROTOCOL_IDENT, OlcbConfigurationManager.OPT_IDENT_USERNAME, "Test User Name");
        scm.setProtocolOption(OlcbConfigurationManager.OPT_PROTOCOL_IDENT, OlcbConfigurationManager.OPT_IDENT_DESCRIPTION, "Test Description");
        OlcbConfigurationManager t = new OlcbConfigurationManager(scm);
        t.configureManagers();

        MimicNodeStore ns = t.get(MimicNodeStore.class);
        ns.addNode(t.nodeID).getSimpleNodeIdent();
        t.getInterface().flushSendQueue();
        t.getInterface().flushSendQueue();

        MimicNodeStore.NodeMemo nmemo = ns.findNode(t.nodeID);
        assertEquals("Test User Name", nmemo.getSimpleNodeIdent().getUserName());
        assertEquals("Test Description", nmemo.getSimpleNodeIdent().getUserDesc());

        t.dispose();
    }

    @BeforeAll
    public static void preClassInit() {
        JUnitUtil.setUp();
       // this test is run separately because it leaves a lot of threads behind
        org.junit.Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));
        scm = new OlcbSystemConnectionMemoScaffold();
        TestTrafficController tc = new TestTrafficController();
        scm.setTrafficController(tc);
    }

    @AfterAll
    public static void postClassTearDown() {
        if (Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning") == false ) {
            if (scm != null && scm.getInterface() !=null ) {
                scm.getTrafficController().terminateThreads();
                scm.getInterface().dispose();
            }
            scm = null;
        }
        jmri.util.JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(OlcbConfigurationManagerTest.class);

}
