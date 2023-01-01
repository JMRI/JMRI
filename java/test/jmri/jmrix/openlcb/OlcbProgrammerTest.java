package jmri.jmrix.openlcb;

import jmri.util.JUnitUtil;
import jmri.ProgrammingMode;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.openlcb.NodeID;

/**
 * OlcbProgrammerTest.java
 *
 * Test for the jmri.jmrix.openlcb.OlcbProgrammer class
 *
 * @author Bob Jacobsen
 */
public class OlcbProgrammerTest extends jmri.jmrix.AbstractProgrammerTest {

    @Test
    @Override
    public void testDefault() {
        Assert.assertEquals("Check Default", ProgrammingMode.DIRECTBYTEMODE,
                programmer.getMode());        
    }
    
    @Override
    @Test
    public void testDefaultViaBestMode() {
        Assert.assertEquals("Check Default", ProgrammingMode.DIRECTBYTEMODE,
                ((OlcbProgrammer)programmer).getBestMode());        
    }

    @Test
    @Override
    public void testSetGetMode() {
        Assert.assertThrows(IllegalArgumentException.class, () -> programmer.setMode(ProgrammingMode.REGISTERMODE));
    }

    @Test
    public void testFindProgramTrack() {
        ti.flush();
        ti.assertSentMessage(":X19914C4CN090099FEFFFF0002;");
        ti.clearSentMessages();
        Assert.assertNull(prog.nid);
        // Seeds alias map with a node ID.
        ti.sendMessage(":X19100555N050101011807;");
        ti.flush();
        // Producer identified.
        ti.sendMessage(":X19547555N090099FEFFFF0002;");
        ti.flush();
        Assert.assertEquals(new NodeID("05.01.01.01.18.07"), prog.nid);
        ti.assertNoSentMessages();
    }

    @Test
    public void testAddressedReadLookup() {
        ti.flush();
        ti.clearSentMessages();
        programmer = prog = new OlcbProgrammer(ti.iface, true, 15);
        Assert.assertEquals(new NodeID("06.01.00.00.C0.0F"), prog.nid);
        // There is a verify node ID message sent.
        ti.assertSentMessage(":X194904C4N06010000C00F;");
        //ti.assertNoSentMessages();
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        ti = new OlcbTestInterface();
        ti.waitForStartup();
        ti.flush();
        log.warn("hello");
        programmer = prog = new OlcbProgrammer(ti.iface, null);
    }

    @Override
    @AfterEach
    public void tearDown() {
        programmer = null;
        ti.dispose();
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

    OlcbTestInterface ti;
    OlcbProgrammer prog;

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OlcbProgrammerTest.class);
}
