package jmri.jmrix.sprog;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * <P>
 * Tests for SprogOpsModeProgrammer
 * </P>
 * @author Paul Bender Copyright (C) 2017
 */
public class SprogOpsModeProgrammerTest {

    private SprogTrafficControlScaffold stcs = null;
    private SprogOpsModeProgrammer op = null;

    @Test
    public void testCtor(){
       Assert.assertNotNull("exists",op);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        // prepare an interface
        jmri.util.JUnitUtil.resetInstanceManager();

        SprogSystemConnectionMemo m = new SprogSystemConnectionMemo(jmri.jmrix.sprog.SprogConstants.SprogMode.OPS);
        stcs = new SprogTrafficControlScaffold(m);
        m.setSprogTrafficController(stcs);

        op = new SprogOpsModeProgrammer(2,false,m);
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }


}
