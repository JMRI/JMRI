package jmri.jmrix.loconet.sdf;

import org.junit.Test;
import org.junit.Assert;

/**
 * Tests for the jmri.jmrix.loconet.sdf.Play class.
 *
 * @author	Bob Jacobsen Copyright 2007
 */
public class PlayTest {

    @Test
    public void testCtor() {
        new Play((byte) 0, (byte) 0);
    }

    @Test
    public void testLoopFlags() {
        Play p = new Play((byte) 0xFF, (byte) 0xFF);

        p.setBrk("loop_till_cam");
        Assert.assertEquals("loop_till_cam", p.brkVal());

        p.setBrk("loop_till_init_TRIG");
        Assert.assertEquals("loop_till_init_TRIG", p.brkVal());

        p.setBrk("loop_till_SND_ACTV11");
        Assert.assertEquals("loop_till_SND_ACTV11", p.brkVal());

        p.setBrk("loop_till_F1");
        Assert.assertEquals("loop_till_F1", p.brkVal());

        p.setBrk("loop_till_F12");
        Assert.assertEquals("loop_till_F12", p.brkVal());

        p.setBrk("loop_till_SCAT4");
        Assert.assertEquals("loop_till_SCAT4", p.brkVal());

        p.setBrk("no_loop");
        Assert.assertEquals("no_loop", p.brkVal());
    }

    @Test
    public void testWavFlagsByInt() {
        Play p = new Play((byte) 0xFF, (byte) 0xFF);

        p.setWaveBrkFlags(0);
        Assert.assertEquals("Brk 0", 0, p.getWaveBrkFlags());
        Assert.assertEquals("Brk 0", "loop_STD", p.wavebrkFlagsVal());

        p.setWaveBrkFlags(1);
        Assert.assertEquals("Brk 1", 1, p.getWaveBrkFlags());
        Assert.assertEquals("Brk 0", "loop_INVERT", p.wavebrkFlagsVal());

        p.setWaveBrkFlags(2);
        Assert.assertEquals("Brk 2", 2, p.getWaveBrkFlags());
        Assert.assertEquals("Brk 0", "loop_GLOBAL", p.wavebrkFlagsVal());

        p.setWaveBrkFlags(3);
        Assert.assertEquals("Brk 3", 3, p.getWaveBrkFlags());
    }

    @Test
    public void testWavFlagsByString() {
        Play p = new Play((byte) 0xFF, (byte) 0xFF);

        p.setWaveBrkFlags("loop_STD");
        Assert.assertEquals("Brk 0", 0, p.getWaveBrkFlags());
        Assert.assertEquals("Brk 0", "loop_STD", p.wavebrkFlagsVal());

        p.setWaveBrkFlags("loop_INVERT");
        Assert.assertEquals("Brk 1", 1, p.getWaveBrkFlags());
        Assert.assertEquals("Brk 0", "loop_INVERT", p.wavebrkFlagsVal());

        p.setWaveBrkFlags("loop_GLOBAL");
        Assert.assertEquals("Brk 2", 2, p.getWaveBrkFlags());
        Assert.assertEquals("Brk 0", "loop_GLOBAL", p.wavebrkFlagsVal());

    }

}
