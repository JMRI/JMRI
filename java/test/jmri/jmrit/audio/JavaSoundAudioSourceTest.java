package jmri.jmrit.audio;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of JavaSoundAudioSource
 *
 * @author	Paul Bender Copyright (C) 2017
 */
public class JavaSoundAudioSourceTest {

    @Test
    public void testCtor() {
        JavaSoundAudioSource l = new JavaSoundAudioSource("test");
        Assert.assertNotNull("exists", l);
    }

    @Test
    public void testC2Stringtor() {
        JavaSoundAudioSource l = new JavaSoundAudioSource("testsysname","testusername");
        Assert.assertNotNull("exists", l);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.AudioManager am = new DefaultAudioManager();
        jmri.InstanceManager.setDefault(jmri.AudioManager.class,am);
        am.init();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
