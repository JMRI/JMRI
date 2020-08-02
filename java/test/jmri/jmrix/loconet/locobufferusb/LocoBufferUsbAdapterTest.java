package jmri.jmrix.loconet.locobufferusb;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class LocoBufferUsbAdapterTest {

    @Test
    public void testCTor() {
        LocoBufferUsbAdapter t = new LocoBufferUsbAdapter();
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

    // private final static Logger log = LoggerFactory.getLogger(LocoBufferUsbAdapterTest.class);

}
