package jmri.jmrix.can.nmranet;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of NmraConfigurationManager
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class NmraConfigurationManagerTest {
        
 
    jmri.jmrix.can.TrafficControllerScaffold tcs = null;
    jmri.jmrix.can.CanSystemConnectionMemo memo = null;

    @Test
    public void testCtor() {
        NmraConfigurationManager cfm = new NmraConfigurationManager(memo);
        Assert.assertNotNull("exists", cfm);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        tcs = new jmri.jmrix.can.TrafficControllerScaffold();
        memo = new jmri.jmrix.can.CanSystemConnectionMemo();
        memo.setTrafficController(tcs);

    }

    @AfterEach
    public void tearDown() {        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }


}
