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
                abstractprogrammer.getMode());        
    }

    @Test(expected=java.lang.IllegalArgumentException.class)
    public void testSetGetMode() {
        abstractprogrammer.setMode(ProgrammingMode.REGISTERMODE);
        Assert.assertEquals("Check mode matches set", ProgrammingMode.REGISTERMODE,
                abstractprogrammer.getMode());        
    }
    
    @Test

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        //new OlcbSystemConnectionMemo();
        abstractprogrammer = new OlcbProgrammer();
    }

    @Override
    @After
    public void tearDown() {
        abstractprogrammer = null;
        JUnitUtil.tearDown();
    }
}
