package jmri.jmrix.dccpp;

import jmri.InstanceManager;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * DCCppInitializationManagerTest.java
 *
 * Test for the jmri.jmrix.dccpp.DCCppInitializationManager class
 *
 * @author Paul Bender
 * @author Mark Underwood
 */
public class DCCppInitializationManagerTest {

    @Test
    public void testCtor() {

        // infrastructure objects
        DCCppInterfaceScaffold t = new DCCppInterfaceScaffold(new DCCppCommandStation());
        DCCppListenerScaffold l = new DCCppListenerScaffold();
        
        DCCppSystemConnectionMemo memo = new DCCppSystemConnectionMemo(t);
        InstanceManager.setMeterManager(new jmri.managers.AbstractMeterManager(memo));

        DCCppInitializationManager m = new DCCppInitializationManager(memo);
        Assert.assertNotNull("exists", t);
        Assert.assertNotNull("exists", l);
        Assert.assertNotNull("exists", m);
        Assert.assertNotNull("exists", memo);
        //jmri.util.JUnitAppender.assertWarnMessage("Command Station disconnected, or powered down");
    }
    
    @BeforeEach
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        jmri.util.JUnitUtil.tearDown();

    }

}
