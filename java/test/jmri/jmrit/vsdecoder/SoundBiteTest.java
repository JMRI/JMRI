package jmri.jmrit.vsdecoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.AudioManager;
import jmri.InstanceManager;
import jmri.ShutDownManager;
import jmri.jmrit.audio.DefaultAudioManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.jdom2.Element;

import org.junit.jupiter.api.*;

/**
 * Tests for the SoundBite class
 *
 * @author Mark Underwood Copyright (C) 2011
 */
public class SoundBiteTest {

    private Runnable damsdt;

    @Test
    @Disabled("Test requires further development")
    public void testStateConstants() {
        // Maybe check the enums here?
    }

    @Test
    public void testCreateSimple() {
        SoundBite uut = new SoundBite("unitUnderTest"); // QUEUE_MODE
        assertEquals("unitUnderTest", uut.getName(), "sound name");
        assertTrue(uut.isInitialized(), "initialized");
        assertNotEquals(jmri.Audio.STATE_PLAYING, uut.getSource().getState(), "is playing");
        uut.shutdown();
    }

    private final static String FILENAME = "java/test/jmri/jmrit/vsdecoder/test.wav";

    @Test
    public void testCreateFull() {
        SoundBite uut = new SoundBite(null, FILENAME, "sysname", "uname"); // BOUND_MODE
        assertEquals("uname", uut.getName(), "sound name");
        assertEquals(FILENAME, uut.getFileName(), "file name");
        assertEquals("sysname", uut.getSystemName(), "system name");
        assertEquals("uname", uut.getUserName(), "user name");
        assertTrue(uut.isInitialized(), "initialized");
        uut.setLooped(true);
        assertNotEquals(jmri.Audio.STATE_PLAYING, uut.getSource().getState(), "is playing");
        JUnitAppender.suppressWarnMessage("Requested operation is not valid");
        JUnitAppender.suppressWarnMessage("Error creating JoalAudioBuffer (IAB$VSD:sysname)");
        JUnitAppender.suppressErrorMessage("Unhandled audio format type 0");

    }

    @Test
    public void testSetGet() {
        SoundBite uut = new SoundBite("unitUnderTest"); // QUEUE_MODE
        uut.setName("new name");
        assertEquals("new name", uut.getName(), "set name");
        assertTrue(uut.isInitialized(), "initialized");
    }

    private Element buildTestXML() {
        Element e = new Element("Sound");
        e.setAttribute("name", "test_sound");
        e.setAttribute("type", "empty");
        return e;
    }

    @Test
    public void testSetXML() {
        SoundBite uut = new SoundBite("unitUnderTest"); // QUEUE_MODE
        Element e = buildTestXML();
        uut.setXml(e);
        // SoundBite.setXml() does nothing.
        assertEquals("unitUnderTest", uut.getName(), "xml name");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        DefaultAudioManager dam = new DefaultAudioManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
        damsdt = dam.audioShutDownTask;
        InstanceManager.setDefault(AudioManager.class, dam);
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitAppender.suppressErrorMessage("Unhandled audio format type 0");

        InstanceManager.getDefault(AudioManager.class).dispose();

        InstanceManager.getDefault(ShutDownManager.class).deregister(damsdt);
        JUnitUtil.tearDown();
    }
}
