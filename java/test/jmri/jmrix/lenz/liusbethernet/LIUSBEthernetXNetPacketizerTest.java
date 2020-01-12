package jmri.jmrix.lenz.liusbethernet;

import jmri.jmrix.lenz.XNetPortControllerScaffold;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * <p>
 * Title: LIUSBEthernetXNetPacketizerTest </p>
 * <p>
 *
 * @author Paul Bender Copyright (C) 2009
 */
public class LIUSBEthernetXNetPacketizerTest extends jmri.jmrix.lenz.XNetPacketizerTest {

    @Test
    @Override
    public void testOutbound() throws Exception {
        LIUSBEthernetXNetPacketizer c = (LIUSBEthernetXNetPacketizer)tc;
        // connect to iostream via port controller scaffold
        jmri.jmrix.lenz.XNetPortControllerScaffold p = new jmri.jmrix.lenz.XNetPortControllerScaffold();
        c.connectPort(p);
        jmri.jmrix.lenz.XNetMessage m = jmri.jmrix.lenz.XNetMessage.getTurnoutCommandMsg(22, true, false, true);
        m.setTimeout(1);  // don't want to wait a long time
        c.sendXNetMessage(m, null);

        p.flush();
        jmri.util.JUnitUtil.waitFor(()->{return p.tostream.available()==6;},"total length 6");

        Assert.assertEquals("total length ", 6, p.tostream.available());
        Assert.assertEquals("Header 0", 0xFF, p.tostream.readByte() & 0xff);
        Assert.assertEquals("Header 1", 0xFE, p.tostream.readByte() & 0xff);
        Assert.assertEquals("Char 0", 0x52, p.tostream.readByte() & 0xff);
        Assert.assertEquals("Char 1", 0x05, p.tostream.readByte() & 0xff);
        Assert.assertEquals("Char 2", 0x8A, p.tostream.readByte() & 0xff);
        Assert.assertEquals("parity", 0xDD, p.tostream.readByte() & 0xff);
        Assert.assertEquals("remaining ", 0, p.tostream.available());
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        tc = new LIUSBEthernetXNetPacketizer(new jmri.jmrix.lenz.LenzCommandStation()) {
            @Override
            protected void handleTimeout(jmri.jmrix.AbstractMRMessage msg, jmri.jmrix.AbstractMRListener l) {
            }
        };
        try {
           port = new XNetPortControllerScaffold();
        } catch (Exception e) {
           Assert.fail("Error creating test port");
        }
    }

}
