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
        Assert.assertEquals("Check Default", OlcbProgrammerManager.OPENLCBMODE,
                programmer.getMode());        
    }
    
    @Override
    @Test
    public void testDefaultViaBestMode() {
        Assert.assertEquals("Check Default", OlcbProgrammerManager.OPENLCBMODE,
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
        //new OlcbSystemConnectionMemo();
        programmer = new OlcbProgrammer();
    }

    @Override
    @AfterEach
    public void tearDown() {
        programmer = null;
        JUnitUtil.tearDown();
    }
}
