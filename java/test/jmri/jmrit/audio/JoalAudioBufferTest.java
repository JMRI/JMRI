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
        Assume.assumeNotNull(JoalAudioFactory.getAL()); // Run test method only when JOAL is present.
        
        JoalAudioBuffer l = new JoalAudioBuffer("test");
        
        Assert.assertNotNull("exists", l);
        Assert.assertEquals("test", l.getSystemName());
    }

    /**
     * This is present to handle the case when JOAL can't 
     * be instantiated, while still testing at least one line
     * of JoalAudioBuffer to keep the JaCoCo scan happy.
     * It runs the ctor only if there isn't a JoalAudioFactory,
     * and expects an exception.
     */
    @Test(expected = java.lang.NullPointerException.class )
    public void testCtorFail() {
        Assume.assumeTrue(null == JoalAudioFactory.getAL());
        JoalAudioBuffer l = new JoalAudioBuffer("test");
        
        // no tests, because what matters is the thrown exception
        Assert.fail("Should have thrown");
    }    

    @Test
    public void testC2Stringtor() {
        Assume.assumeNotNull(JoalAudioFactory.getAL()); // Run test method only when JOAL is present.
        
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
    }

    @After
    public void tearDown() {
        // this created an audio manager, clean that up
        InstanceManager.getDefault(jmri.AudioManager.class).cleanup();

        jmri.util.JUnitAppender.suppressErrorMessage("Unhandled audio format type 0");
        JUnitUtil.tearDown();
    }
}
