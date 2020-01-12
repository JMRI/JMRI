package jmri.jmrix.lenz.liusbserver;

import jmri.jmrix.lenz.XNetPortControllerScaffold;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * <p>
 * Title: LIUSBServerXNetPacketizerTest </p>
 * <p>
 *
 * @author Paul Bender Copyright (C) 2009
 */
public class LIUSBServerXNetPacketizerTest extends jmri.jmrix.lenz.XNetPacketizerTest {

    @Test
    @Override
    public void testOutbound() throws Exception {
        LIUSBServerXNetPacketizer c = (LIUSBServerXNetPacketizer)tc;
        // connect to iostream via port controller scaffold
        jmri.jmrix.lenz.XNetPortControllerScaffold p = new jmri.jmrix.lenz.XNetPortControllerScaffold();
        c.connectPort(p);
        jmri.jmrix.lenz.XNetMessage m = jmri.jmrix.lenz.XNetMessage.getTurnoutCommandMsg(22, true, false, true);
        m.setTimeout(1);  // don't want to wait a long time
        c.sendXNetMessage(m, null);

        p.flush();
        jmri.util.JUnitUtil.waitFor(()->{return p.tostream.available()==13;},"total length 13");

        Assert.assertEquals("total length ", 13, p.tostream.available());
        Assert.assertEquals("Char 0", '5', p.tostream.readByte() & 0xff);
        Assert.assertEquals("Char 1", '2', p.tostream.readByte() & 0xff);
        Assert.assertEquals("Char 2", ' ', p.tostream.readByte() & 0xff);
        Assert.assertEquals("Char 3", '0', p.tostream.readByte() & 0xff);
        Assert.assertEquals("Char 4", '5', p.tostream.readByte() & 0xff);
        Assert.assertEquals("Char 5", ' ', p.tostream.readByte() & 0xff);
        Assert.assertEquals("Char 6", '8', p.tostream.readByte() & 0xff);
        Assert.assertEquals("Char 7", 'A', p.tostream.readByte() & 0xff);
        Assert.assertEquals("Char 8", ' ', p.tostream.readByte() & 0xff);
        Assert.assertEquals("Char 9", 'D', p.tostream.readByte() & 0xff);
        Assert.assertEquals("Char 10", 'D', p.tostream.readByte() & 0xff);
        Assert.assertEquals("Char 11", '\n', p.tostream.readByte() & 0xff);
        Assert.assertEquals("Char 12", '\r', p.tostream.readByte() & 0xff);
        Assert.assertEquals("remaining ", 0, p.tostream.available());
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        tc = new LIUSBServerXNetPacketizer(new jmri.jmrix.lenz.LenzCommandStation()) {
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
