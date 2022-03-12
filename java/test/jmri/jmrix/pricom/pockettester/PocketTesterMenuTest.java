package jmri.jmrix.pricom.pockettester;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PocketTesterMenuTest {

    @Test
    public void testCTor() {
        PocketTesterMenu t = new PocketTesterMenu();
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

    // private final static Logger log = LoggerFactory.getLogger(PocketTesterMenuTest.class);

}
