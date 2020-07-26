package jmri.jmrix.loconet.usb_dcs240;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class UsbDcs240AdapterTest {

    @Test
    public void testCTor() {
        UsbDcs240Adapter t = new UsbDcs240Adapter();
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(UsbDcs240AdapterTest.class);

}
