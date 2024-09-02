package jmri.jmrit.vsdecoder;

import jmri.AudioManager;
import jmri.InstanceManager;
import jmri.ShutDownManager;
import jmri.jmrit.audio.DefaultAudioManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.jdom2.Element;
import org.junit.Assert;
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
        Assert.assertEquals("sound name", "unitUnderTest", uut.getName());
        Assert.assertFalse("is playing", uut.getSource().getState() == jmri.Audio.STATE_PLAYING);
    }

    private final static String FILENAME = "java/test/jmri/jmrit/vsdecoder/test.wav";

    @Test
    public void testCreateFull() {
        SoundBite uut = new SoundBite(null, FILENAME, "sysname", "uname"); // BOUND_MODE
        Assert.assertEquals("sound name", "uname", uut.getName());
        Assert.assertEquals("file name", FILENAME, uut.getFileName());
        Assert.assertEquals("system name", "sysname", uut.getSystemName());
        Assert.assertEquals("user name", "uname", uut.getUserName());
        Assert.assertTrue("initialized", uut.isInitialized());
        Assert.assertFalse("is playing", uut.getSource().getState() == jmri.Audio.STATE_PLAYING);
    }

    @Test
    public void TestSetGet() {
        SoundBite uut = new SoundBite("unitUnderTest"); // QUEUE_MODE
        uut.setName("new name");
        Assert.assertEquals("set name", "new name", uut.getName());
        uut.setLooped(true);
        Assert.assertTrue("set looped", uut.isLooped());
    }

    private Element buildTestXML() {
        Element e = new Element("Sound");
        e.setAttribute("name", "test_sound");
        e.setAttribute("type", "empty");
        return (e);
    }

    @Test
    public void testSetXML() {
        SoundBite uut = new SoundBite("unitUnderTest"); // QUEUE_MODE
        Element e = buildTestXML();
        uut.setXml(e);
        // SoundBite.setXml() does nothing.
        Assert.assertEquals("xml name", "unitUnderTest", uut.getName());
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
        InstanceManager.getDefault(ShutDownManager.class).deregister(damsdt);
        JUnitUtil.tearDown();
    }
}
