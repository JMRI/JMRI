package jmri.jmrix.dccpp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import jmri.InstanceManager;

import org.junit.jupiter.api.*;

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
        assertNull(l.rcvdMsg);
        assertNull(l.rcvdRply);
        assertNull(l.timeOutMsg);
        assertEquals(0, l.rcvCount);

        DCCppSystemConnectionMemo memo = new DCCppSystemConnectionMemo(t);
        InstanceManager.setMeterManager(new jmri.managers.AbstractMeterManager(memo));

        DCCppInitializationManager m = new DCCppInitializationManager(memo);
        assertNotNull( t, "exists");
        assertNotNull( l, "exists");
        assertNotNull( m, "exists");
        assertNotNull( memo, "exists");
        //jmri.util.JUnitAppender.assertWarnMessage("Command Station disconnected, or powered down");

        t.terminateThreads();
    }
    
    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();

    }

}
