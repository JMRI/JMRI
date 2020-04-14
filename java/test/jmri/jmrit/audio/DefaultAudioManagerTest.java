package jmri.jmrit.audio;

import jmri.Audio;
import jmri.AudioException;
import jmri.InstanceManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of DefaultAudioManager
 *
 * @author	Paul Bender Copyright (C) 2017
 * @author	Matthew Harris Copyright (C) 2019
 */
public class DefaultAudioManagerTest extends jmri.managers.AbstractManagerTestBase<jmri.AudioManager,jmri.Audio> {

    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", l);
    }

    /**
     * Test of getActiveAudioFactory method, of class DefaultAudioManager.
     */
    @Test
    public void testGetActiveAudioFactory() {
        AudioFactory result = l.getActiveAudioFactory();
        Assert.assertNotNull("Verify that ActiveAudioFactory is not null", result);
    }

    /**
     * Test of getXMLOrder method, of class DefaultAudioManager.
     */
    @Test
    public void testGetXMLOrder() {
        int expResult = jmri.Manager.AUDIO;
        int result = l.getXMLOrder();
        Assert.assertEquals("Verify DefaultAudioManager XMLOrder", expResult, result);
    }

    /**
     * Test of getNamedBeanSet method, of class DefaultAudioManager.
     */
    @Test
    public void testGetNamedBeanSet() {
        int expResult = 1;
        int result = l.getNamedBeanSet(Audio.LISTENER).size();
        Assert.assertEquals("Verify that we get one listener", expResult, result);

        // Now let's create a couple of buffers
        boolean canCreateBuffers = true;
        try {
            l.provideAudio("IAB1");
            l.provideAudio("IAB2");
        } catch (AudioException ex) {
            canCreateBuffers = false;
        }

        Assert.assertTrue("Verify buffers created without error", canCreateBuffers);

        expResult = 2;
        result = l.getNamedBeanSet(Audio.BUFFER).size();
        Assert.assertEquals("Verify that we get two buffers", expResult, result);

        // Now let's create a couple of sources and bind those to the buffers
        boolean canCreateSources = true;
        try {
            AudioSource s = (AudioSource) l.provideAudio("IAS1");
            s.setAssignedBuffer((AudioBuffer) l.getNamedBean("IAB1"));
            s = (AudioSource) l.provideAudio("IAS2");
            s.setAssignedBuffer((AudioBuffer) l.getNamedBean("IAB2"));
        } catch (AudioException ex) {
            canCreateSources = false;
        }

        Assert.assertTrue("Verify sources created without error", canCreateSources);

        expResult = 2;
        result = l.getNamedBeanSet(Audio.SOURCE).size();
        Assert.assertEquals("Verify that we get two sources", expResult, result);

        // Now verify Sources and Buffers are bound
        // First pairing
        AudioSource s = (AudioSource) l.getAudio("IAS1");
        AudioBuffer b = (AudioBuffer) l.getAudio("IAB1");
        AudioBuffer ab = s.getAssignedBuffer();
        Assert.assertEquals("Verify AudioSource IAS1 bound to AudioBuffer IAB1", b, ab);
        // Second pairing
        s = (AudioSource) l.getAudio("IAS1");
        b = (AudioBuffer) l.getAudio("IAB1");
        ab = s.getAssignedBuffer();
        Assert.assertEquals("Verify AudioSource IAS1 bound to AudioBuffer IAB1", b, ab);

        // Now verify that the complete set of Audio objects is returned
        // 1 Listener, 2 Buffers & 2 Sources
        expResult = 5;
        result = l.getNamedBeanSet().size();
        Assert.assertEquals("Verify that we get two buffers & one listener", expResult, result);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        l = new DefaultAudioManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
        l.init();
        JUnitUtil.waitFor(()->{return l.isInitialised();});
    }

    @After
    public void tearDown() {
        l.cleanup();
        JUnitUtil.waitFor(()->{return !l.isInitialised();});
        l = null;
        JUnitUtil.tearDown();
    }
}
