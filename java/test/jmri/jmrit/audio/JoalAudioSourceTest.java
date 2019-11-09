package jmri.jmrit.audio;

import jmri.InstanceManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of JoalAudioSource
 *
 * @author	Paul Bender Copyright (C) 2017
 */
public class JoalAudioSourceTest {

    @Test
    public void testCtor() {
        JoalAudioSource l;
        // This structure is because Jacoco insists on us running some JoalAudioSource
        // code even if there's no valid JOAL implementation present.
        try {
            l = new JoalAudioSource("test");
            Assert.assertNotNull("exists", l);
            Assert.assertEquals("test", l.getSystemName());
        
            Assert.assertEquals(jmri.Audio.STATE_STOPPED, l.getState());
            Assert.assertEquals(0, l.numProcessedBuffers());
            Assert.assertEquals(0, l.numQueuedBuffers());
        } catch (NullPointerException e) {
            if (JoalAudioFactory.getAL() == null) {
                // this is expected - no JOAL to check
                return;
            } else Assert.fail("AL OK, but NPE in test");
        }
    }

    @Test
    public void testC2Stringtor() {
        JoalAudioSource l;
        // This structure is because Jacoco insists on us running some JoalAudioSource
        // code even if there's no valid JOAL implementation present.
        try {
            l = new JoalAudioSource("testsysname","testusername");
            Assert.assertNotNull("exists", l);
            Assert.assertEquals("testsysname", l.getSystemName());
            Assert.assertEquals("testusername", l.getUserName());
        } catch (NullPointerException e) {
            if (JoalAudioFactory.getAL() == null) {
                // this is expected - no JOAL to check
                return;
            } else Assert.fail("AL OK, but NPE in test");
        }
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.AudioManager am = new DefaultAudioManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
        jmri.InstanceManager.setDefault(jmri.AudioManager.class,am);
        am.init();
    }

    @After
    public void tearDown() {
        // this created an audio manager, clean that up
        InstanceManager.getDefault(jmri.AudioManager.class).cleanup();

        jmri.util.JUnitAppender.suppressErrorMessage("Unhandled audio format type 0");
        JUnitUtil.tearDown(); 
    }
}
