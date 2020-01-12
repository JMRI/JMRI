package jmri.jmrix.openlcb;

import jmri.jmrix.can.TestTrafficController;
import jmri.util.JUnitUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openlcb.MimicNodeStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class OlcbConfigurationManagerTest {
        
    private static OlcbSystemConnectionMemo scm;

    @Test
    public void testCTor() {
        OlcbConfigurationManager t = new OlcbConfigurationManager(scm);
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testConfigureManagers() {
        OlcbConfigurationManager t = new OlcbConfigurationManager(scm);
        // this tet verifies this does not throw an exception
        t.configureManagers(); 
    }

    @Test
    public void testConfiguredNodeId() {
        scm.setProtocolOption(OlcbConfigurationManager.OPT_PROTOCOL_IDENT, OlcbConfigurationManager.OPT_IDENT_NODEID, "05.01.01.01.00.ff");
        OlcbConfigurationManager t = new OlcbConfigurationManager(scm);
        t.configureManagers();
        assertEquals("05.01.01.01.00.FF", t.nodeID.toString());
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
    }

    @BeforeClass
    public static void preClassInit() {
        JUnitUtil.setUp();
        scm = new OlcbSystemConnectionMemo();
        TestTrafficController tc = new TestTrafficController();
        scm.setTrafficController(tc);
    }

    @AfterClass
    public static void postClassTearDown() {
        if(scm != null && scm.getInterface() !=null ) {
           scm.getInterface().dispose();
        }
        scm = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

    private final static Logger log = LoggerFactory.getLogger(OlcbConfigurationManagerTest.class);

}
