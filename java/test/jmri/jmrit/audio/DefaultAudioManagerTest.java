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
 */
public class DefaultAudioManagerTest extends jmri.managers.AbstractManagerTestBase<jmri.AudioManager,jmri.Audio> {

    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", l);
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
