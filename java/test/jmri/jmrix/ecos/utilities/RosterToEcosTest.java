package jmri.jmrix.ecos.utilities;

import jmri.jmrix.ecos.EcosSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the RosterToEcos class.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class RosterToEcosTest {

    @Test
    public void testCtor() {
        Assert.assertNotNull("RosterToEcos constructor", new RosterToEcos(new EcosSystemConnectionMemo()));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initDefaultUserMessagePreferences();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
