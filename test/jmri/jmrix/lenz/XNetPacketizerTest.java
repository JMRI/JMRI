package jmri.jmrix.lenz;

import junit.framework.*;
import apps.tests.*;
import javax.swing.*;

/**
 * <p>Title: XNetPacketizerTest </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * @author Bob Jacobsen
 * @version $Revision: 1.5 $
 */
public class XNetPacketizerTest extends TestCase {

    public XNetPacketizerTest(String s) {
        super(s);
    }
    
    public void testOutbound() throws Exception {
        LenzCommandStation lcs = new LenzCommandStation();
        XNetPacketizer c = new XNetPacketizer(lcs);
        // connect to iostream via port controller scaffold
        XNetPortControllerScaffold p = new XNetPortControllerScaffold();
        c.connectPort(p);
        c.startThreads();
        XNetMessage m = lcs.getTurnoutCommandMsg(22, true, false, true);
        c.sendXNetMessage(m, null);
	Thread.sleep(100); // intermittent problem with seeing 4 characters?
        Assert.assertEquals("total length ", 4, p.tostream.available());
        Assert.assertEquals("Char 0", 0x52, p.tostream.readByte()&0xff);
        Assert.assertEquals("Char 1", 0x05, p.tostream.readByte()&0xff);
        Assert.assertEquals("Char 2", 0x12, p.tostream.readByte()&0xff);
        Assert.assertEquals("parity", 0x45, p.tostream.readByte()&0xff);
        Assert.assertEquals("remaining ", 0, p.tostream.available());
    }
    
    public void testInbound() throws Exception {
        LenzCommandStation lcs = new LenzCommandStation();
        XNetPacketizer c = new XNetPacketizer(lcs);
        
        // make sure Swing is up
        JFrame j = new JFrame();
        j.pack();
        
        // connect to iostream via port controller
        XNetPortControllerScaffold p = new XNetPortControllerScaffold();
        c.connectPort(p);
        c.startThreads();
        
        // object to receive reply
        XNetListenerScaffold l = new XNetListenerScaffold();
        c.addXNetListener(0xff, l);
        
        // send a message
        XNetMessage m = lcs.getTurnoutCommandMsg(22, true, false, true);
        // that's already tested, so don't do here.
        
        // now send reply
        p.tistream.write(0x52);
        p.tistream.write(0x12);
        p.tistream.write(0x12);
        p.tistream.write(0x52);
        
        // check that the message was picked up by the read thread.
        Assert.assertTrue("reply received ", waitForReply(l));
        Assert.assertEquals("first char of reply ", 0x52, l.rcvdMsg.getElement(0));
    }
    
    
    private boolean waitForReply(XNetListenerScaffold l) {
        // wait for reply (normally, done by callback; will check that later)
        int i = 0;
        while ( l.rcvdMsg == null && i++ < 100  )  {
            try {
                Thread.sleep(10);
            } catch (Exception e) {
            }
        }
        if (log.isDebugEnabled()) log.debug("past loop, i="+i
                                            +" reply="+l.rcvdMsg);
        if (i==0) log.warn("waitForReply saw an immediate return; is threading right?");
        return i<100;
    }
    
    // The minimal setup for log4J
    apps.tests.Log4JFixture log4jfixtureInst = new apps.tests.Log4JFixture(this);
    protected void setUp() { log4jfixtureInst.setUp(); }
    protected void tearDown() { log4jfixtureInst.tearDown(); }
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XNetPacketizerTest.class.getName());
    
}
