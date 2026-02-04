package jmri.jmrix.lenz;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * LenzSpecificOpsModeProgrammerTest.java
 *
 * Test for the jmri.jmrix.lenz.XNetOpsModeProgrammer class
 * Note that this class has tests that test Lenz specific behavior.
 *
 * @author Paul Bender
 */

public class LenzSpecificOpsModeProgrammerTest {

    protected XNetOpsModeProgrammer op = null;
    protected XNetInterfaceScaffold tc = null;
    protected jmri.ProgListener pl = null;
    protected int lastValue;
    protected int lastStatus;

    @Test
    public void testConfirmCVWithNotSupported() throws jmri.ProgrammerException{
        op.confirmCV("29",5,pl);
        XNetMessage m = XNetMessage.getVerifyOpsModeCVMsg(0,5,29,5);
        Assert.assertEquals("outbound message sent",1,tc.outbound.size());
        Assert.assertEquals("outbound message",m,tc.outbound.elementAt(0));
        // send a message reply
        op.message(new XNetReply("01 04 05")); // send "OK" message to the programmer.
        // verify the result request was sent
        Assert.assertEquals("outbound message sent",2,tc.outbound.size());
        m = XNetMessage.getOpsModeResultsMsg();
        Assert.assertEquals("outbound message",m,tc.outbound.elementAt(1));
        //reply with not supported
        op.message(new XNetReply("61 82 E3")); // send "Not Supported" message to the programmer.
        // and now we need to check the status is right
        jmri.util.JUnitUtil.waitFor( ()->{ return lastValue == 5; }, "written value" );
        Assert.assertEquals("status",jmri.ProgListener.NotImplemented,lastStatus);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // infrastructure objects
        tc = new XNetInterfaceScaffold(new LenzCommandStation());

        op = new XNetOpsModeProgrammer(5, tc);

        pl = (value, status) -> {
            lastValue = value;
            lastStatus = status;
        };

        lastValue = -1;
        lastStatus = -1;

    }

    @AfterEach
    public void tearDown() {
        tc = null;
        op = null;
        pl = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
