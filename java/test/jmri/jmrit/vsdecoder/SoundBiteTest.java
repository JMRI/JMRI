package jmri.jmrit.vsdecoder;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jdom2.Element;

/**
 * Tests for the SoundBite class
 *
 * @author Mark Underwood Copyright (C) 2011
 */
public class SoundBiteTest extends TestCase {

    public void testStateConstants() {
        // Maybe check the enums here?
    }

    public void testCreateSimple() {
        SoundBite uut = new SoundBite("unitUnderTest");
        Assert.assertEquals("sound name", "unitUnderTest", uut.getName());
        Assert.assertFalse("is playing", uut.isPlaying());
    }

    public void testCreateFull() {
        SoundBite uut = new SoundBite(null, "test.wav", "sysname", "uname");
        Assert.assertEquals("sound name", "uname", uut.getName());
        Assert.assertEquals("file name", "filename", uut.getFileName());
        Assert.assertEquals("system name", "sysname", uut.getSystemName());
        Assert.assertEquals("user name", "uname", uut.getUserName());
        Assert.assertTrue("initialized", uut.isInitialized());
        Assert.assertFalse("is playing", uut.isPlaying());
    }

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

    public void testSetXML() {
        SoundBite uut = new SoundBite("unitUnderTest");
        Element e = buildTestXML();
        uut.setXml(e);
        // SoundBite.setXml() does nothing.
        Assert.assertEquals("xml name", "unitUnderTest", uut.getName());
    }

    // from here down is testing infrastructure
    public SoundBiteTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SoundBiteTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SoundBiteTest.class);
        return suite;
    }

    protected void setUp() {
        //super.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }
}
