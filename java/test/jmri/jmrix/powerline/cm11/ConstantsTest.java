package jmri.jmrix.powerline.cm11;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ConstantsTest {

    // no Ctor test, class only supplies static methods

    @Test
    public void testCm11Constants() {
        Assertions.assertEquals( 0x55, Constants.READY_REQ);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ConstantsTest.class);

}
