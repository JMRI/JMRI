package jmri.jmrit.vsdecoder;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the NotchSound class
 *
 * @author Mark Underwood Copyright (C) 2011
 */
public class NotchSoundTest {

    private NotchSound uut = null;
    private String filename = "java/test/jmri/jmrit/vsdecoder/test.wav";

    @Test
    public void testCreateFull() {
        Assert.assertEquals("sound name", "uname", uut.getName());
        Assert.assertEquals("file name", filename, uut.getFileName());
        Assert.assertEquals("system name", "sysname", uut.getSystemName());
        Assert.assertEquals("user name", "uname", uut.getUserName());
        Assert.assertTrue("initialized", uut.isInitialized());
        Assert.assertFalse("is playing", uut.isPlaying());
    }

    @Test
    public void TestSetGet() {
        uut.setName("new name");
        Assert.assertEquals("set name", "new name", uut.getName());
        uut.setLooped(true);
        Assert.assertTrue("set looped", uut.isLooped());
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        uut = new NotchSound(null, filename, "sysname", "uname"); // BOUND_MODE
    }

    @After
    public void tearDown() {
        uut = null;
        JUnitUtil.tearDown();
    }
}
