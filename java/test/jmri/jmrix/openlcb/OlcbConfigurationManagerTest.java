package jmri.jmrix.openlcb;

import jmri.jmrix.can.TestTrafficController;
import jmri.util.JUnitUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

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
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(OlcbConfigurationManagerTest.class);

}
