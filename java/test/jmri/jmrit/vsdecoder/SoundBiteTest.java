package jmri.jmrit.vsdecoder;

import jmri.AudioManager;
import jmri.InstanceManager;
import jmri.jmrit.audio.JoalAudioFactory;
import org.jdom2.Element;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the SoundBite class
 *
 * @author Mark Underwood Copyright (C) 2011
 */
public class SoundBiteTest {

    @Test
    public void testStateConstants() {
        // Maybe check the enums here?
    }

    @Test
    public void testCreateSimple() {
        SoundBite uut = new SoundBite("unitUnderTest"); // QUEUE_MODE
        Assert.assertEquals("sound name", "unitUnderTest", uut.getName());
        Assert.assertFalse("is playing", uut.isPlaying());
    }

    String filename = "java/test/jmri/jmrit/vsdecoder/test.wav";

    @Test
    public void testCreateFull() {
        Assume.assumeTrue("Requires Joal Audio", InstanceManager.getDefault(AudioManager.class).getActiveAudioFactory() instanceof JoalAudioFactory);
        SoundBite uut = new SoundBite(null, filename, "sysname", "uname"); // BOUND_MODE
        Assert.assertEquals("sound name", "uname", uut.getName());
        Assert.assertEquals("file name", filename, uut.getFileName());
        Assert.assertEquals("system name", "sysname", uut.getSystemName());
        Assert.assertEquals("user name", "uname", uut.getUserName());
        Assert.assertTrue("initialized", uut.isInitialized());
        Assert.assertFalse("is playing", uut.isPlaying());
    }

    @Test
    public void TestSetGet() {
        Assume.assumeTrue("Requires Joal Audio", InstanceManager.getDefault(AudioManager.class).getActiveAudioFactory() instanceof JoalAudioFactory);
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
        Assume.assumeTrue("Requires Joal Audio", InstanceManager.getDefault(AudioManager.class).getActiveAudioFactory() instanceof JoalAudioFactory);
        SoundBite uut = new SoundBite("unitUnderTest"); // QUEUE_MODE
        Element e = buildTestXML();
        uut.setXml(e);
        // SoundBite.setXml() does nothing.
        Assert.assertEquals("xml name", "unitUnderTest", uut.getName());
    }

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
}
