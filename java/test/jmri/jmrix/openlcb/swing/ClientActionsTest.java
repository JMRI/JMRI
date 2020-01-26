package jmri.jmrix.openlcb.swing;

import jmri.jmrix.can.TestTrafficController;
import jmri.jmrix.openlcb.OlcbConfigurationManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlcb.NodeID;
import org.openlcb.OlcbInterface;
import org.openlcb.can.CanInterface;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class ClientActionsTest {

    @Test
    public void testCTor() {
        TestTrafficController tc = new TestTrafficController();
        NodeID nodeID = new NodeID("02.01.0D.00.00.01");
        CanInterface canInterface = OlcbConfigurationManager.createOlcbCanInterface(nodeID, tc);
        OlcbInterface iface = canInterface.getInterface();
        ClientActions t = new ClientActions(iface);
        Assert.assertNotNull("exists",t);
        // terminate the canInterface (and terminate thread)
        canInterface.dispose();
        // terminate the OlcbInterface (and terminate thread)
        iface.dispose();
        
        // expected (but not checked) message
        jmri.util.JUnitAppender.suppressWarnMessage("ool did not terminate");
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(ClientActionsTest.class);

}
