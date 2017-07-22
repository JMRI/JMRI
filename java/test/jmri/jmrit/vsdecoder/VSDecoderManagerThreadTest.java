package jmri.jmrit.vsdecoder;

import jmri.AudioManager;
import jmri.InstanceManager;
import jmri.jmrit.audio.JoalAudioFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class VSDecoderManagerThreadTest {

    @Test
    public void testInstance() {
        Assume.assumeTrue("Requires Joal Audio", InstanceManager.getDefault(AudioManager.class).getActiveAudioFactory() instanceof JoalAudioFactory);
        VSDecoderManagerThread t = VSDecoderManagerThread.instance();
        Assert.assertNotNull("exists", t);
        // the instance method starts a thread, make sure it goes away.
        t.kill();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        InstanceManager.getDefault(AudioManager.class).init();
    }

    @After
    public void tearDown() {
        InstanceManager.getDefault(AudioManager.class).cleanUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(VSDecoderManagerThreadTest.class.getName());

}
