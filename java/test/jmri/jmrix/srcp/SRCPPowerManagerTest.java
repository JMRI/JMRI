package jmri.jmrix.srcp;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.*;
import org.junit.*;

/**
 * SRCPPowerManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.srcp.SRCPPowerManager class
 *
 * @author	Bob Jacobsen
 */
public class SRCPPowerManagerTest extends jmri.jmrix.AbstractPowerManagerTestBase {

    private SRCPTrafficControlScaffold stc = null;
  
    // service routines to simulate receiving on, off from interface
    @Override
    protected void hearOn() {
       stc.sendTestReply(new SRCPReply("12345678910 100 INFO 1 POWER ON hello world\n\r"));
    }

    @Override
    protected void sendOnReply() {
       stc.sendTestReply(new SRCPReply("12345678910 100 INFO 1 POWER ON hello world\n\r"));
    }

    @Override
    protected void sendOffReply() {
       stc.sendTestReply(new SRCPReply("12345678910 100 INFO 1 POWER OFF hello world\n\r"));
    }

    @Override
    protected void hearOff() {
       stc.sendTestReply(new SRCPReply("12345678910 100 INFO 1 POWER OFF hello world\n\r"));
    }

    @Override
    protected void sendIdleReply() {
       return;
    }

    @Override
    protected void hearIdle() {
       return;
    }

    @Override
    protected int numListeners() {
        return stc.numListeners();
    }

    @Override
    protected int outboundSize() {
        return stc.outbound.size();
    }

    @Override
    protected boolean outboundOnOK(int index) {
        return ((stc.outbound.elementAt(index))).toString().equals("SET 1 POWER ON\n");
    }

    @Override
    protected boolean outboundOffOK(int index) {
        return ((stc.outbound.elementAt(index))).toString().equals("SET 1 POWER OFF\n");
    }

    @Override
    protected boolean outboundIdleOK(int index) {
        return ((stc.outbound.elementAt(index))).toString().equals("SET 1 POWER OFF\n");
    }

    @Test
    @Override
    @Ignore("unsolicited state changes are currently ignored")
    @ToDo("class under test needs modification to respond to unsolicited state changes.  Once that is in place, remove this overriden test")
    public void testStateOn(){
    }

    @Test
    @Override
    @Ignore("unsolicited state changes are currently ignored")
    @ToDo("class under test needs modification to respond to unsolicited state changes.  Once that is in place, remove this overriden test")
    public void testStateOff(){
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        stc = new SRCPTrafficControlScaffold();
        SRCPBusConnectionMemo memo = new SRCPBusConnectionMemo(stc, "TEST", 1);
        p = new SRCPPowerManager(memo, 1);
    }

    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }
}
