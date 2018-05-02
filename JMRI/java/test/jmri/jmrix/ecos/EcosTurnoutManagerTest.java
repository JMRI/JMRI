package jmri.jmrix.ecos;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * EcosTurnoutManagerTest.java
 *
 * Description:	tests for the EcosTurnoutManager class
 *
 * @author	Paul Bender Copyright (C) 2012,2016
 */
public class EcosTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTestBase {

    @Override
    public String getSystemName(int i) {
        return "UT" + i;
    }

    EcosTrafficController tc = null;

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        tc = new EcosInterfaceScaffold();
        EcosSystemConnectionMemo memo = new EcosSystemConnectionMemo(tc);
        l = new EcosTurnoutManager(memo);
    }

    @After
    public void tearDown() {
        tc = null;
        JUnitUtil.tearDown();
    }


}
