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
        Assert.assertNotNull(JoalAudioFactory.getAL());   // fatal problem in setup
        
        JoalAudioBuffer l = new JoalAudioBuffer("test");
        Assert.assertNotNull("exists", l);
        Assert.assertEquals("test", l.getSystemName());
    }

    @Test
    public void testC2Stringtor() {
        Assert.assertNotNull(JoalAudioFactory.getAL());  // fatal problem in setup
        
        JoalAudioBuffer l = new JoalAudioBuffer("testsysname","testusername");
        Assert.assertNotNull("exists", l);
        Assert.assertEquals("testsysname", l.getSystemName());
        Assert.assertEquals("testusername", l.getUserName());
        
        Assert.assertEquals("Empty buffer", l.toString());
        Assert.assertEquals(0, l.getLength());
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.AudioManager am = new DefaultAudioManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
        jmri.InstanceManager.setDefault(jmri.AudioManager.class,am);
        am.init();
        
        // If there isn't a JoalAudioFactory at this point, have to create one for testing
        if (JoalAudioFactory.getAL() == null) new JoalAudioFactory().init();
    }

    @After
    public void tearDown() {
        // this created an audio manager, clean that up
        InstanceManager.getDefault(jmri.AudioManager.class).cleanup();

        jmri.util.JUnitAppender.suppressErrorMessage("Unhandled audio format type 0");
        JUnitUtil.tearDown();
    }
}
