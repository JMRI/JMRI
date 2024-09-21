package jmri.jmrix.sprog;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SprogConstantsTest {

    @Test
    public void testSprogConstants() {
        Assertions.assertEquals(50, SprogConstants.PACKET_DELAY_WARN_THRESHOLD);

    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SprogConstantsTest.class);

}
