package jmri.jmrix.jmriclient;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;


/**
 * JMRIClientPowerManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.jmriclient.JMRIClientPowerManager class
 *
 * @author	Bob Jacobsen
 * @author  Paul Bender Copyright (C) 2017
 */
public class JMRIClientPowerManagerTest extends jmri.jmrix.AbstractPowerManagerTestBase {

    private JMRIClientTrafficControlScaffold stc = null;

    // service routines to simulate receiving on, off from interface
    @Override
    protected void hearOn() {
      stc.sendTestReply(new JMRIClientReply("POWER ON\n\r"));
    }

    @Override
    protected void sendOnReply() {
       stc.sendTestReply(new JMRIClientReply("POWER ON\n\r"));
    }

    @Override
    protected void sendOffReply() {
       stc.sendTestReply(new JMRIClientReply("POWER OFF\n\r"));
    }

    @Override
    protected void hearOff() {
       stc.sendTestReply(new JMRIClientReply("POWER OFF\n\r"));
    }

    @Override
    protected void sendIdleReply() {
       stc.sendTestReply(new JMRIClientReply("POWER IDLE\n\r"));
    }
    
    @Override
    protected void hearIdle() {
        stc.sendTestReply(new JMRIClientReply("POWER IDLE\n\r"));
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
        return ((stc.outbound.elementAt(index))).toString().equals("POWER ON\n");
    }

    @Override
    protected boolean outboundOffOK(int index) {
        return ((stc.outbound.elementAt(index))).toString().equals("POWER OFF\n");
    }

    @Override
    protected boolean outboundIdleOK(int index) {
        return ((stc.outbound.elementAt(index))).toString().equals("POWER IDLE\n");
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        stc = new JMRIClientTrafficControlScaffold();
        p = new JMRIClientPowerManager(new JMRIClientSystemConnectionMemo(stc));
    }

    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }


}
