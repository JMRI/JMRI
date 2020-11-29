package jmri.jmrit.audio;

import jmri.InstanceManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * Test simple functioning of NullAudioSource
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class NullAudioSourceTest {

    @Test
    public void testCtor() {
        NullAudioSource l = new NullAudioSource("IAS1");
        Assert.assertNotNull("exists", l);
    }

    @Test
    public void testC2Stringtor() {
        NullAudioSource l = new NullAudioSource("IAS1","testusername");
        Assert.assertNotNull("exists", l);
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
