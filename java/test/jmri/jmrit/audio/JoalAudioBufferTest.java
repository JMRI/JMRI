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
 * Test simple functioning of JoalAudioBuffer
 *
 * @author	Paul Bender Copyright (C) 2017
 */
public class JoalAudioBufferTest {

    @Test
    public void testCtor() {
        Assume.assumeNotNull(JoalAudioFactory.getAL());
        JoalAudioBuffer l = new JoalAudioBuffer("test");
        Assert.assertNotNull("exists", l);
    }

    @Test(expected = java.lang.NullPointerException.class )
    public void testCtorFail() {
        Assume.assumeTrue(null == JoalAudioFactory.getAL());
        JoalAudioBuffer l = new JoalAudioBuffer("test");
        Assert.assertNotNull("exists", l);
    }

    @Test
    public void testC2Stringtor() {
        Assume.assumeNotNull(JoalAudioFactory.getAL());
        JoalAudioBuffer l = new JoalAudioBuffer("testsysname","testusername");
        Assert.assertNotNull("exists", l);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.AudioManager am = new DefaultAudioManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
        jmri.InstanceManager.setDefault(jmri.AudioManager.class,am);
        am.init();
        jmri.util.JUnitAppender.suppressWarnMessage("Initialised Null audio system - no sounds will be available.");
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
