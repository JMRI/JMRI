package jmri.jmrit.vsdecoder;

import org.junit.Assert;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.jdom2.Element;

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
        SoundBite uut = new SoundBite("unitUnderTest");
        Assert.assertEquals("sound name", "unitUnderTest", uut.getName());
        Assert.assertFalse("is playing", uut.isPlaying());
    }

    @Test
    @Ignore("Causes NPE")
    public void testCreateFull() {
        SoundBite uut = new SoundBite(null, "test.wav", "sysname", "uname");
        Assert.assertEquals("sound name", "uname", uut.getName());
        Assert.assertEquals("file name", "filename", uut.getFileName());
        Assert.assertEquals("system name", "sysname", uut.getSystemName());
        Assert.assertEquals("user name", "uname", uut.getUserName());
        Assert.assertTrue("initialized", uut.isInitialized());
        Assert.assertFalse("is playing", uut.isPlaying());
    }

    @Test
    public void TestSetGet() {
        SoundBite uut = new SoundBite("unitUnderTest");
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
        SoundBite uut = new SoundBite("unitUnderTest");
        Element e = buildTestXML();
        uut.setXml(e);
        // SoundBite.setXml() does nothing.
        Assert.assertEquals("xml name", "unitUnderTest", uut.getName());
    }

    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }
}
