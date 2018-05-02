package jmri.jmrit.audio;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of JavaSoundAudioBuffer
 *
 * @author	Paul Bender Copyright (C) 2017
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

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
