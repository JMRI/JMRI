package jmri.jmrix.nce;

import jmri.DccLocoAddress;
import jmri.util.JUnitUtil;
import jmri.util.JUnitAppender;
import jmri.util.junit.annotations.*;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class NceConsistManagerTest extends jmri.implementation.AbstractConsistManagerTestBase {

    private NceTrafficControlScaffold tcis = null;
    private NceSystemConnectionMemo memo = null;

    @Override
    @Test
    @Ignore("produces multiple error messages on CI servers")
    @ToDo("rewrite parent class test here with appropriate replies to consist memory read requests")
    public void testGetConsist() {
        // getConsist with a valid address should always return
        // a consist.
        DccLocoAddress addr = new DccLocoAddress(5, false);
        Assert.assertNotNull("add consist", cm.getConsist(addr));
        tcis.sendTestReply(new NceReply(tcis, "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"));

    }

    @Test
    @Ignore("produces multiple error messages on CI servers")
    @ToDo("rewrite parent class test here with appropriate replies to consist memory read requests")
    @Override
    public void testConsists() {
       super.testConsists();
       // no message is being generated in response to consist memory read
       // messages, so the parent class sometimes produces error messages.  
       // We need to supress those.
       JUnitAppender.suppressErrorMessage("read timeout");
       JUnitAppender.suppressErrorMessage("Time out reading NCE command station consist memory");
    }

    @Override
    @Test
    @Ignore("causes NPE on Appveyor; produces multiple error messages on CI servers")
    @ToDo("rewrite parent class test here with appropriate replies to consist memory read requests. Investigate why Appveyor throws NPE while getting port name from traffic controller")
    public void testRequestUpdateFromLayout() {
       super.testRequestUpdateFromLayout();
       // no message is being generated in response to consist memory read
       // messages, so the parent class sometimes produces error messages.  
       // We need to supress those.
       JUnitAppender.suppressErrorMessage("read timeout");
       JUnitAppender.suppressErrorMessage("Time out reading NCE command station consist memory");
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        tcis = new NceTrafficControlScaffold();
        memo = new NceSystemConnectionMemo();
        memo.setNceTrafficController(tcis);
        cm = new NceConsistManager(memo);
    }

    @After
    @Override
    public void tearDown() {
        cm = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(NceConsistManagerTest.class);
}
