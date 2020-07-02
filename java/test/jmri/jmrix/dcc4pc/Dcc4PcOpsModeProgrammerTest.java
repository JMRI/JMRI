package jmri.jmrix.dcc4pc;

import jmri.progdebugger.DebugProgrammerManager;
import jmri.ProgrammingMode;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class Dcc4PcOpsModeProgrammerTest extends jmri.jmrix.AbstractOpsModeProgrammerTestBase {

    @Test
    @Override
    public void testGetCanRead(){
       Assert.assertTrue("can read",programmer.getCanRead());
    }

    @Test
    @Override
    public void testDefault() {
        Assert.assertEquals("Check Default", ProgrammingMode.OPSBITMODE,
                programmer.getMode());        
    }

    @Override
    @Test
    public void testDefaultViaBestMode() {
        Assert.assertEquals("Check Default", ProgrammingMode.OPSBITMODE,
                ((Dcc4PcOpsModeProgrammer)programmer).getBestMode());        
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        Dcc4PcProgrammerManager pm = new Dcc4PcProgrammerManager(new DebugProgrammerManager());
        Dcc4PcOpsModeProgrammer t = new Dcc4PcOpsModeProgrammer(false, 5, pm);
        programmer = t;
    }

    @AfterEach
    @Override
    public void tearDown() {
        programmer = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(Dcc4PcOpsModeProgrammerTest.class);
}
