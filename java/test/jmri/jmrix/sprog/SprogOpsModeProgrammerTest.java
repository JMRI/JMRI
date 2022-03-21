package jmri.jmrix.sprog;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for SprogOpsModeProgrammer.
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SprogOpsModeProgrammerTest extends jmri.jmrix.AbstractOpsModeProgrammerTestBase {

    private SprogTrafficControlScaffold stcs = null;
    private SprogSystemConnectionMemo m = null;

    @BeforeEach
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

    @AfterEach
    @Override
    public void tearDown() {
        m.getSlotThread().interrupt();
        m.dispose();
        JUnitUtil.waitFor(() -> { return !m.getSlotThread().isAlive(); });
        stcs.dispose();
        programmer = null;
        JUnitUtil.tearDown();
    }

}
