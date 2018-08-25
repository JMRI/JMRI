package jmri.jmrix.sprog;

import jmri.ProgrammingMode;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for SprogProgrammer.
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SprogProgrammerTest extends jmri.jmrix.AbstractProgrammerTest {

    private SprogTrafficControlScaffold stcs = null;
    private SprogProgrammer op = null;

    @Test
    @Override
    public void testDefault() {
        Assert.assertEquals("Check Default", ProgrammingMode.DIRECTBITMODE,
                abstractprogrammer.getMode());        
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    @Override
    public void testSetGetMode() {
        abstractprogrammer.setMode(ProgrammingMode.REGISTERMODE);
        Assert.assertEquals("Check mode matches set", ProgrammingMode.REGISTERMODE,
                abstractprogrammer.getMode());        
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        // prepare an interface
        jmri.util.JUnitUtil.resetInstanceManager();

        SprogSystemConnectionMemo m = new SprogSystemConnectionMemo(jmri.jmrix.sprog.SprogConstants.SprogMode.SERVICE);
        stcs = new SprogTrafficControlScaffold(m);
        m.setSprogTrafficController(stcs);

        abstractprogrammer = op = new SprogProgrammer(m);
    }

    @After
    public void tearDown() {
        stcs.dispose();
        abstractprogrammer = op = null;
        JUnitUtil.tearDown();
    }

}
