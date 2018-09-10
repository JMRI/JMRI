package jmri.jmrit.vsdecoder;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jdom2.Element;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

/**
 * Tests for the VSDSound class
 *
 * @author Mark Underwood Copyright (C) 2011
 */
public class VSDSoundTest extends TestCase {

    public void testStateConstants() {
        // Maybe check the enums here?
    }

    // Note: VSDSound is abstract.  Using SoundBite as test vehicle.
    public void testCreateSimple() {
        VSDSound uut = new SoundBite("unitUnderTest"); // BOUND_MODE
        Assert.assertEquals("sound name", "unitUnderTest", uut.getName());
        Assert.assertFalse("is playing", uut.isPlaying());
    }

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

    public void testSetXML() {
        VSDSound uut = new SoundBite("unitUnderTest"); // BOUND_MODE
        Element e = buildTestXML();
        uut.setXml(e);
        // VSDSound.setXml() does nothing.
        Assert.assertEquals("xml name", "unitUnderTest", uut.getName());
    }

    // from here down is testing infrastructure
    public VSDSoundTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {VSDSoundTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(VSDSoundTest.class);
        return suite;
    }

    @Before
    @Override
    public void setUp() {
        jmri.util.JUnitUtil.setUp();

    }

    @After
    @Override
    public void tearDown() {
        jmri.util.JUnitAppender.suppressWarnMessage("Initialised Null audio system - no sounds will be available.");
        jmri.util.JUnitUtil.tearDown();

    }
}
