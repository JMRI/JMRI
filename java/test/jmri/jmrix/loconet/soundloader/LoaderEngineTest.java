package jmri.jmrix.loconet.soundloader;

import jmri.jmrix.loconet.LocoNetMessage;
import jmri.util.JUnitUtil;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.junit.Assert;

/**
 * Tests for the jmri.jmrix.loconet.soundloader.LoaderEngine class.
 *
 * @author	Bob Jacobsen Copyright 2001, 2002, 2006
 */
public class LoaderEngineTest {

    @Test
    public void testGetEraseMessage() {
        LoaderEngine l = new LoaderEngine(null);
        LocoNetMessage m = l.getEraseMessage();
        Assert.assertEquals("contents", "D3 02 01 7F 00 50", m.toString());
        Assert.assertEquals("checksum", true, m.checkParity());
    }

    @Test
    public void testGetIntMessage() {
        LoaderEngine l = new LoaderEngine(null);
        LocoNetMessage m = l.getInitMessage();
        Assert.assertEquals("contents", "D3 01 00 00 00 2D", m.toString());
        Assert.assertEquals("checksum", true, m.checkParity());
    }

    @Test
    public void testGetExitMessage() {
        LoaderEngine l = new LoaderEngine(null);
        LocoNetMessage m = l.getExitMessage();
        Assert.assertEquals("contents", "D3 00 00 00 00 2C", m.toString());
        Assert.assertEquals("checksum", true, m.checkParity());
    }

    @Test
    public void testGetStartWavDataMessage1() {
        LoaderEngine l = new LoaderEngine(null);
        LocoNetMessage m = l.getStartDataMessage(LoaderEngine.TYPE_WAV, 0x17, 128);
        Assert.assertEquals("contents", "D3 04 17 01 00 3E", m.toString());
        Assert.assertEquals("checksum", true, m.checkParity());
    }

    @Test
    public void testGetStartWavDataMessage2() {
        LoaderEngine l = new LoaderEngine(null);
        LocoNetMessage m = l.getStartDataMessage(LoaderEngine.TYPE_WAV, 0x17, 512);
        Assert.assertEquals("contents", "D3 04 17 02 00 3D", m.toString());
        Assert.assertEquals("checksum", true, m.checkParity());
    }

    @Test
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

    @Test
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

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
