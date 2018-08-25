package jmri.jmrix.dcc4pc;

import jmri.progdebugger.DebugProgrammerManager;
import jmri.ProgrammingMode;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class Dcc4PcOpsModeProgrammerTest extends jmri.jmrix.AbstractOpsModeProgrammerTestBase {

    @Test
    @Override
    public void testGetCanRead(){
       Assert.assertTrue("can read",abstractprogrammer.getCanRead());
    }

    @Test
    @Override
    public void testDefault() {
        Assert.assertEquals("Check Default", ProgrammingMode.OPSBITMODE,
                abstractprogrammer.getMode());        
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        Dcc4PcProgrammerManager pm = new Dcc4PcProgrammerManager(new DebugProgrammerManager());
        Dcc4PcOpsModeProgrammer t = new Dcc4PcOpsModeProgrammer(false, 5, pm);
        abstractprogrammer = t;
    }

    @After
    public void tearDown() {
        abstractprogrammer = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(Dcc4PcOpsModeProgrammerTest.class);
}
