package jmri.jmrix.dcc4pc;

import jmri.InstanceManager;
import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * JUnit tests for the Dcc4PcSystemConnectionMemo class
 * <p>
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class Dcc4PcSystemConnectionMemoTest extends SystemConnectionMemoTestBase<Dcc4PcSystemConnectionMemo> {

    @Override
    @Test
    public void testProvidesConsistManager() {
        // Dcc4Pc systems report being able to provide an addresed programmer, 
        // but they really just forward it to another connection. 
        Assert.assertFalse("Provides ConsistManager", scm.provides(jmri.ConsistManager.class));
    }

    // The "Programmer" option names a connection that may start after this one. The ops-mode
    // programmer is offered straight away, so Preferences > Defaults can always choose it.
    @Test
    public void testProgrammerFromConnectionStartedLater() {
        scm.useProgrammerFrom("Test Station");
        Assert.assertTrue("offered before the other connection starts",
                scm.provides(jmri.AddressedProgrammerManager.class));
        jmri.AddressedProgrammerManager apm = scm.get(jmri.AddressedProgrammerManager.class);
        Assert.assertTrue("ops mode reads through DCC4PC", apm instanceof Dcc4PcProgrammerManager);
        Assert.assertEquals("listed under this connection", scm.getUserName(), apm.getUserName());
        Assert.assertFalse("no consist manager", scm.provides(jmri.ConsistManager.class));

        Assert.assertNull("no programmer while the other connection is missing",
                apm.getAddressedProgrammer(true, 9328));
        jmri.util.JUnitAppender.assertWarnMessageStartingWith("Dcc4Pc: no connection to send Programming on Main packets");

        new jmri.jmrix.internal.InternalSystemConnectionMemo("T", "Test Station", false);
        Assert.assertNotNull("programmer once it has started", apm.getAddressedProgrammer(true, 9328));
    }

    // With the Programmer option blank, the default command station's connection sends the packets.
    @Test
    public void testProgrammerFromDefaultCommandStation() {
        scm.useProgrammerFrom("");
        Assert.assertTrue(scm.provides(jmri.AddressedProgrammerManager.class));
        jmri.AddressedProgrammerManager apm = scm.get(jmri.AddressedProgrammerManager.class);

        jmri.managers.ManagerDefaultSelector defaults = new jmri.managers.ManagerDefaultSelector();
        InstanceManager.store(defaults, jmri.managers.ManagerDefaultSelector.class);
        new jmri.jmrix.internal.InternalSystemConnectionMemo("T", "Test Station", false);
        defaults.setDefault(jmri.CommandStation.class, "Test Station");
        Assert.assertNotNull("programmer through the default command station", apm.getAddressedProgrammer(true, 9328));
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        Dcc4PcTrafficController tc = new Dcc4PcTrafficController() {
            @Override
            public void sendDcc4PcMessage(Dcc4PcMessage m, Dcc4PcListener reply) {
            }

            @Override
            public void transmitLoop() {
            }

            @Override
            public void receiveLoop() {
            }
        };
        scm = new Dcc4PcSystemConnectionMemo(tc);
        InstanceManager.setDefault(Dcc4PcSystemConnectionMemo.class, scm);
    }

    @AfterEach
    @Override
    public void tearDown() {
        scm.getDcc4PcTrafficController().terminateThreads();
        scm.dispose();
        JUnitUtil.clearShutDownManager(); // the RailCom tag manager registers one when an ops programmer is made
        JUnitUtil.tearDown();
    }

}
