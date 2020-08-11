package jmri.jmrix.loconet.locostats;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PR3MS100ModeStatusTest {

    @Test
    public void testCTor() {
        PR3MS100ModeStatus t = new PR3MS100ModeStatus(0,0,0);
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

    // private final static Logger log = LoggerFactory.getLogger(PR3MS100ModeStatusTest.class);

}
