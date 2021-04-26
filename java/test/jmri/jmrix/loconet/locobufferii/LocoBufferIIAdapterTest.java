package jmri.jmrix.loconet.locobufferii;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class LocoBufferIIAdapterTest {

    @Test
    public void testCTor() {
        LocoBufferIIAdapter t = new LocoBufferIIAdapter();
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

    // private final static Logger log = LoggerFactory.getLogger(LocoBufferIIAdapterTest.class);

}
