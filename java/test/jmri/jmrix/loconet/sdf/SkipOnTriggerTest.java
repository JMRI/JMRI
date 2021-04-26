package jmri.jmrix.loconet.sdf;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SkipOnTriggerTest {

    @Test
    public void testCTor() {
        SkipOnTrigger t = new SkipOnTrigger(1,2);
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

    // private final static Logger log = LoggerFactory.getLogger(SkipOnTriggerTest.class);

}
