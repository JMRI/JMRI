package jmri.jmrix.sprog;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for SprogOpsModeProgrammer.
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SprogOpsModeProgrammerTest extends jmri.jmrix.AbstractOpsModeProgrammerTestBase {

    private SprogTrafficControlScaffold stcs = null;
    private SprogSystemConnectionMemo m = null;

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        // prepare an interface
        jmri.util.JUnitUtil.resetInstanceManager();

        m = new SprogSystemConnectionMemo(jmri.jmrix.sprog.SprogConstants.SprogMode.OPS);
        stcs = new SprogTrafficControlScaffold(m);
        m.setSprogTrafficController(stcs);
        m.configureCommandStation();

        programmer = new SprogOpsModeProgrammer(2,false,m);
    }

    @After
    @Override
    public void tearDown() {
        m.getSlotThread().interrupt();
        stcs.dispose();
        programmer = null;
        JUnitUtil.tearDown();
    }

}
