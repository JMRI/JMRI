package jmri.jmrix.openlcb;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * OlcbProgrammerTest.java
 *
 * Description:	tests for the jmri.jmrix.openlcb.OlcbProgrammer class
 *
 * @author	Bob Jacobsen
 */
public class OlcbProgrammerTest extends jmri.jmrix.AbstractProgrammerTest {

    public void testCtor() {
        Assert.assertNotNull(abstractprogrammer);
    }

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
