package jmri.jmrix.openlcb;

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

    @Test
    public void testCTor() {
        OlcbConfigurationManager t = new OlcbConfigurationManager(new OlcbSystemConnectionMemo());
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testConfigureManagers() {
        jmri.jmrix.can.TrafficController tc = new jmri.jmrix.can.TestTrafficController();
        OlcbSystemConnectionMemo memo = new OlcbSystemConnectionMemo();
        memo.setTrafficController(tc);
        OlcbConfigurationManager t = new OlcbConfigurationManager(memo);
        // this tet verifies this does not throw an exception
        t.configureManagers(); 
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(OlcbConfigurationManagerTest.class);

}
