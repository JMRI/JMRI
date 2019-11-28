package jmri.jmrix.lenz;

import java.util.ArrayList;
import java.util.List;
import jmri.ProgrammingMode;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * XNetOpsModeProgrammerTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.XNetOpsModeProgrammer class
 *
 * @author	Paul Bender
 */
public class XNetOpsModeProgrammerTest extends jmri.jmrix.AbstractOpsModeProgrammerTestBase {

    protected XNetOpsModeProgrammer op = null;
    protected XNetInterfaceScaffold tc = null;
    protected jmri.ProgListener pl = null;
    protected int lastValue;
    protected int lastStatus;

    @Test
    public void testSupportedModes(){
       // getSupportedModes() will return a list
       // containing only ProgrammingMode.OPSBYTEMODE.
       List<ProgrammingMode> list= new ArrayList<>();
       list.add(ProgrammingMode.OPSBYTEMODE);
       Assert.assertEquals("Modes",list,op.getSupportedModes());
    }

    @Test
    @Override
    public void testGetCanRead(){
       Assert.assertTrue("can read",op.getCanRead());
    }

    @Test
    public void testLongAddress(){
       Assert.assertTrue("long address",op.getLongAddress());
    }

    @Test
    @Override
    public void testGetAddressNumber(){
       Assert.assertEquals("address",5,op.getAddressNumber());
    }

    @Test
    @Override
    public void testGetAddress(){
       Assert.assertEquals("address","5 true",op.getAddress());
    }

    @Test
    public void testWriteCV() throws jmri.ProgrammerException{
        op.writeCV("29",5,pl);
        XNetMessage m = XNetMessage.getWriteOpsModeCVMsg(0,5,29,5);
        Assert.assertEquals("outbound message sent",1,tc.outbound.size());
        Assert.assertEquals("outbound message",m,tc.outbound.elementAt(0));
        op.message(new XNetReply("01 04 05")); // send "OK" message to the programmer.
        // and now we need to check the status is right
        Assert.assertEquals("written value",5,lastValue);
        Assert.assertEquals("status",jmri.ProgListener.OK,lastStatus);
    }

    @Test
    public void testReadCV() throws jmri.ProgrammerException{
        op.readCV("29",pl);
        XNetMessage m = XNetMessage.getVerifyOpsModeCVMsg(0,5,29,0);
        Assert.assertEquals("outbound message sent",1,tc.outbound.size());
        Assert.assertEquals("outbound message",m,tc.outbound.elementAt(0));
        // and now we need to check the status is right
        Assert.assertEquals("written value",29,lastValue);
        Assert.assertEquals("status",jmri.ProgListener.NotImplemented,lastStatus);
        // send a message reply
        op.message(new XNetReply("01 04 05")); // send "OK" message to the programmer.
        // and verify the status is the same.
        Assert.assertEquals("written value",29,lastValue);
        Assert.assertEquals("status",jmri.ProgListener.NotImplemented,lastStatus);
    }

    @Test
    public void testConfirmCV() throws jmri.ProgrammerException{
        op.confirmCV("29",5,pl);
        XNetMessage m = XNetMessage.getVerifyOpsModeCVMsg(0,5,29,5);
        Assert.assertEquals("outbound message sent",1,tc.outbound.size());
        Assert.assertEquals("outbound message",m,tc.outbound.elementAt(0));
        // and now we need to check the status is right
        Assert.assertEquals("written value",5,lastValue);
        Assert.assertEquals("status",jmri.ProgListener.NotImplemented,lastStatus);
        // send a message reply
        op.message(new XNetReply("01 04 05")); // send "OK" message to the programmer.
        // and verify the status is the same.
        Assert.assertEquals("written value",5,lastValue);
        Assert.assertEquals("status",jmri.ProgListener.NotImplemented,lastStatus);
    }

    @Test
    public void testWriteCVWithNotSupported() throws jmri.ProgrammerException{
        op.writeCV("29",5,pl);
        XNetMessage m = XNetMessage.getWriteOpsModeCVMsg(0,5,29,5);
        Assert.assertEquals("outbound message sent",1,tc.outbound.size());
        Assert.assertEquals("outbound message",m,tc.outbound.elementAt(0));
        op.message(new XNetReply("61 82 E3")); // send "Not Supported" message to the programmer.
        // and now we need to check the status is right
        Assert.assertEquals("written value",5,lastValue);
        Assert.assertEquals("status",jmri.ProgListener.NotImplemented,lastStatus);
    }

    @Test
    public void testWriteCVWithRetransmittableError() throws jmri.ProgrammerException{
        op.writeCV("29",5,pl);
        XNetMessage m = XNetMessage.getWriteOpsModeCVMsg(0,5,29,5);
        Assert.assertEquals("outbound message sent",1,tc.outbound.size());
        Assert.assertEquals("outbound message",m,tc.outbound.elementAt(0));
        op.message(new XNetReply("61 80 E1")); // send "Transfer Error" message to the programmer.

        // and now we need to check the status is right
        // these should be the defaults set in setUp.
        Assert.assertEquals("written value",-1,lastValue);
        Assert.assertEquals("status",-1,lastStatus);

        // then finish without an error (tc will retransmit).
        op.message(new XNetReply("01 04 05")); // send "OK" message to the programmer.
        // and now we need to check the status is right
        Assert.assertEquals("written value",5,lastValue);
        Assert.assertEquals("status",jmri.ProgListener.OK,lastStatus);
    }

    @Test
    public void testWriteCVWithOtherError() throws jmri.ProgrammerException{
        op.writeCV("29",5,pl);
        XNetMessage m = XNetMessage.getWriteOpsModeCVMsg(0,5,29,5);
        Assert.assertEquals("outbound message sent",1,tc.outbound.size());
        Assert.assertEquals("outbound message",m,tc.outbound.elementAt(0));
        op.message(new XNetReply("61 02 63")); // send "Service Mode Entry" message to the programmer, which is an error here.
        // and now we need to check the status is right
        Assert.assertEquals("written value",5,lastValue);
        Assert.assertEquals("status",jmri.ProgListener.UnknownError,lastStatus);
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        // infrastructure objects
        tc = new XNetInterfaceScaffold(new LenzCommandStation());

        op = new XNetOpsModeProgrammer(5, tc);

        pl = new jmri.ProgListener(){
           @Override
           public void programmingOpReply(int value, int status){
                 lastValue = value;
                 lastStatus = status;
           }
        };

        lastValue = -1;
        lastStatus = -1;
        programmer = op;

    }

    @After
    @Override
    public void tearDown() {
        tc = null;
        op = null;
        pl = null;
        programmer = null;
	    JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
