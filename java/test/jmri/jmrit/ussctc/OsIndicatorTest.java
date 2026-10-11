package jmri.jmrit.ussctc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Sensor;
import jmri.Turnout;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for classes in the jmri.jmrit.ussctc.OsIndicator class
 *
 * @author Bob Jacobsen Copyright 2003, 2007, 2015
 */
public class OsIndicatorTest {

    @Test
    public void testCreate() {
        Assertions.assertNotNull( new OsIndicator("IT12", "IS34", "IS56"));
    }

    @Test
    public void testAccess() {
        OsIndicator os = new OsIndicator("IT12", "IS34", "IS56");

        assertEquals("IT12", os.getOutputName(), "output");
        assertEquals("IS34", os.getOsSensorName(), "input");
        assertEquals("IS56", os.getLockName(), "lock");
    }

    @Test
    public void testIntantiateNoLock() throws JmriException {
        OsIndicator os = new OsIndicator("IT12", "IS34", "");

        Turnout t1 = InstanceManager.turnoutManagerInstance()
                .provideTurnout("IT12");
        t1.setCommandedState(Turnout.CLOSED);

        Sensor s1 = InstanceManager.sensorManagerInstance()
                .provideSensor("IS34");
        s1.setKnownState(Sensor.INACTIVE);

        assertEquals(Sensor.INACTIVE, s1.getKnownState(),
                "sensor before");
        assertEquals(Turnout.CLOSED, t1.getCommandedState(),
                "output before");

        os.instantiate();

        assertEquals(Sensor.INACTIVE, s1.getKnownState(),
                "sensor after instantiate");
        assertEquals(Turnout.CLOSED, t1.getCommandedState(),
                "output after instantiate");
    }

    @Test
    public void testIntantiateLocked() throws JmriException {
        OsIndicator os = new OsIndicator("IT12", "IS34", "IS56");

        Turnout t1 = InstanceManager.turnoutManagerInstance()
                .provideTurnout("IT12");
        t1.setCommandedState(Turnout.CLOSED);

        Sensor s1 = InstanceManager.sensorManagerInstance()
                .provideSensor("IS34");
        s1.setKnownState(Sensor.INACTIVE);

        Sensor s2 = InstanceManager.sensorManagerInstance()
                .provideSensor("IS56");
        s2.setKnownState(Sensor.INACTIVE);

        assertEquals(Sensor.INACTIVE, s1.getKnownState(),
                "sensor before");
        assertEquals(Sensor.INACTIVE, s2.getKnownState(),
                "lock before");
        assertEquals(Turnout.CLOSED, t1.getCommandedState(),
                "output before");

        os.instantiate();

        assertEquals(Sensor.INACTIVE, s1.getKnownState(),
                "sensor after instantiate");
        assertEquals(Sensor.INACTIVE, s2.getKnownState(),
                "lock after instantiate");
        assertEquals(Turnout.CLOSED, t1.getCommandedState(),
                "output after instantiate");
    }

    @Test
    public void testInvokeNoLock() throws JmriException {
        OsIndicator os = new OsIndicator("IT12", "IS34", "");

        Turnout t1 = InstanceManager.turnoutManagerInstance()
                .provideTurnout("IT12");
        t1.setCommandedState(Turnout.CLOSED);

        Sensor s1 = InstanceManager.sensorManagerInstance()
                .provideSensor("IS34");
        s1.setKnownState(Sensor.INACTIVE);

        assertEquals(Sensor.INACTIVE, s1.getKnownState(),
                "sensor before");
        assertEquals(Turnout.CLOSED, t1.getCommandedState(),
                "output before");

        os.instantiate();

        assertEquals(Sensor.INACTIVE, s1.getKnownState(),
                "sensor after instantiate");
        assertEquals(Turnout.CLOSED, t1.getCommandedState(),
                "output after instantiate");

        // and change
        s1.setKnownState(Sensor.ACTIVE);

        assertEquals(Sensor.ACTIVE, s1.getKnownState(),
                "sensor after activate");
        assertEquals(Turnout.THROWN, t1.getCommandedState(),
                "output after activate");

        s1.setKnownState(Sensor.INACTIVE);

        assertEquals(Sensor.INACTIVE, s1.getKnownState(),
                "sensor after inactivate");
        assertEquals(Turnout.CLOSED, t1.getCommandedState(),
                "output after inactivate");

    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
