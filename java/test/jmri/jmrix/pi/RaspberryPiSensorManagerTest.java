package jmri.jmrix.pi;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioProvider;
import jmri.Sensor;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.ToDo;
import org.junit.*;

/**
 * Tests for RaspberryPiSensorManager.
 * <p>
 * Resets the GPIO support by disposing the sensors + pins.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class RaspberryPiSensorManagerTest extends jmri.managers.AbstractSensorMgrTestBase {

    @Override
    public String getSystemName(int i) {
        return l.getSystemPrefix() + "S" + i;
    }

    @Test
    public void ConstructorTest(){
        Assert.assertNotNull(l);
    }

    @Test
    public void checkPrefix(){
        Assert.assertEquals("Prefix", "P", l.getSystemPrefix());
    }

    @Override
    @Test
    public void testPullResistanceConfigurable(){
        Assert.assertTrue("Pull Resistance Configurable", l.isPullResistanceConfigurable());
    }

    @Override
    @Before
    public void setUp() {
       JUnitUtil.setUp();
       GpioProvider myprovider = new PiGpioProviderScaffold();
       GpioFactory.setDefaultProvider(myprovider);
       JUnitUtil.resetInstanceManager();
       l = new RaspberryPiSensorManager(new RaspberryPiSystemConnectionMemo());
    }

    @After
    public void tearDown() {
        // unprovisionPin if it exists to allow reuse of GPIO pin in next test (without need to override test)
        RaspberryPiSensor t1 = (RaspberryPiSensor) l.getSensor(getSystemName(getNumToTest1()));
        if (t1 != null) {
            t1.dispose();
        }
        RaspberryPiSensor t2 = (RaspberryPiSensor) l.getSensor(getSystemName(getNumToTest2()));
        if (t2 != null) {
            t2.dispose();
        }
        JUnitUtil.clearShutDownManager();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.tearDown();
    }

}
