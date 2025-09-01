package jmri.util;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class JmriJFrameActionTest {

    @Test
    public void testCTor() {
        JmriJFrameAction t = new JmriJFrameAction("Test");
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

    // private final static Logger log = LoggerFactory.getLogger(JmriJFrameActionTest.class);

}
