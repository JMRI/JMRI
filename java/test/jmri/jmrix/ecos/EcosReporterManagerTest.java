package jmri.jmrix.ecos;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * EcosReporterManagerTest.java
 *
 * Description:	tests for the EcosReporterManager class
 *
 * @author	Paul Bender Copyright (C) 2012,2016
 */
public class EcosReporterManagerTest extends jmri.managers.AbstractReporterMgrTestBase {

    @Override
    public String getSystemName(String i) {
        return "UR" + i;
    }

    EcosTrafficController tc = null;

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initDefaultUserMessagePreferences();
        tc = new EcosInterfaceScaffold();
        EcosSystemConnectionMemo memo = new EcosSystemConnectionMemo(tc);
        l = new EcosReporterManager(memo);
    }

    @After
    public void tearDown() {
        tc = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }


}
