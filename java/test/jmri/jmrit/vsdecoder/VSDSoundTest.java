package jmri.jmrit.vsdecoder;

import jmri.*;
import org.jdom2.Element;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the VSDSound class
 *
 * @author Mark Underwood Copyright (C) 2011
 */
public class VSDSoundTest {

    @Test
    public void testStateConstants() {
        // Maybe check the enums here?
    }

    // Note: VSDSound is abstract.  Using SoundBite as test vehicle.
    @Test
    public void testCreateSimple() {
        VSDSound uut = new SoundBite("unitUnderTest"); // BOUND_MODE
        Assert.assertEquals("sound name", "unitUnderTest", uut.getName());
        Assert.assertFalse("is playing", uut.isPlaying());
    
        // this created an audio manager, clean that up
        InstanceManager.getDefault(jmri.AudioManager.class).cleanup();
    }

    @Test
    public void testSetGet() {
        VSDSound uut = new SoundBite("unitUnderTest"); // BOUND_MODE
        uut.setName("new name");
        Assert.assertEquals("set name", "new name", uut.getName());
    }

    private Element buildTestXML() {
        Element e = new Element("Sound");
        e.setAttribute("name", "test_sound");
        e.setAttribute("type", "empty");
        return (e);
    }

    @Test
    public void testSetXML() {
        VSDSound uut = new SoundBite("unitUnderTest"); // BOUND_MODE
        Element e = buildTestXML();
        uut.setXml(e);
        // VSDSound.setXml() does nothing.
        Assert.assertEquals("xml name", "unitUnderTest", uut.getName());
    }

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();

    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();

    }
}
