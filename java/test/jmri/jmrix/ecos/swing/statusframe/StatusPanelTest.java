package jmri.jmrix.ecos.swing.statusframe;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of StatusPanel
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class StatusPanelTest {

    jmri.jmrix.ecos.EcosSystemConnectionMemo memo = null;

    @Test
    public void testCtor() {
        StatusPanel action = new StatusPanel();
        Assert.assertNotNull("exists", action);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initDefaultUserMessagePreferences();
        memo = new jmri.jmrix.ecos.EcosSystemConnectionMemo();

        jmri.InstanceManager.store(memo, jmri.jmrix.ecos.EcosSystemConnectionMemo.class);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }
}
