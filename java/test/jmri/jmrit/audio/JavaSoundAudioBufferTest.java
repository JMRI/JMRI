package jmri.jmrit.audio;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * Test simple functioning of JavaSoundAudioBuffer
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class JavaSoundAudioBufferTest {

    @Test
    public void testCtor() {
        JavaSoundAudioBuffer l = new JavaSoundAudioBuffer("test");
        Assert.assertNotNull("exists", l);
    }

    @Test
    public void testC2Stringtor() {
        JavaSoundAudioBuffer l = new JavaSoundAudioBuffer("testsysname","testusername");
        Assert.assertNotNull("exists", l);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {        JUnitUtil.tearDown();    }
}
