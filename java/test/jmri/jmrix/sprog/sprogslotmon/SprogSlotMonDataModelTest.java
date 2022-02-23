package jmri.jmrix.sprog.sprogslotmon;

import jmri.jmrix.sprog.SprogSystemConnectionMemo;
import jmri.jmrix.sprog.SprogTrafficControlScaffold;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Test simple functioning of SprogSlotMonDataModel 
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SprogSlotMonDataModelTest {

    private SprogTrafficControlScaffold stcs = null;
    private SprogSystemConnectionMemo m = null;

    @Test
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    public void testCtor() {
        int numSlots = m.getNumSlots();
        SprogSlotMonDataModel action = new SprogSlotMonDataModel(numSlots, 8, m);
        Assert.assertNotNull("exists", action);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        m = new jmri.jmrix.sprog.SprogSystemConnectionMemo(jmri.jmrix.sprog.SprogConstants.SprogMode.OPS);
        stcs = new SprogTrafficControlScaffold(m);
        m.setSprogTrafficController(stcs);
        m.configureCommandStation();
    }

    @AfterEach
    public void tearDown() {
        m.getSlotThread().interrupt();
        m.dispose();
        JUnitUtil.waitFor(() -> { return !m.getSlotThread().isAlive(); });
        stcs.dispose();
        JUnitUtil.tearDown();
    }
}
