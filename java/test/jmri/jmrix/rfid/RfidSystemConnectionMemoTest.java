package jmri.jmrix.rfid;

import javax.annotation.Nonnull;

import jmri.Reporter;
import jmri.Sensor;
import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.jmrix.rfid.RfidSystemConnectionMemo class.
 *
 * @author Paul Bender
 */
public class RfidSystemConnectionMemoTest extends SystemConnectionMemoTestBase<RfidSystemConnectionMemo> {

    @Override
    @Test
    public void testProvidesConsistManager() {
        Assert.assertFalse("Provides ConsistManager", scm.provides(jmri.ConsistManager.class));
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        scm = new RfidSystemConnectionMemo();
        RfidTrafficController tc = new RfidTrafficController() {
            @Override
            public void sendInitString() {
            }

            @Override
            public void transmitLoop() {
            }

            @Override
            public void receiveLoop() {
            }
        };
        scm.setRfidTrafficController(tc);
        RfidSensorManager s = new RfidSensorManager(scm) {
            @Override
            @Nonnull
            protected Sensor createNewSensor(@Nonnull String systemName, String userName) throws IllegalArgumentException {
                return new RfidSensor(systemName, userName);
            }

            @Override
            public void message(RfidMessage m) {
            }

            @Override
            public void reply(RfidReply m) {
            }

        };
        RfidReporterManager r = new RfidReporterManager(scm) {
            @Override
            @Nonnull
            protected Reporter createNewReporter(@Nonnull String systemName, String userName) throws IllegalArgumentException {
                return new RfidReporter(systemName, userName);
            }

            @Override
            public void message(RfidMessage m) {
            }

            @Override
            public void reply(RfidReply m) {
            }

        };
        scm.configureManagers(s, r);
    }

    @Override
    @AfterEach
    public void tearDown() {
        scm.getTrafficController().terminateThreads();
        scm.dispose();
        JUnitUtil.tearDown();

    }

}
