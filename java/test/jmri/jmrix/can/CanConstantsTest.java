package jmri.jmrix.can;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class CanConstantsTest {

     // no testCtor as class only supplies static methods

    @Test
    public void testCanConstants() {
        Assertions.assertEquals(0,CanConstants.CANRS);
        Assertions.assertEquals(1,CanConstants.CANUSB);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CanConstantsTest.class);

}
