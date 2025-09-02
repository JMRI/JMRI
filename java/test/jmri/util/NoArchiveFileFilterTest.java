package jmri.util;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class NoArchiveFileFilterTest {

    @Test
    public void testCTor() {
        NoArchiveFileFilter t = new NoArchiveFileFilter();
        Assertions.assertNotNull(t, "exists");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(NoArchiveFileFilterTest.class);

}
