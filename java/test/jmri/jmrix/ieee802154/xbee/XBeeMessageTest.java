package jmri.jmrix.ieee802154.xbee;

import com.digi.xbee.api.packet.GenericXBeePacket;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * XBeeMessageTest.java
 * <p>
 * Test for the jmri.jmrix.ieee802154.xbee.XBeeMessage class
 *
 * @author Paul Bender
 */
public class XBeeMessageTest extends jmri.jmrix.AbstractMessageTestBase {

    @Override
    @Test
    public void testCtor() {
        Assert.assertEquals("length", 3, m.getNumDataElements());
    }

    @Test
    public void testByteArrayRetrieval(){
        byte a[]= { (byte)0xFF,(byte)0x00,(byte)0x12,(byte)0x9C,(byte)0xF1,(byte)0x54,(byte)0x00,(byte)0x00,
                   (byte)0x00,(byte)0x01,(byte)0x11,(byte)0x2A,(byte)0x00,(byte)0x00,(byte)0x0A,(byte)0x00,
                   (byte)0x00,(byte)0x00};
        m = new XBeeMessage(GenericXBeePacket.createPacket(a));
        for(int i=0;i<a.length;i++)
           Assert.assertEquals("payload element " + i,a[i],(byte)m.getElement(i));
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        m = new XBeeMessage(3);
    }

    @Override
    @AfterEach
    public void tearDown() {
        m = null;
        JUnitUtil.tearDown();
    }

}
