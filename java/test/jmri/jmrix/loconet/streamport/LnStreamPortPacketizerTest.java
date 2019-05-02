package jmri.jmrix.loconet.streamport;

import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.*;
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
    
    @SuppressWarnings("unused") // partial implementation of test? See jmri.jmrix.AbstractPortControllerScaffold
    private DataInputStream tostream; // so we can read it from this

    @SuppressWarnings("unused") // partial implementation of test? See jmri.jmrix.AbstractPortControllerScaffold
    private DataOutputStream tistream; // tests write to this
    
    private DataInputStream istream;   // so the traffic controller can read from this

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        memo = new LocoNetSystemConnectionMemo();
        lnp = new LnStreamPortPacketizer(memo);
        memo.setLnTrafficController(lnp);
        try {
           PipedInputStream tempPipe;
           tempPipe = new PipedInputStream();
           tostream = new DataInputStream(tempPipe);
           ostream = new DataOutputStream(new PipedOutputStream(tempPipe));

           tempPipe = new PipedInputStream();
           istream = new DataInputStream(tempPipe);
           tistream = new DataOutputStream(new PipedOutputStream(tempPipe));
           apc = new LnStreamPortController(memo, istream, ostream, "Test Stream Port");
       } catch (java.io.IOException ioe) {
           Assert.fail("failed to initialize port controller");
       }
    }

    @Override
    @After
    public void tearDown() {
        memo.dispose();
        lnp.terminateThreads();
        lnp = null;
        apc.dispose();
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
