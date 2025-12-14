package jmri.jmrix.mrc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class MrcPacketsTest {

    // no Ctor test, class only supplies static methods

    @Test
    public void testMrcPacketConstants() {
        assertEquals( 238, MrcPackets.BADCMDRECEIVEDCODE );
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(MrcPacketsTest.class);

}
