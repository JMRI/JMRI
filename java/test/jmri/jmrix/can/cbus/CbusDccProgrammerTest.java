package jmri.jmrix.can.cbus;

import jmri.ProgrammingMode;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class CbusDccProgrammerTest extends jmri.jmrix.AbstractProgrammerTest {

    @Test
    @Override
    public void testDefault() {
        Assert.assertEquals("Check Default", ProgrammingMode.PAGEMODE,
                programmer.getMode());        
    }
    
    @Override
    @Test
    public void testDefaultViaBestMode() {
        Assert.assertEquals("Check Default", ProgrammingMode.PAGEMODE,
                ((CbusDccProgrammer)programmer).getBestMode());        
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        programmer = new CbusDccProgrammer(new TrafficControllerScaffold());
    }

    @Override
    @After
    public void tearDown() {
        programmer = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusDccProgrammerTest.class);

}
