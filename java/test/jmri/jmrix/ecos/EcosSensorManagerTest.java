package jmri.jmrix.ecos;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import jmri.Sensor;

/**
 * EcosSensorManagerTest.java
 *
 * Description:	tests for the EcosSensorManager class
 *
 * @author	Paul Bender Copyright (C) 2012,2016
 */
public class EcosSensorManagerTest extends jmri.managers.AbstractSensorMgrTest {

    @Override
    public String getSystemName(int i) {
        return "US" + i;
    }

    EcosTrafficController tc = null;

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        tc = new EcosInterfaceScaffold();
        EcosSystemConnectionMemo memo = new EcosSystemConnectionMemo(tc);
        l = new EcosSensorManager(memo);
    }

    @After
    public void tearDown() {
        tc = null;
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }


}
