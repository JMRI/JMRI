package jmri.jmrit.ussctc;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Sensor;
import jmri.Turnout;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for classes in the jmri.jmrit.ussctc.OsIndicator class
 *
 * @author	Bob Jacobsen Copyright 2003, 2007, 2015
 */
public class OsIndicatorTest {

    @Test
    public void testCreate() {
        new OsIndicator("IT12", "IS34", "IS56");
    }

    @Test
    public void testAccess() {
        OsIndicator os = new OsIndicator("IT12", "IS34", "IS56");

        Assert.assertEquals("output", "IT12", os.getOutputName());
        Assert.assertEquals("input", "IS34", os.getOsSensorName());
        Assert.assertEquals("lock", "IS56", os.getLockName());
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

        Assert.assertEquals("sensor before",
                Sensor.INACTIVE, s1.getKnownState());
        Assert.assertEquals("output before",
                Turnout.CLOSED, t1.getCommandedState());

        os.instantiate();

        Assert.assertEquals("sensor after instantiate",
                Sensor.INACTIVE, s1.getKnownState());
        Assert.assertEquals("output after instantiate",
                Turnout.CLOSED, t1.getCommandedState());
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

        Assert.assertEquals("sensor before",
                Sensor.INACTIVE, s1.getKnownState());
        Assert.assertEquals("lock before",
                Sensor.INACTIVE, s2.getKnownState());
        Assert.assertEquals("output before",
                Turnout.CLOSED, t1.getCommandedState());

        os.instantiate();

        Assert.assertEquals("sensor after instantiate",
                Sensor.INACTIVE, s1.getKnownState());
        Assert.assertEquals("lock after instantiate",
                Sensor.INACTIVE, s2.getKnownState());
        Assert.assertEquals("output after instantiate",
                Turnout.CLOSED, t1.getCommandedState());
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

        Assert.assertEquals("sensor before",
                Sensor.INACTIVE, s1.getKnownState());
        Assert.assertEquals("output before",
                Turnout.CLOSED, t1.getCommandedState());

        os.instantiate();

        Assert.assertEquals("sensor after instantiate",
                Sensor.INACTIVE, s1.getKnownState());
        Assert.assertEquals("output after instantiate",
                Turnout.CLOSED, t1.getCommandedState());

        // and change
        s1.setKnownState(Sensor.ACTIVE);

        Assert.assertEquals("sensor after activate",
                Sensor.ACTIVE, s1.getKnownState());
        Assert.assertEquals("output after activate",
                Turnout.THROWN, t1.getCommandedState());

        s1.setKnownState(Sensor.INACTIVE);

        Assert.assertEquals("sensor after inactivate",
                Sensor.INACTIVE, s1.getKnownState());
        Assert.assertEquals("output after inactivate",
                Turnout.CLOSED, t1.getCommandedState());

    }

    @Before
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();

        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
    }

    @After
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.tearDown();
    }

}
