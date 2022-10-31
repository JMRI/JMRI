package jmri.util;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class JmriInsetsTest {

    // no testCtor as class only supplies static method

    @Test
    public void testGetInsets() {
        Assertions.assertNotNull(JmriInsets.getInsets(),"exists");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(JmriInsetsTest.class);

}
