package jmri.jmrix.ieee802154.xbee;

import jmri.jmrix.AbstractMessageTestBase;
import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the XBeeBroadcastMessage class
 *
 * @author Paul Bender Copyright (C) 2023
 */

class XBeeBroadcastMessageTest extends AbstractMessageTestBase {

    private static byte testPayload[] = {(byte) 0xFF, (byte) 0x00, (byte) 0x12, (byte) 0x9C, (byte) 0xF1, (byte) 0x54, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x11, (byte) 0x2A, (byte) 0x00, (byte) 0x00, (byte) 0x0A, (byte) 0x00, (byte) 0x00, (byte) 0x00};

    @Override
    @Test
    public void testCtor() {
        Assert.assertEquals("length", testPayload.length+5, m.getNumDataElements());
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        m = XBeeBroadcastMessage.getTX16BroadcastMessage(testPayload);
    }

    @Override
    @AfterEach
    public void tearDown() {
        m = null;
        JUnitUtil.tearDown();
    }

    @Test
    void rightTX16BroadcastPayload() {
        XBeeBroadcastMessage msg = (XBeeBroadcastMessage)  m;
        for (int i = 0; i < testPayload.length; i++)
            Assert.assertEquals("payload element " + i, testPayload[i], msg.getXBeeRequest().getAPIData()[i+4]);
    }
}
