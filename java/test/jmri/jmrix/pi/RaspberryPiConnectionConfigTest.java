package jmri.jmrix.pi;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioProvider;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for RaspberryPiConnectionConfig class.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class RaspberryPiConnectionConfigTest extends jmri.jmrix.AbstractConnectionConfigTestBase {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        GpioProvider myprovider = new PiGpioProviderScaffold();
        GpioFactory.setDefaultProvider(myprovider);
        cc = new RaspberryPiConnectionConfig();
    }

    @AfterEach
    @Override
    public void tearDown() {
        cc = null;
        JUnitUtil.tearDown();
    }

}
