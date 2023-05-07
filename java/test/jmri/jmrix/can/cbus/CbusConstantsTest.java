package jmri.jmrix.can.cbus;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class CbusConstantsTest {

    // no testCtor as class only supplies static methods

    @Test
    public void testExists() {
        Assertions.assertEquals(0x90, CbusConstants.CBUS_ACON);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusConstantsTest.class);

}
