package jmri.jmrix.pi;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioProvider;

import jmri.InstanceManager;
import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for RaspberryPiSystemConnectionMemo.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class RaspberryPiSystemConnectionMemoTest extends SystemConnectionMemoTestBase<RaspberryPiSystemConnectionMemo> {

    @Test
    public void checkProvidesSensorManager() {
        Assert.assertTrue(scm.provides(jmri.SensorManager.class));
    }

    @Test
    public void checkProvidesWhenDisabled() {
        scm.setDisabled(true);
        Assert.assertFalse(scm.provides(jmri.SensorManager.class));
    }

    @Test
    public void checkProvidesTurnoutManager() {
        Assert.assertTrue(scm.provides(jmri.TurnoutManager.class));
    }

    @Test
    public void checkProvidesLightManager() {
        Assert.assertFalse(scm.provides(jmri.LightManager.class)); //false until implemented.
    }
    
    @Override
    @Test
    public void testProvidesConsistManager() {
        Assert.assertFalse(scm.provides(jmri.ConsistManager.class));
    }

    @Test
    public void checkProvidesOtherManager() {
        Assert.assertFalse(scm.provides(jmri.GlobalProgrammerManager.class));
    }

    @Test
    public void setAndGetSensorManager() {
        RaspberryPiSensorManager sm = new RaspberryPiSensorManager(scm);
        scm.setSensorManager(sm);
        Assert.assertSame("Sensor Manager", sm, scm.getSensorManager());
    }

    @Test
    public void setAndGetTurnoutManager() {
        RaspberryPiTurnoutManager sm = new RaspberryPiTurnoutManager(scm);
        scm.setTurnoutManager(sm);
        Assert.assertSame("Turnout Manager", sm, scm.getTurnoutManager());
    }

    @Test
    public void setAndGetLightManager() {
        Assert.assertNull("Light Manager", scm.getLightManager());
    }

    @Test
    public void checkConfigureManagers() {
        scm.configureManagers();
        Assert.assertNotNull("Sensor Manager after configureManagers", scm.getSensorManager());
        Assert.assertNotNull("Turnout Manager after configureManagers", scm.getTurnoutManager());
        Assert.assertNull("Light Manager after configureManagers", scm.getLightManager());
    }

    @Test
    public void checkGetSensorManager() {
        scm.configureManagers();
        Assert.assertNotNull(scm.get(jmri.SensorManager.class));
    }

    @Test
    public void checkGetSensorManagerWhenDisabled() {
        scm.configureManagers();
        scm.setDisabled(true);
        Assert.assertNull(scm.get(jmri.SensorManager.class));
    }

    @Test
    public void checkGetTurnoutManager() {
        scm.configureManagers();
        Assert.assertNotNull(scm.get(jmri.TurnoutManager.class));
    }

    @Test
    public void checkGetLightManager() {
        scm.configureManagers();
        Assert.assertNull(scm.get(jmri.LightManager.class)); // null until implemented.
    }

    @Test
    public void checkGetOtherManager() {
        scm.configureManagers();
        Assert.assertNull(scm.get(jmri.GlobalProgrammerManager.class));
    }

    @Test
    public void checkDispose() {
        // verify the connection is registered
        Assert.assertNotNull(InstanceManager.getDefault(RaspberryPiSystemConnectionMemo.class));
        scm.dispose();
        // after dispose, should be deregistered.
        Assert.assertNull(InstanceManager.getNullableDefault(RaspberryPiSystemConnectionMemo.class));
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        
        GpioProvider myprovider = new PiGpioProviderScaffold();
        GpioFactory.setDefaultProvider(myprovider);

        scm = new RaspberryPiSystemConnectionMemo();
        scm.configureManagers();
    }

    @AfterEach
    @Override
    public void tearDown() {
        scm = null;
        JUnitUtil.tearDown();
    }

}
