package jmri.jmrix.pi;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioProvider;
import jmri.Turnout;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.ToDo;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

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

    @Test
    public void ConstructorTest() {
        Assert.assertNotNull(l);
    }

    @Test
    public void checkPrefix() {
        Assert.assertEquals("Prefix", "P", l.getSystemPrefix());
    }

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        GpioProvider myprovider = new PiGpioProviderScaffold();
        GpioFactory.setDefaultProvider(myprovider);
        jmri.util.JUnitUtil.resetInstanceManager();
        l = new RaspberryPiTurnoutManager(new RaspberryPiSystemConnectionMemo());
    }

    @After
    public void tearDown() {
        // unprovisionPin if it exists to allow reuse of GPIO pin in next test (without need to override test)
        RaspberryPiTurnout t1 = (RaspberryPiTurnout) l.getTurnout(getSystemName(getNumToTest1()));
        if (t1 != null) {
            t1.dispose();
        }
        RaspberryPiTurnout t2 = (RaspberryPiTurnout) l.getTurnout(getSystemName(getNumToTest2()));
        if (t2 != null) {
            t2.dispose();
        }
        JUnitUtil.clearShutDownManager();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.tearDown();
    }

}
