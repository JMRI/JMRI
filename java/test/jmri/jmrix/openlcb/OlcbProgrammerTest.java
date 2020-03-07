package jmri.jmrix.openlcb;

import jmri.util.JUnitUtil;
import jmri.ProgrammingMode;
import org.junit.*;

/**
 * OlcbProgrammerTest.java
 *
 * Description:	tests for the jmri.jmrix.openlcb.OlcbProgrammer class
 *
 * @author	Bob Jacobsen
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

    @Test(expected=java.lang.IllegalArgumentException.class)
    @Override
    public void testSetGetMode() {
        programmer.setMode(ProgrammingMode.REGISTERMODE);
        Assert.assertEquals("Check mode matches set", ProgrammingMode.REGISTERMODE,
                programmer.getMode());        
    }
    
    @Test

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        //new OlcbSystemConnectionMemo();
        programmer = new OlcbProgrammer();
    }

    @Override
    @After
    public void tearDown() {
        programmer = null;
        JUnitUtil.tearDown();
    }
}
