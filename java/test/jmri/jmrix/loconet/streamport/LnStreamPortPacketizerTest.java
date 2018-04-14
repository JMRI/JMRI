package jmri.jmrix.loconet.streamport;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
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
           
    private DataOutputStream ostream;  // Traffic controller writes to this
    private DataInputStream tostream; // so we can read it from this

    private DataOutputStream tistream; // tests write to this
    private DataInputStream istream;   // so the traffic controller can read from this

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
           tostream = new DataInputStream(tempPipe);
           ostream = new DataOutputStream(new PipedOutputStream(tempPipe));

           tempPipe = new PipedInputStream();
           istream = new DataInputStream(tempPipe);
           tistream = new DataOutputStream(new PipedOutputStream(tempPipe));
           apc = new LnStreamPortController(memo,istream,ostream,"Test Stream Port");
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
        istream = null;
        tistream = null;
        ostream = null;
        tostream = null;
        JUnitUtil.tearDown();
    }

    @Override
    @Test
    @Ignore("may be causing hang on travis and appveyor")
    public void testStartThreads() {
       ((LnStreamPortPacketizer)lnp).connectPort(apc);
       lnp.startThreads();
    }
}
