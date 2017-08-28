package jmri.jmrit.vsdecoder.swing;

import jmri.AudioManager;
import jmri.InstanceManager;
import jmri.jmrit.audio.JoalAudioFactory;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class VSDControlTest {

    @Test
    public void testCTor() {
        Assume.assumeTrue("Requires JOAL Audio", InstanceManager.getDefault(AudioManager.class).getActiveAudioFactory() instanceof JoalAudioFactory);
        VSDControl t = new VSDControl();
        Assert.assertNotNull("exists", t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        InstanceManager.getDefault(AudioManager.class).init();
    }

    @After
    public void tearDown() {
        InstanceManager.getDefault(AudioManager.class).cleanUp();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(VSDControlTest.class.getName());

}
