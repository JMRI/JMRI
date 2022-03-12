package jmri.jmrit.audio;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * Test simple functioning of NullAudioListener
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class NullAudioListenerTest {

    @Test
    public void testCtor() {
        NullAudioListener l = new NullAudioListener("test");
        Assert.assertNotNull("exists", l);
    }

    @Test
    public void testC2Stringtor() {
        NullAudioListener l = new NullAudioListener("testsysname","testusername");
        Assert.assertNotNull("exists", l);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {        JUnitUtil.tearDown();    }
}
