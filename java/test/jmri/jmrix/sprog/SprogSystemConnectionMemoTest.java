package jmri.jmrix.sprog;

import jmri.CommandStation;
import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;
import jmri.jmrix.sprog.SprogConstants.SprogMode;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for SprogSystemConnectionMemo.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SprogSystemConnectionMemoTest extends SystemConnectionMemoTestBase<SprogSystemConnectionMemo> {

    @Test
    public void setAndGetSProgMode() {
        scm.setSprogMode(SprogMode.SERVICE);
        Assert.assertEquals("Sprog Mode", SprogMode.SERVICE, scm.getSprogMode());
    }

    @Test
    public void setAndGetTrafficController() {
        SprogTrafficController tc = new SprogTrafficControlScaffold(scm);
        scm.setSprogTrafficController(tc);
        Assert.assertEquals("Traffic Controller", tc, scm.getSprogTrafficController());
        tc.dispose();
    }

    @Test
    public void configureAndGetCSTest() {
        SprogTrafficController tc = new SprogTrafficControlScaffold(scm);
        scm.setSprogTrafficController(tc);
        scm.setSprogMode(SprogMode.SERVICE);
        scm.configureCommandStation();
        Assert.assertNotNull("Command Station", scm.getCommandStation());
        Assert.assertNotNull("Command Station", scm.get(CommandStation.class));
        tc.dispose();
    }

    @Test
    public void configureAndGetOPSCSTest() {
        SprogTrafficController tc = new SprogTrafficControlScaffold(scm);
        scm.setSprogTrafficController(tc);
        scm.setSprogMode(SprogMode.OPS);
        scm.configureCommandStation();
        Assert.assertNotNull("Command Station", scm.getCommandStation());
        Assert.assertNotNull("Command Station", scm.get(CommandStation.class));
        tc.dispose();
    }

    @Test
    public void whenConfigureManagersCalled_CommandStationIsInitialized() {
        SprogTrafficController tc = new SprogTrafficControlScaffold(scm);
        scm.setSprogTrafficController(tc);
        scm.setSprogMode(SprogMode.OPS);
        scm.configureManagers();
        Assert.assertNotNull("Command Station", scm.getCommandStation());
        Assert.assertNotNull("Command Station", scm.get(CommandStation.class));
        tc.dispose();
    }

    @Override
    @Test
    public void testProvidesConsistManager() {
        SprogSystemConnectionMemo memo = new SprogSystemConnectionMemo();
        // by default, does.
        Assert.assertTrue("Provides ConsistManager", memo.provides(jmri.ConsistManager.class));
        // In service mode, does not.
        memo.setSprogMode(SprogMode.SERVICE);
        Assert.assertFalse("Provides ConsistManager", memo.provides(jmri.ConsistManager.class));
        // In ops mode, does.
        memo.setSprogMode(SprogMode.OPS);
        Assert.assertTrue("Provides ConsistManager", memo.provides(jmri.ConsistManager.class));
    }

    private SprogTrafficControlScaffold stcs = null;

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        scm = new SprogSystemConnectionMemo(SprogConstants.SprogMode.OPS);
        stcs = new SprogTrafficControlScaffold(scm);
        scm.setSprogTrafficController(stcs);
        scm.configureManagers();
    }

    @Override
    @AfterEach
    public void tearDown() {
        scm.getSlotThread().interrupt();
        JUnitUtil.waitThreadTerminated(scm.getSlotThread().getName());
        scm.getSprogTrafficController().dispose();
        scm.dispose();
        stcs.dispose();
        JUnitUtil.tearDown();
    }

}
