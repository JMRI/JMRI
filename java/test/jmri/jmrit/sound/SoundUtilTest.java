package jmri.jmrit.sound;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the SoundUtil class.
 * <P>
 * Note: This makes noise!
 *
 * @author	Bob Jacobsen Copyright 2006
 */
public class SoundUtilTest extends TestCase {

    public void testLargeBuffer() throws java.io.IOException, javax.sound.sampled.UnsupportedAudioFileException {
        String name = "bottle-open.wav";
        byte[] results = SoundUtil.bufferFromFile(name,
                11025.0f, 8, 1, false, false);
        Assert.assertEquals("length", 44557, results.length);
        Assert.assertEquals("byte 0", 0x80, 0xFF & results[0]);
        Assert.assertEquals("byte 1", 0x81, 0xFF & results[1]);
    }

    // from here down is testing infrastructure
    public SoundUtilTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SoundUtilTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SoundUtilTest.class);
        return suite;
    }

    // static private Logger log = LoggerFactory.getLogger(XmlFileTest.class.getName());
}
