package jmri.jmrix.sprog;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for SprogProgrammerManager.
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SprogProgrammerManagerTest {

    private SprogTrafficControlScaffold stcs = null;
    private SprogSystemConnectionMemo m = null;
    private SprogProgrammer op = null;

    @Test
    public void testCtor(){
       Assert.assertNotNull("exists",new SprogProgrammerManager(op,m));
    }

    @Test
    public void testModeCtor(){
       Assert.assertNotNull("exists",new SprogProgrammerManager(op,SprogConstants.SprogMode.SERVICE,m));
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        // prepare an interface
        jmri.util.JUnitUtil.resetInstanceManager();

        m = new SprogSystemConnectionMemo(jmri.jmrix.sprog.SprogConstants.SprogMode.SERVICE);
        stcs = new SprogTrafficControlScaffold(m);
        m.setSprogTrafficController(stcs);

        op = new SprogProgrammer(m);
    }

    @AfterEach
    public void tearDown() {
        stcs.dispose();
        op = null;
        m = null;
        stcs = null;
        JUnitUtil.tearDown();
    }


}
