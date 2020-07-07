package jmri.jmrix.direct;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ThrottleManagerTest extends jmri.managers.AbstractThrottleManagerTestBase {

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists", tm);
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();

        DirectSystemConnectionMemo m = new DirectSystemConnectionMemo();
        tm = new ThrottleManager(m);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ThrottleManagerTest.class);

}
