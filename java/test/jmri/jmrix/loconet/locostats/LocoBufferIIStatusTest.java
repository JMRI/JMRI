package jmri.jmrix.loconet.locostats;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class LocoBufferIIStatusTest {

    @Test
    public void testCTor() {
        LocoBufferIIStatus t = new LocoBufferIIStatus(0,0,0);
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

    // private final static Logger log = LoggerFactory.getLogger(LocoBufferIIStatusTest.class);

}
