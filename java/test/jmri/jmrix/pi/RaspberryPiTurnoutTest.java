package jmri.jmrix.pi;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioProvider;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class RaspberryPiTurnoutTest {

    private GpioProvider myprovider = null;

    @Test
    public void testCTor() {
        RaspberryPiTurnout t = new RaspberryPiTurnout("PiT2");
        Assert.assertNotNull("exists",t);
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
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(RaspberryPiTurnoutTest.class.getName());

}
