package jmri.jmrix.powerline.simulator;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ConstantsTest {

    // no Ctor test, class only supplies static methods

    @Test
    public void testPowerlineSimConstants() {
        Assertions.assertEquals( 0x12, Constants.CMD_LIGHT_ON_FAST);
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
