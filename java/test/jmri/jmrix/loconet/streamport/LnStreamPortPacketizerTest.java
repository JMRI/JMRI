package jmri.jmrix.loconet.streamport;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * Tests for jmri.jmrix.loconet.streamport.StreamPortPacketizer
 *
 * @author Bob Jacobsen Copyright (C) 2002
 * @author Paul Bender Copyright (C) 2018
 */
public class LnStreamPortPacketizerTest extends jmri.jmrix.loconet.LnPacketizerTest {

    private LocoNetSystemConnectionMemo memo;
    private LnStreamPortController apc;

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        lnp = new LnStreamPortPacketizer();
        memo = new LocoNetSystemConnectionMemo();
        memo.setLnTrafficController(lnp);
        try {
           PipedInputStream tempPipe;
           tempPipe = new PipedInputStream();
           DataOutputStream ostream;  // Traffic controller writes to this
           DataInputStream tostream; // so we can read it from this
           tostream = new DataInputStream(tempPipe);
           ostream = new DataOutputStream(new PipedOutputStream(tempPipe));
           apc = new LnStreamPortController(memo,tostream,ostream,"Test Stream Port");
       } catch (java.io.IOException ioe) {
           Assert.fail("failed to initialize port controller");
       }
    }

    @Override
    @After
    public void tearDown() {
        memo.dispose();
        lnp = null;
        apc = null;
        memo = null;
        JUnitUtil.tearDown();
    }

    @Override
    @Test
    public void testStartThreads() {
       ((LnStreamPortPacketizer)lnp).connectPort(apc);
       lnp.startThreads();
    }
}
