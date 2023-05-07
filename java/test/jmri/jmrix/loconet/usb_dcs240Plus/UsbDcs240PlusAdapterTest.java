package jmri.jmrix.loconet.usb_dcs240Plus;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class UsbDcs240PlusAdapterTest {

    @Test
    public void testCTor() {
        UsbDcs240PlusAdapter t = new UsbDcs240PlusAdapter();
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

    // private final static Logger log = LoggerFactory.getLogger(UsbDcs240PlusAdapterTest.class);

}
