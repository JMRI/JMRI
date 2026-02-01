package jmri.jmrit.vsdecoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the NotchTransition class
 *
 * @author Mark Underwood Copyright (C) 2011
 */
public class NotchTransitionTest {

    private NotchTransition uut = null;
    private final static String FILENAME = "java/test/jmri/jmrit/vsdecoder/test.wav";

    @Test
    public void testCreateFull() {
        assertEquals("uname", uut.getName(), "sound name");
        assertEquals(FILENAME, uut.getFileName(), "file name");
        assertEquals("sysname", uut.getSystemName(), "system name");
        assertEquals("uname", uut.getUserName(), "user name");
        assertTrue(uut.isInitialized(), "initialized");
        assertNotEquals(jmri.Audio.STATE_PLAYING, uut.getSource().getState(), "is playing");
    }

    @Test
    public void testSetGet() {
        uut.setName("new name");
        assertEquals("new name", uut.getName(), "set name");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        uut = new NotchTransition(null, FILENAME, "sysname", "uname"); // BOUND_MODE
    }

    @AfterEach
    public void tearDown() {
        uut = null;

        // this created an audio manager, clean that up
        jmri.InstanceManager.getDefault(jmri.AudioManager.class).cleanup();

        jmri.util.JUnitAppender.suppressErrorMessage("Unhandled audio format type 0");
        JUnitUtil.tearDown();
    }
}
