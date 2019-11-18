package jmri.jmrit.audio;

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
        l.init();
        JUnitUtil.waitFor(()->{return l.isInitialised();});
        Assert.assertNotNull("Verify that ActiveAudioFactory is not null", result);
        l.cleanup();
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

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        l = new DefaultAudioManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
    }

    @After
    public void tearDown() {
        l = null;
        JUnitUtil.tearDown();
    }
}
