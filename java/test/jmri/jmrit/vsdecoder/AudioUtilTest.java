package jmri.jmrit.vsdecoder;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class AudioUtilTest {

    // no Ctor test, tested class only supplies static methods.

    @Test
    public void testAudioUtilIsAudioRunning() {
        Assertions.assertFalse(AudioUtil.isAudioRunning());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(AudioUtilTest.class);

}
