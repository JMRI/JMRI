package jmri;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TransitManagerTest {

    @Test
    public void testCTor() {
        TransitManager t = new jmri.managers.DefaultTransitManager();
        Assertions.assertNotNull( t, "exists");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private static final Logger log = LoggerFactory.getLogger(TransitManagerTest.class);

}
