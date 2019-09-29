package jmri.jmrix.pi;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioProvider;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for RaspberryPiSystemConnectionMemo.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class RaspberryPiSystemConnectionMemoTest extends jmri.jmrix.SystemConnectionMemoTestBase {

    @Test
    public void checkProvidesSensorManager() {
        RaspberryPiSystemConnectionMemo m = (RaspberryPiSystemConnectionMemo)scm;
        Assert.assertTrue(m.provides(jmri.SensorManager.class));
    }

    @Test
    public void checkProvidesWhenDisabled() {
        RaspberryPiSystemConnectionMemo m = (RaspberryPiSystemConnectionMemo)scm;
        m.setDisabled(true);
        Assert.assertFalse(m.provides(jmri.SensorManager.class));
    }

    @Test
    public void checkProvidesTurnoutManager() {
        RaspberryPiSystemConnectionMemo m = (RaspberryPiSystemConnectionMemo)scm;
        Assert.assertTrue(m.provides(jmri.TurnoutManager.class));
    }

    @Test
    public void checkProvidesLightManager() {
        RaspberryPiSystemConnectionMemo m = (RaspberryPiSystemConnectionMemo)scm;
        Assert.assertFalse(m.provides(jmri.LightManager.class)); //false until implemented.
    }
    
    @Override
    @Test
    public void testProvidesConsistManager() {
        RaspberryPiSystemConnectionMemo m = (RaspberryPiSystemConnectionMemo)scm;
        Assert.assertFalse(m.provides(jmri.ConsistManager.class));
    }

    @Test
    public void checkProvidesOtherManager() {
        RaspberryPiSystemConnectionMemo m = (RaspberryPiSystemConnectionMemo)scm;
        Assert.assertFalse(m.provides(jmri.GlobalProgrammerManager.class));
    }

    @Test
    public void setAndGetSensorManager() {
        RaspberryPiSystemConnectionMemo m = (RaspberryPiSystemConnectionMemo)scm;
        RaspberryPiSensorManager sm = new RaspberryPiSensorManager(m);
        m.setSensorManager(sm);
        Assert.assertSame("Sensor Manager", sm, m.getSensorManager());
    }

    @Test
    public void setAndGetTurnoutManager() {
        RaspberryPiSystemConnectionMemo m = (RaspberryPiSystemConnectionMemo)scm;
        RaspberryPiTurnoutManager sm = new RaspberryPiTurnoutManager(m);
        m.setTurnoutManager(sm);
        Assert.assertSame("Turnout Manager", sm, m.getTurnoutManager());
    }

    @Test
    public void setAndGetLightManager() {
        RaspberryPiSystemConnectionMemo m = (RaspberryPiSystemConnectionMemo)scm;
        //RaspberryPiLightManager sm = new RaspberryPiLightManager(m.getSystemPrefix());
        //m.setTurnoutManager(sm);
        //Assert.assertSame("Light Manager", sm, m.getLightManager());
        Assert.assertNull("Light Manager", m.getLightManager());
    }

    @Test
    public void checkConfigureManagers() {
        RaspberryPiSystemConnectionMemo m = (RaspberryPiSystemConnectionMemo)scm;
        m.configureManagers();
        Assert.assertNotNull("Sensor Manager after configureManagers", m.getSensorManager());
        Assert.assertNotNull("Turnout Manager after configureManagers", m.getTurnoutManager());
        Assert.assertNull("Light Manager after configureManagers", m.getLightManager());
    }

    @Test
    public void checkGetSensorManager() {
        RaspberryPiSystemConnectionMemo m = (RaspberryPiSystemConnectionMemo)scm;
        m.configureManagers();
        Assert.assertNotNull(m.get(jmri.SensorManager.class));
    }

    @Test
    public void checkGetSensorManagerWhenDisabled() {
        RaspberryPiSystemConnectionMemo m = (RaspberryPiSystemConnectionMemo)scm;
        m.configureManagers();
        m.setDisabled(true);
        Assert.assertNull(m.get(jmri.SensorManager.class));
    }

    @Test
    public void checkGetTurnoutManager() {
        RaspberryPiSystemConnectionMemo m = (RaspberryPiSystemConnectionMemo)scm;
        m.configureManagers();
        Assert.assertNotNull(m.get(jmri.TurnoutManager.class));
    }

    @Test
    public void checkGetLightManager() {
        RaspberryPiSystemConnectionMemo m = (RaspberryPiSystemConnectionMemo)scm;
        m.configureManagers();
        Assert.assertNull(m.get(jmri.LightManager.class)); // null until implemented.
    }

    @Test
    public void checkGetOtherManager() {
        RaspberryPiSystemConnectionMemo m = (RaspberryPiSystemConnectionMemo)scm;
        m.configureManagers();
        Assert.assertNull(m.get(jmri.GlobalProgrammerManager.class));
    }

    @Test
    public void checkDispose() {
        RaspberryPiSystemConnectionMemo m = (RaspberryPiSystemConnectionMemo)scm;
        // verify the connection is registered
        Assert.assertNotNull(jmri.InstanceManager.getDefault(RaspberryPiSystemConnectionMemo.class));
        m.dispose();
        // after dispose, should be deregistered.
        Assert.assertNull(jmri.InstanceManager.getNullableDefault(RaspberryPiSystemConnectionMemo.class));
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        
        GpioProvider myprovider = new PiGpioProviderScaffold();
        GpioFactory.setDefaultProvider(myprovider);

        RaspberryPiSystemConnectionMemo memo = new RaspberryPiSystemConnectionMemo();
        memo.configureManagers();
        scm = memo;
    }

    @After
    @Override
    public void tearDown() {
        scm = null;
        JUnitUtil.tearDown();
    }

}
