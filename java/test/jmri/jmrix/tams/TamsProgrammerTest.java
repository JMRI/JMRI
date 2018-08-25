package jmri.jmrix.tams;

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
public class TamsProgrammerTest extends jmri.jmrix.AbstractProgrammerTest {

    @Test
    @Override
    public void testDefault() {
        Assert.assertEquals("Check Default", ProgrammingMode.PAGEMODE,
                abstractprogrammer.getMode());        
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        TamsTrafficController tc = new TamsTrafficController();
        TamsProgrammer t = new TamsProgrammer(tc);
        abstractprogrammer = t;
    }

    @After
    @Override
    public void tearDown() {
        abstractprogrammer = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TamsProgrammerTest.class);

}
