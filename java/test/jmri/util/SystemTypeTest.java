package jmri.util;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SystemTypeTest {

    // no testCtor as tested class only supplies static methods

    @Test
    public void testFindsThisSystem() {
        Assertions.assertNotNull(SystemType.getOSName());
        Assertions.assertTrue(SystemType.getType() > 0);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SystemTypeTest.class);

}
