package jmri.jmrix.loconet;

import java.io.*;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.jmrix.loconet.*;

/**
 * LocoNetMessageTest.java
 *
 * Description:	    tests for the jmri.jmrix.loconet.LocoNetMessage class
 * @author			Bob Jacobsen
 * @version         $Id: LocoNetMessageTest.java,v 1.4 2003-01-05 08:53:49 jacobsen Exp $
 */
public class LocoNetMessageTest extends TestCase {

    public void testCtor() {
        LocoNetMessage m = new LocoNetMessage(3);
        Assert.assertEquals("length", 3, m.getNumDataElements());
    }

    public void testArrayCtor() {
        LocoNetMessage m = new LocoNetMessage(new int[]{11,12,13,14});
        Assert.assertEquals("length", 4, m.getNumDataElements());
        Assert.assertEquals("first value", 11, m.getElement(0));
        Assert.assertEquals("second value", 12, m.getElement(1));
        Assert.assertEquals("third value", 13, m.getElement(2));
        Assert.assertEquals("fourth value", 14, m.getElement(3));
    }

    public void testGetPeerXfr() {
        // basic message
        LocoNetMessage m1 = LocoNetMessage.makePeerXfr(0x1050, 0x1051,
                                        new int[] {1,2,3,4,5,6,7,8}, 0);
        checkPeerXfr(m1, 0x1050, 0x1051,
                        new int[] {1,2,3,4,5,6,7,8}, 0);

        // some high data bits set
        LocoNetMessage m2 = LocoNetMessage.makePeerXfr(0x1050, 0x1051,
                                        new int[] {0x80, 0x81, 3, 4, 0xf5, 6, 7, 0xf8}, 0);
        checkPeerXfr(m2, 0x1050, 0x1051,
                        new int[] {0x80, 0x81, 3, 4, 0xf5, 6, 7, 0xf8}, 0);

        // all high data bits set
        LocoNetMessage m3 = LocoNetMessage.makePeerXfr(0x1050, 0x1051,
                                        new int[] {0x80, 0x81, 0x83, 0x84, 0xf5, 0x86, 0x87, 0xf8}, 0);
        checkPeerXfr(m3, 0x1050, 0x1051,
                        new int[] {0x80, 0x81, 0x83, 0x84, 0xf5, 0x86, 0x87, 0xf8}, 0);

        // check code three times
        LocoNetMessage m4 = LocoNetMessage.makePeerXfr(0x1050, 0x1051,
                                        new int[] {1,2,3,4,5,6,7,8}, 0x11);
        checkPeerXfr(m4, 0x1050, 0x1051,
                        new int[] {1,2,3,4,5,6,7,8}, 0x11);

        m4 = LocoNetMessage.makePeerXfr(0x1050, 0x1051,
                                        new int[] {1,2,3,4,5,6,7,8}, 0x38);
        checkPeerXfr(m4, 0x1050, 0x1051,
                        new int[] {1,2,3,4,5,6,7,8}, 0x38);

        m4 = LocoNetMessage.makePeerXfr(0x1050, 0x1051,
                                        new int[] {1,2,3,4,5,6,7,8}, 63);
        checkPeerXfr(m4, 0x1050, 0x1051,
                        new int[] {1,2,3,4,5,6,7,8}, 63);

    }

    // use the makePeerXfr calls, already tested to check the decoding
    public void testGetPeerXfrData() {
        int[] test;
        int[] data;
        LocoNetMessage m;

        test = new int[] {1,2,3,4,5,6,7,8};
        m = LocoNetMessage.makePeerXfr(0x1050, 0x1051, test, 63);
        data = m.getPeerXfrData();
        for (int i=0; i<8; i++)
            Assert.assertEquals("simple value "+i, ""+test[i], ""+data[i]);

        test = new int[] {0x81,0x21,0x83,0x84,0x54,0x86,0x66,0x88};
        m = LocoNetMessage.makePeerXfr(0x1050, 0x1051, test, 63);
        data = m.getPeerXfrData();
        for (int i=0; i<8; i++)
            Assert.assertEquals("high-bit value "+i, ""+test[i], ""+data[i]);

        test = new int[] {0xB5,0xD3,0x63,0xF4,0x5E,0x77,0xFF,0x22};
        m = LocoNetMessage.makePeerXfr(0x1050, 0x1051, test, 63);
        data = m.getPeerXfrData();
        for (int i=0; i<8; i++)
            Assert.assertEquals("complicated value "+i, ""+test[i], ""+data[i]);
    }

    // service routine to check the contents of a single message
    protected void checkPeerXfr(LocoNetMessage m, int src, int dst, int[] d, int code) {
        Assert.assertEquals("opcode ", 0xE5, m.getElement(0));
        Assert.assertEquals("secondary op code ", 0x10, m.getElement(1));

        // check the 8 data bytes
        int pxct1 = m.getElement(5);
        int pxct2 = m.getElement(10);

        Assert.assertEquals("data 0", d[0], (m.getElement(6)&0x7F)+((pxct1&0x01)!=0?0x80:0));
        Assert.assertEquals("data 1", d[1], (m.getElement(7)&0x7F)+((pxct1&0x02)!=0?0x80:0));
        Assert.assertEquals("data 2", d[2], (m.getElement(8)&0x7F)+((pxct1&0x04)!=0?0x80:0));
        Assert.assertEquals("data 3", d[3], (m.getElement(9)&0x7F)+((pxct1&0x08)!=0?0x80:0));

        Assert.assertEquals("data 4", d[4], (m.getElement(11)&0x7F)+((pxct2&0x01)!=0?0x80:0));
        Assert.assertEquals("data 5", d[5], (m.getElement(12)&0x7F)+((pxct2&0x02)!=0?0x80:0));
        Assert.assertEquals("data 6", d[6], (m.getElement(13)&0x7F)+((pxct2&0x04)!=0?0x80:0));
        Assert.assertEquals("data 7", d[7], (m.getElement(14)&0x7F)+((pxct2&0x08)!=0?0x80:0));

        // check code
        Assert.assertEquals("code low nibble", code&0x7, (m.getElement(5)&0x70)/16);
        Assert.assertEquals("code high nibble", (code&0x38)/8, (m.getElement(10)&0x70)/16);

        // check the source address
        Assert.assertEquals("low 7 src address", src&0x7F, m.getElement(2));

        // check the dest address
        Assert.assertEquals("low 7 dst address", dst&0x7F, m.getElement(3));
        Assert.assertEquals("high 7 dst address", (dst&0x7F00)/256, m.getElement(4));
    }

	// from here down is testing infrastructure

	public LocoNetMessageTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {LocoNetMessageTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(LocoNetMessageTest.class);
		return suite;
	}

	 static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoNetMessageTest.class.getName());

}
