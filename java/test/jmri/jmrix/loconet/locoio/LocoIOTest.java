package jmri.jmrix.loconet.locoio;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class LocoIOTest {

    // no Ctor test, class only supplies static methods

    @Test
    public void testLocoIoConstants() {
        assertEquals( 0x0100, LocoIO.LOCOIO_BROADCAST_ADDRESS);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LocoIOTest.class);

}
