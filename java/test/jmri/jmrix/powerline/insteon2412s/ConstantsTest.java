package jmri.jmrix.powerline.insteon2412s;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ConstantsTest {

    // no Ctor test, class only supplies static methods

    @Test
    public void test2412Constants() {
        Assertions.assertEquals( 0x03,Constants.BUTTON_HELD);
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
