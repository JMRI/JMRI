package jmri.jmrix.sprog;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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

    // The minimal setup for log4J
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        // prepare an interface
        jmri.util.JUnitUtil.resetInstanceManager();

        m = new SprogSystemConnectionMemo(jmri.jmrix.sprog.SprogConstants.SprogMode.SERVICE);
        stcs = new SprogTrafficControlScaffold(m);
        m.setSprogTrafficController(stcs);

        op = new SprogProgrammer(m);
    }

    @After
    public void tearDown() {
        stcs.dispose();
        op = null;
        m = null;
        stcs = null;
        JUnitUtil.tearDown();
    }


}
