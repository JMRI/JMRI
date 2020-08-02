package jmri.jmrit.audio;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * Test simple functioning of JavaSoundAudioListener
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class JavaSoundAudioListenerTest {

    @Test
    public void testCtor() {
        JavaSoundAudioListener l = new JavaSoundAudioListener("test");
        Assert.assertNotNull("exists", l);
    }

    @Test
    public void testC2Stringtor() {
        JavaSoundAudioListener l = new JavaSoundAudioListener("testsysname", "testusername");
        Assert.assertNotNull("exists", l);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
