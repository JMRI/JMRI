package jmri.jmrix.ecos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.ShutDownManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * EcosSensorManagerTest.java
 *
 * Test for the EcosSensorManager class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class EcosSensorManagerTest extends jmri.managers.AbstractSensorMgrTestBase {

    @Override
    public String getSystemName(int i) {
        return "US" + i;
    }

    private EcosTrafficController tc;

    @Test
    public void testIncomingReplyGivesItsReceivedTime() {
        EcosSensorManager m = (EcosSensorManager) l;
        // the ECoS lists its feedback modules: module 100 has 8 ports
        m.reply(new EcosReply("<REPLY queryObjects(26, ports)>\n100 ports[8]\n<END 0 (OK)>\n"));
        Sensor s = m.getSensor("US100:01");
        assertNotNull(s, "port 1 of module 100");

        List<Long> times = new ArrayList<>();
        List<Integer> states = new ArrayList<>();
        s.addRawStateListener((sensor, oldState, newState, timeNanos) -> {
            states.add(newState);
            times.add(timeNanos);
        });

        EcosReply rActive = new EcosReply("<EVENT 100>\n100 state[0x1]\n<END 0 (OK)>\n");
        rActive.setReceivedTime(1234L);
        m.reply(rActive);

        long before = System.nanoTime();
        // not stamped, no time recorded
        m.reply(new EcosReply("<EVENT 100>\n100 state[0x0]\n<END 0 (OK)>\n"));
        long after = System.nanoTime();

        assertEquals(List.of(Sensor.ACTIVE, Sensor.INACTIVE), states);
        assertEquals(1234L, times.get(0), "the time the reply was received");
        assertTrue(times.get(1) >= before && times.get(1) <= after, "not stamped: dated when set");
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initDefaultUserMessagePreferences();
        tc = new EcosInterfaceScaffold();
        EcosSystemConnectionMemo memo = new EcosSystemConnectionMemo(tc);
        l = new EcosSensorManager(memo);
        InstanceManager.getDefault(ShutDownManager.class).deregister(memo.getPreferenceManager().ecosPreferencesShutDownTask);
    }

    @AfterEach
    public void tearDown() {
        l.dispose();
        tc.terminateThreads();
        l = null;
        tc = null;
        JUnitUtil.tearDown();
    }

}
