package jmri.jmrix.tams;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TamsConstantsTest {

    // no Ctor test, class only supplies static methods.

    @Test
    public void testTamsConstants() {
        Assertions.assertEquals( 0x99, TamsConstants.XSENSOFF);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TamsConstantsTest.class);

}
