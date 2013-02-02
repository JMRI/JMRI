package jmri.jmrix.lenz;

import org.apache.log4j.Logger;
import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * <p>Title: XNetPacketizerTest </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * @author Bob Jacobsen
 * @version $Revision$
 */
public class XNetPacketizerTest extends TestCase {

    public XNetPacketizerTest(String s) {
        super(s);
    }

    public void testOutbound() throws Exception {
        LenzCommandStation lcs = new LenzCommandStation();
        XNetPacketizer c = new XNetPacketizer(lcs){
            protected void handleTimeout(jmri.jmrix.AbstractMRMessage msg,jmri.jmrix.AbstractMRListener l) {} // don't care about timeout
            public void receiveLoop() {}
            protected void portWarn(Exception e) {}
        };
        // connect to iostream via port controller scaffold
        XNetPortControllerScaffold p = new XNetPortControllerScaffold();
        c.connectPort(p);
        //c.startThreads();
        XNetMessage m = XNetMessage.getTurnoutCommandMsg(22, true, false, true);
        m.setTimeout(1);  // don't want to wait a long time
        c.sendXNetMessage(m, null);
	    jmri.util.JUnitUtil.releaseThread(this, 100); // Allow time for other threads to send 4 characters
        Assert.assertEquals("total length ", 4, p.tostream.available());
        Assert.assertEquals("Char 0", 0x52, p.tostream.readByte()&0xff);
        Assert.assertEquals("Char 1", 0x05, p.tostream.readByte()&0xff);
        Assert.assertEquals("Char 2", 0x8A, p.tostream.readByte()&0xff);
        Assert.assertEquals("parity", 0xDD, p.tostream.readByte()&0xff);
        Assert.assertEquals("remaining ", 0, p.tostream.available());
    }

    public void testInbound() throws Exception {
        LenzCommandStation lcs = new LenzCommandStation();
        XNetPacketizer c = new XNetPacketizer(lcs){
            protected void handleTimeout(jmri.jmrix.AbstractMRMessage msg,jmri.jmrix.AbstractMRListener l) {} // don't care about timeout
            protected void reportReceiveLoopException(Exception e) {}           
            protected void portWarn(Exception e) {}
        };

        // connect to iostream via port controller
        XNetPortControllerScaffold p = new XNetPortControllerScaffold();
        c.connectPort(p);

        // object to receive reply
        XNetListenerScaffold l = new XNetListenerScaffold();
        c.addXNetListener(~0, l);

        // now send reply
        p.tistream.write(0x52);
        p.tistream.write(0x12);
        p.tistream.write(0x12);
        p.tistream.write(0x52);

        // check that the message was picked up by the read thread.
        Assert.assertTrue("reply received ", waitForReply(l));
        Assert.assertEquals("first char of reply ", 0x52, l.rcvdRply.getElement(0));
    }

    public void testInterference() throws Exception {
	// This test checks to make sure that when two listeners register for events
        // at the same time, the first listener is still the active listener until
        // it receives a message.
        LenzCommandStation lcs = new LenzCommandStation();
        XNetPacketizer c = new XNetPacketizer(lcs){
            protected void handleTimeout(jmri.jmrix.AbstractMRMessage msg,jmri.jmrix.AbstractMRListener l) {} // don't care about timeout
            protected void reportReceiveLoopException(Exception e) {}           
            protected void portWarn(Exception e) {}
        };

        // connect to iostream via port controller
        XNetPortControllerScaffold p = new XNetPortControllerScaffold();
        c.connectPort(p);

        // We need three objects to receive messages.
        // The first one recieves broadcast messages. 
        // The others only receive directed messages.

        XNetListenerScaffold l = new XNetListenerScaffold();
        XNetListenerScaffold l1 = new XNetListenerScaffold();
        XNetListenerScaffold l2 = new XNetListenerScaffold();
        c.addXNetListener(~0, l);

        // we're going to loop through this, because we're trying to catch
        // a threading/synchronization issue in AbstractMRTrafficController.
	for(int i=0;i<5;i++){

        // first, we send an unsolicited message
        p.tistream.write(0x42);
        p.tistream.write(0x12);
        p.tistream.write(0x12);
        p.tistream.write(0x42);

        // now we need to send a message with both the second and third listeners 
        // as reply receiver.
        XNetMessage m = XNetMessage.getTurnoutCommandMsg(22, true, false, true);
        c.sendXNetMessage(m, l1);
        
        XNetMessage m1 = XNetMessage.getTurnoutCommandMsg(23, true, false, true);
        c.sendXNetMessage(m1, l2);

	jmri.util.JUnitUtil.releaseThread(this, 500); // Allow time for messages to process into the system

        // and now we verify l1 is the last sender.
	Assert.assertEquals("itteration " +i + " Last Sender l1, before l1 reply",l1,c.getLastSender());

	l.rcvdRply=null;
	l1.rcvdRply=null;
	l2.rcvdRply=null;

        // Now we reply to the messages above
        p.tistream.write(0x01);
        p.tistream.write(0x04);
        p.tistream.write(0x05);

        // check that the message was picked up by the read thread.
        Assert.assertTrue("itteration " + i + " reply received ", waitForReply(l1));
        Assert.assertEquals("itteration " + i +" first char of reply to l1", 0x01, l1.rcvdRply.getElement(0));

	jmri.util.JUnitUtil.releaseThread(this, 500); // Allow time for messages to process into the system
        
        // and now we verify l2 is the last sender.
	Assert.assertEquals("Last Sender l2",l2,c.getLastSender());
	l.rcvdRply=null;
	l1.rcvdRply=null;
	l2.rcvdRply=null;

        p.tistream.write(0x01);
        p.tistream.write(0x04);
        p.tistream.write(0x05);

        // check that the message was picked up by the read thread.
        Assert.assertTrue("itteration "+i+" reply received ", waitForReply(l2));
        Assert.assertEquals("itteration "+i+" first char of reply to l2", 0x01, l2.rcvdRply.getElement(0));
	jmri.util.JUnitUtil.releaseThread(this, 500); // Allow time for messages to process into the system
	l.rcvdRply=null;
	l1.rcvdRply=null;
	l2.rcvdRply=null;
        Assert.assertEquals("itteration "+i+" l received count ", 3*(i+1), l.rcvCount);
        }


    }


    private boolean waitForReply(XNetListenerScaffold l) {
        // wait for reply (normally, done by callback; will check that later)
        int i = 0;
        while ( l.rcvdRply == null && i++ < 100  )  {
            jmri.util.JUnitUtil.releaseThread(this, 10);
        }
        if (log.isDebugEnabled()) log.debug("past loop, i="+i
                                            +" reply="+l.rcvdRply);
        if (i==0) log.warn("waitForReply saw an immediate return; is threading right?");
        return i<100;
    }

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", XNetPacketizerTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

    static Logger log = Logger.getLogger(XNetPacketizerTest.class.getName());

}
