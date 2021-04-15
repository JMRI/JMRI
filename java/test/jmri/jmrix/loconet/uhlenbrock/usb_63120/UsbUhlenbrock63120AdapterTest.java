package jmri.jmrix.loconet.uhlenbrock.usb_63120;

import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017, 2020
 */
public class UsbUhlenbrock63120AdapterTest {

    @Test
    public void testCTor() {
        UsbUhlenbrock63120Adapter t = new UsbUhlenbrock63120Adapter();
        Assertions.assertNotNull(t, "exists");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
