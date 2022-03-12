package jmri.jmrit.vsdecoder.listener;

import jmri.*;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of VSDListener
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class VSDListenerTest {

    @Test
    public void testCtor() {
        VSDListener s = new VSDListener();
        Assert.assertNotNull("exists", s);
    
        // this created an audio manager, clean that up
        InstanceManager.getDefault(jmri.AudioManager.class).cleanup();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        // this created an audio manager, clean that up
        InstanceManager.getDefault(jmri.AudioManager.class).cleanup();

        jmri.util.JUnitAppender.suppressErrorMessage("Unhandled audio format type 0");
        JUnitUtil.tearDown();
    }
}
