package jmri.jmrix.openlcb;

import jmri.util.JUnitUtil;
import jmri.ProgrammingMode;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        testIface = new OlcbTestInterface();
        testIface.waitForStartup();
        programmer = new OlcbProgrammer(testIface.iface, null);
    }

    @Override
    @AfterEach
    public void tearDown() {
        programmer = null;
        testIface.dispose();
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

    OlcbTestInterface testIface;
}
