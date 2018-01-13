package jmri.jmrix.pi;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioProvider;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class RaspberryPiSensorTest {

    @Test
    public void testCTor() {
        RaspberryPiSensor t = new RaspberryPiSensor("PiS1");
        Assert.assertNotNull("exists",t);
    }

    @Test
    @Ignore("need to reset the provider")
    public void testGetPullState() {
        RaspberryPiSensor t = new RaspberryPiSensor("PiS1");
        Assert.assertEquals("default pull state",jmri.Sensor.PullResistance.PULL_DOWN,t.getPullResistance());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        GpioProvider myprovider = new PiGpioProviderScaffold();
        GpioFactory.setDefaultProvider(myprovider);
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(RaspberryPiSensorTest.class);

}
