package jmri.jmrix.roco.z21;

import java.util.ArrayList;
import java.util.List;
import jmri.ProgrammingMode;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import jmri.jmrix.lenz.XNetInterfaceScaffold;
import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.XNetReply;

/**
 * Z21XNetOpsModeProgrammerTest.java
 *
 * Description:	tests for the jmri.jmrix.roco.z21.Z21XNetOpsModeProgrammer class
 *
 * @author	Paul Bender
 */
public class Z21XNetOpsModeProgrammerTest extends jmri.jmrix.lenz.XNetOpsModeProgrammerTest {

    @Override
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

    @Override
    @Test
    public void testReadCV() throws jmri.ProgrammerException{
        op.readCV("29",pl);
        XNetMessage m = XNetMessage.getVerifyOpsModeCVMsg(0,5,29,0);
        Assert.assertEquals("outbound message sent",1,tc.outbound.size());
        Assert.assertEquals("outbound message",m,tc.outbound.elementAt(0));
        // send a message reply
        op.message(new XNetReply("64 14 00 1C 05 69"));
        jmri.util.JUnitUtil.waitFor(()->{return lastValue != -1;}, "Receive Called by Programmer");
        // and verify the status is right.
        Assert.assertEquals("read value",5,lastValue);
        Assert.assertEquals("status",jmri.ProgListener.OK,lastStatus);
    }

    @Override
    @Test
    public void testConfirmCV() throws jmri.ProgrammerException{
        op.confirmCV("29",5,pl);
        XNetMessage m = XNetMessage.getVerifyOpsModeCVMsg(0,5,29,5);
        Assert.assertEquals("outbound message sent",1,tc.outbound.size());
        Assert.assertEquals("outbound message",m,tc.outbound.elementAt(0));
        // send a message reply
        op.message(new XNetReply("64 14 00 1C 05 69"));
        jmri.util.JUnitUtil.waitFor(()->{return lastValue != -1;}, "Receive Called by Programmer");
        // and now we need to check the status is right
        Assert.assertEquals("confirm value",5,lastValue);
        Assert.assertEquals("status",jmri.ProgListener.OK,lastStatus);
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        // infrastructure objects
        tc = new XNetInterfaceScaffold(new RocoZ21CommandStation());

        op = new Z21XNetOpsModeProgrammer(5, tc);

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
