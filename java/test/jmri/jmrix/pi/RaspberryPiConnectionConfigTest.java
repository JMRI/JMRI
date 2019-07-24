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
public class RaspberryPiConnectionConfigTest extends jmri.jmrix.AbstractConnectionConfigTestBase {

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        GpioProvider myprovider = new PiGpioProviderScaffold();
        GpioFactory.setDefaultProvider(myprovider);

        JUnitUtil.setUp();
        cc = new RaspberryPiConnectionConfig();
    }

    @After
    @Override
    public void tearDown() {
        cc = null;
        JUnitUtil.tearDown();
    }

}
