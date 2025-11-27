package jmri.managers;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class DeferringProgrammerManagerTest {

    @Test
    public void testCTor() {
        DeferringProgrammerManager t = new DeferringProgrammerManager();
        Assertions.assertNotNull( t, "exists");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DeferringProgrammerManagerTest.class);

}
