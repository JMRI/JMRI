package jmri.jmrit.audio;

import jmri.InstanceManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import org.junit.Assume;

/**
 * Test simple functioning of JoalAudioBuffer
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class JoalAudioBufferTest {

    @Test
    public void testCtor() {
        JoalAudioBuffer l = new JoalAudioBuffer("test");

        Assert.assertNotNull("exists", l);
        Assert.assertEquals("test", l.getSystemName());
    }

    @Test
    public void testC2Stringtor() {
        Assume.assumeNotNull(JoalAudioFactory.getAL()); // Run test method only when JOAL is present.

        JoalAudioBuffer l = new JoalAudioBuffer("testsysname","testusername");

        Assert.assertNotNull("exists", l);
        Assert.assertEquals("testsysname", l.getSystemName());
        Assert.assertEquals("testusername", l.getUserName());
        Assert.assertEquals("testsysname", l.toString());
        Assert.assertEquals(0, l.getLength());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.AudioManager am = new DefaultAudioManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
        jmri.InstanceManager.setDefault(jmri.AudioManager.class,am);
        am.init();
    }

    @AfterEach
    public void tearDown() {
        // this created an audio manager, clean that up
        InstanceManager.getDefault(jmri.AudioManager.class).cleanup();

        jmri.util.JUnitAppender.suppressErrorMessage("Unhandled audio format type 0");
        JUnitUtil.tearDown();
    }
}
