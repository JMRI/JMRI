package jmri.jmrix.nce;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class NceBinaryCommandTest {

    // no Ctor test, class only supplies static methods.

    @Test
    public void testNceBinaryCommands() {
        assertArrayEquals( new byte[]{ (byte)NceMessage.READ1_CMD, 0, 2},
            NceBinaryCommand.accMemoryRead1(2));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(NceBinaryCommandTest.class);

}
