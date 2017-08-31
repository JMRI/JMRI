package jmri.jmrix.pi;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioProvider;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for RaspberryPiConnectionConfig class.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class RaspberryPiConnectionConfigTest {

    @Test
    public void ConstructorTest() {
        Assert.assertNotNull("ConnectionConfig constructor", new RaspberryPiConnectionConfig());
    }

    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        GpioProvider myprovider = new PiGpioProviderScaffold();
        GpioFactory.setDefaultProvider(myprovider);

        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
