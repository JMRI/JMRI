package jmri.jmrix.pi;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioProvider;

import jmri.InstanceManager;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for RaspberryPiTurnoutManager.
 * <p>
 * Resets the GPIO support by disposing the turnouts + pins.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class RaspberryPiTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTestBase {

    @Override
    public String getSystemName(int i) {
        return l.getSystemPrefix() + "T" + i;
    }
    
    @Override
    protected String getASystemNameWithNoPrefix() {
        return "" + getNumToTest2();
    }

    @Test
    public void ConstructorTest() {
        Assert.assertNotNull(l);
    }

    @Test
    public void checkPrefix() {
        Assert.assertEquals("Prefix", "P", l.getSystemPrefix());
    }

    private GpioProvider myProvider;

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        myProvider = new PiGpioProviderScaffold();
        GpioFactory.setDefaultProvider(myProvider);
        l = new RaspberryPiTurnoutManager(new RaspberryPiSystemConnectionMemo());
    }

    @AfterEach
    public void tearDown() {
        // unprovisionPin if it exists to allow reuse of GPIO pin in next test (without need to override test)
        RaspberryPiTurnout t1 = (RaspberryPiTurnout) l.getTurnout(getSystemName(getNumToTest1()));
        if (t1 != null) {
            t1.dispose();
        }
        t1 = (RaspberryPiTurnout) l.getTurnout(getSystemName(getNumToTest2()));
        if (t1 != null) {
            t1.dispose();
        }
        t1 = (RaspberryPiTurnout) l.getTurnout(getSystemName(1));
        if (t1 != null) {
            t1.dispose();
        }
        t1 = (RaspberryPiTurnout) l.getTurnout(getSystemName(2));
        if (t1 != null) {
            t1.dispose();
        }
        RaspberryPiSensor s1 = (RaspberryPiSensor) InstanceManager.sensorManagerInstance().getSensor("PS1");
        if (s1 != null) {
            s1.dispose();
        }
        s1 = (RaspberryPiSensor) InstanceManager.sensorManagerInstance().getSensor("PS2");
        if (s1 != null) {
            s1.dispose();
        }
        // shutdown() will forcefully shutdown all GPIO monitoring threads and scheduled tasks, includes unexport.pin
        myProvider.shutdown();
        // GpioFactory.setDefaultProvider(null);
        l.dispose();

        JUnitUtil.clearShutDownManager();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.tearDown();
    }

}
