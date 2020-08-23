package jmri.jmrix.sprog;

import jmri.ProgrammingMode;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for SprogProgrammer.
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SprogProgrammerTest extends jmri.jmrix.AbstractProgrammerTest {

    private SprogTrafficControlScaffold stcs = null;

    @Test
    @Override
    public void testDefault() {
        Assert.assertEquals("Check Default", ProgrammingMode.DIRECTBITMODE,
                programmer.getMode());        
    }
    
    @Override
    @Test
    public void testDefaultViaBestMode() {
        Assert.assertEquals("Check Default", ProgrammingMode.DIRECTBITMODE,
                ((SprogProgrammer)programmer).getBestMode());        
    }

    @Test
    @Override
    public void testSetGetMode() {
        Assert.assertThrows(IllegalArgumentException.class, () -> programmer.setMode(ProgrammingMode.REGISTERMODE));        
    }

    @BeforeEach
    @Override
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        // prepare an interface
        jmri.util.JUnitUtil.resetInstanceManager();

        SprogSystemConnectionMemo m = new SprogSystemConnectionMemo(jmri.jmrix.sprog.SprogConstants.SprogMode.SERVICE);
        stcs = new SprogTrafficControlScaffold(m);
        m.setSprogTrafficController(stcs);
        m.configureManagers();
        programmer = new SprogProgrammer(m);
    }

    @AfterEach
    @Override
    public void tearDown() {
        stcs.dispose();
        programmer = null;
        JUnitUtil.tearDown();
    }

}
