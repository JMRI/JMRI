// LoaderEngineTest.java
package jmri.jmrix.loconet.soundloader;

import jmri.jmrix.loconet.LocoNetMessage;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.loconet.soundloader.LoaderEngine class.
 *
 * @author	Bob Jacobsen Copyright 2001, 2002, 2006
 * @version $Revision$
 */
public class LoaderEngineTest extends TestCase {

    public void testGetEraseMessage() {
        LoaderEngine l = new LoaderEngine(null);
        LocoNetMessage m = l.getEraseMessage();
        Assert.assertEquals("contents", "D3 02 01 7F 00 50", m.toString());
        Assert.assertEquals("checksum", true, m.checkParity());
    }

    public void testGetIntMessage() {
        LoaderEngine l = new LoaderEngine(null);
        LocoNetMessage m = l.getInitMessage();
        Assert.assertEquals("contents", "D3 01 00 00 00 2D", m.toString());
        Assert.assertEquals("checksum", true, m.checkParity());
    }

    public void testGetExitMessage() {
        LoaderEngine l = new LoaderEngine(null);
        LocoNetMessage m = l.getExitMessage();
        Assert.assertEquals("contents", "D3 00 00 00 00 2C", m.toString());
        Assert.assertEquals("checksum", true, m.checkParity());
    }

    public void testGetStartWavDataMessage1() {
        LoaderEngine l = new LoaderEngine(null);
        LocoNetMessage m = l.getStartDataMessage(LoaderEngine.TYPE_WAV, 0x17, 128);
        Assert.assertEquals("contents", "D3 04 17 01 00 3E", m.toString());
        Assert.assertEquals("checksum", true, m.checkParity());
    }

    public void testGetStartWavDataMessage2() {
        LoaderEngine l = new LoaderEngine(null);
        LocoNetMessage m = l.getStartDataMessage(LoaderEngine.TYPE_WAV, 0x17, 512);
        Assert.assertEquals("contents", "D3 04 17 02 00 3D", m.toString());
        Assert.assertEquals("checksum", true, m.checkParity());
    }

    public void testGetSendWavDataMessage() {
        LoaderEngine l = new LoaderEngine(null);
        int[] idata = new int[]{0x17, 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x4D, 0x75, 0x74, 0x65, 0x2E, 0x77, 0x61, 0x76,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] data = new byte[idata.length];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (idata[i] & 0xFF);
        }

        LocoNetMessage m = l.getSendDataMessage(LoaderEngine.TYPE_WAV, 0x17, data);
        Assert.assertEquals("contents", "D3 08 17 28 00 1B 17 80 00 00 00 00 00 00 "
                + "4D 75 74 65 2E 77 61 76 00 00 00 00 00 00 "
                + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
                + "00 00 00 00 0F", m.toString());
        Assert.assertEquals("checksum", true, m.checkParity());
    }

    public void testFullTransfer1() {
        LoaderEngine l = new LoaderEngine(null);
        int handle = 0x17;
        String name = "Mute.wav";
        byte[] contents = new byte[128];
        for (int i = 0; i < 128; i++) {
            contents[i] = (byte) 0x80;
        }

        LocoNetMessage m;

        m = l.initTransfer(LoaderEngine.TYPE_WAV, handle, name, contents);
        Assert.assertEquals("contents", "D3 04 17 01 00 3E", m.toString());
        Assert.assertEquals("checksum", true, m.checkParity());

        m = l.nextTransfer();
        Assert.assertEquals("contents", "D3 08 17 28 00 1B 17 80 00 00 00 00 00 00 "
                + "4D 75 74 65 2E 77 61 76 00 00 00 00 00 00 "
                + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
                + "00 00 00 00 0F", m.toString());
        Assert.assertEquals("checksum", true, m.checkParity());

        m = l.nextTransfer();
        Assert.assertEquals("contents", "D3 08 17 00 01 32 80 80 80 80 80 80 80 80 80 80 "
                + "80 80 80 80 80 80 80 80 80 80 80 80 80 80 80 80 "
                + "80 80 80 80 80 80 80 80 80 80 80 80 80 80 80 80 "
                + "80 80 80 80 80 80 80 80 80 80 80 80 80 80 80 80 "
                + "80 80 80 80 80 80 80 80 80 80 80 80 80 80 80 80 "
                + "80 80 80 80 80 80 80 80 80 80 80 80 80 80 80 80 "
                + "80 80 80 80 80 80 80 80 80 80 80 80 80 80 80 80 "
                + "80 80 80 80 80 80 80 80 80 80 80 80 80 80 80 80 "
                + "80 80 80 80 80 80 FF", m.toString());
        Assert.assertEquals("checksum", true, m.checkParity());

        m = l.nextTransfer();
        Assert.assertEquals("end", null, m);
    }

    // from here down is testing infrastructure
    public LoaderEngineTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {LoaderEngineTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(LoaderEngineTest.class);
        return suite;
    }

    private final static Logger log = LoggerFactory.getLogger(LoaderEngineTest.class.getName());

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
