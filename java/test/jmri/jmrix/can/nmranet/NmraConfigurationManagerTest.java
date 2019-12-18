package jmri.jmrix.can.nmranet;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of NmraConfigurationManager
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class NmraConfigurationManagerTest {
        
 
    jmri.jmrix.can.TrafficControllerScaffold tcs = null;
    jmri.jmrix.can.CanSystemConnectionMemo memo = null;

    @Test
    public void testCtor() {
        NmraConfigurationManager cfm = new NmraConfigurationManager(memo);
        Assert.assertNotNull("exists", cfm);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        tcs = new jmri.jmrix.can.TrafficControllerScaffold();
        memo = new jmri.jmrix.can.CanSystemConnectionMemo();
        memo.setTrafficController(tcs);

    }

    @After
    public void tearDown() {        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }


}
