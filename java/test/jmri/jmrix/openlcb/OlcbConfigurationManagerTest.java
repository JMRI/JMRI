package jmri.jmrix.openlcb;

import jmri.jmrix.can.TestTrafficController;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class OlcbConfigurationManagerTest {
        
    private OlcbSystemConnectionMemo scm;

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

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        //OlcbTestInterface testIf = new OlcbTestInterface();
        scm  = new OlcbSystemConnectionMemo();
        //scm.setTrafficController(testIf.tc);
        //scm.setInterface(testIf.iface);
        TestTrafficController tc = new TestTrafficController();
        scm.setTrafficController(tc);
    }

    @After
    public void tearDown() {
        if(scm.getInterface()!=null) {
           scm.getInterface().dispose();
        }
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(OlcbConfigurationManagerTest.class);

}
