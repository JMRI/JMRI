package jmri.jmrix.openlcb;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlcb.NodeID;
import jmri.InstanceManager;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.ConfigurationManager;
import jmri.jmrix.can.TestTrafficController;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class OlcbConfigurationManagerTest {
        
    private OlcbSystemConnectionMemo memo;
    public TestTrafficController tc;
    public OlcbConfigurationManager configurationManager;

    @Test
    public void testCTor() {
        OlcbConfigurationManager t = new OlcbConfigurationManager(memo);
        Assert.assertNotNull("exists",t);
        t.dispose();
    }

    @Test
    public void testConfigureManagers() {
        OlcbConfigurationManager t = new OlcbConfigurationManager(memo);
        // this tet verifies this does not throw an exception
        t.configureManagers(); 
        t.dispose();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        tc = new TestTrafficController();
        memo = new OlcbSystemConnectionMemo();
        memo.setTrafficController(tc);
        memo.setProtocol(ConfigurationManager.OPENLCB);
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(OlcbConfigurationManagerTest.class);

}
