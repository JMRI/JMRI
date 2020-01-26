package jmri.jmrix.srcp;

import jmri.util.JUnitUtil;
import jmri.ProgrammingMode;
import org.junit.*;

/**
 * SRCPProgrammerTest.java
 *
 * Description:	tests for the jmri.jmrix.srcp.SRCPProgrammer class
 *
 * @author	Bob Jacobsen
 */
public class SRCPProgrammerTest extends jmri.jmrix.AbstractProgrammerTest {

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
                ((SRCPProgrammer)programmer).getBestMode());        
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        SRCPBusConnectionMemo sm = new SRCPBusConnectionMemo(new SRCPTrafficController() {
            @Override
            public void sendSRCPMessage(SRCPMessage m, SRCPListener reply) {
            }
        }, "A", 1);
        programmer = new SRCPProgrammer(sm);
    }

    @Override
    @After
    public void tearDown() {
        programmer = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }
}
