package jmri.jmrit.audio;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import jmri.Audio;
import jmri.InstanceManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * Test simple functioning of JavaSoundAudioSource
 *
 * @author Paul Bender Copyright (C) 2017
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

    /**
     * Binding to a buffer that has not loaded returns at once: it runs on the
     * audio command thread, holding the audio factory. It used to wait up to
     * 20 seconds; the timeout gives a margin of 10.
     */
    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    public void testBindToUnloadedBufferDoesNotWait() {
        JavaSoundAudioSource source = new JavaSoundAudioSource("IAS1");
        AudioBuffer buffer = new JavaSoundAudioBuffer("IAB1");
        Assert.assertNotEquals("not loaded", Audio.STATE_LOADED, buffer.getState());
        Assert.assertFalse("not bound", source.bindAudioBuffer(buffer));
    }

    /**
     * A source assigned a buffer before the buffer has loaded is bound when
     * the buffer loads, as long as it is still the assigned buffer.
     *
     * @throws jmri.JmriException if a buffer's state can't be set
     */
    @Test
    public void testBoundWhenAssignedBufferLoads() throws jmri.JmriException {
        List<AudioBuffer> assigned = new ArrayList<>();
        JavaSoundAudioSource source = new JavaSoundAudioSource("IAS1") {
            @Override
            public void setAssignedBuffer(AudioBuffer audioBuffer) {
                assigned.add(audioBuffer);
            }

            @Override
            public AudioBuffer getAssignedBuffer() {
                return assigned.isEmpty() ? null : assigned.get(0);
            }
        };
        AudioBuffer buffer = new JavaSoundAudioBuffer("IAB1");
        AudioBuffer other = new JavaSoundAudioBuffer("IAB2");
        source.setAssignedBuffer(buffer);
        Assert.assertFalse("not bound while loading", source.bindAudioBuffer(buffer));

        other.setState(Audio.STATE_LOADED);
        Assert.assertEquals("another buffer loading changes nothing", 1, assigned.size());
        buffer.setState(Audio.STATE_LOADED);
        Assert.assertEquals("bind queued again once loaded", List.of(buffer, buffer), assigned);
        buffer.setState(Audio.STATE_EMPTY);
        buffer.setState(Audio.STATE_LOADED);
        Assert.assertEquals("no longer waiting for it", 2, assigned.size());
    }

    /**
     * A source bound to a buffer it was not assigned does not take that
     * buffer when it loads; nor does a source deleted while it waits; and a
     * buffer that changes state without loading changes nothing.
     *
     * @throws jmri.JmriException if a buffer's state can't be set
     */
    @Test
    public void testNotBoundWhenOtherwiseLoaded() throws jmri.JmriException {
        List<AudioBuffer> assigned = new ArrayList<>();
        JavaSoundAudioSource source = new JavaSoundAudioSource("IAS1") {
            @Override
            public void setAssignedBuffer(AudioBuffer audioBuffer) {
                assigned.add(audioBuffer);
            }
        };
        AudioBuffer notAssigned = new JavaSoundAudioBuffer("IAB1");
        Assert.assertFalse(source.bindAudioBuffer(notAssigned));
        notAssigned.setState(Audio.STATE_INITIAL); // a change, but not loaded
        notAssigned.setState(Audio.STATE_LOADED);
        Assert.assertTrue("buffer not assigned, not bound", assigned.isEmpty());

        AudioBuffer awaited = new JavaSoundAudioBuffer("IAB2");
        Assert.assertFalse(source.bindAudioBuffer(awaited));
        source.cleanup(); // as when the source is deleted
        awaited.setState(Audio.STATE_LOADED);
        Assert.assertTrue("deleted source not bound", assigned.isEmpty());
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
