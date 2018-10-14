package jmri.jmrix.nce;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the NceAIU class.
 *
 * @author	Bob Jacobsen Copyright 2002
 * @author Paul Bender Copyright (C) 2016
 */
public class NceSensorManagerTest extends jmri.managers.AbstractSensorMgrTestBase {

    private NceInterfaceScaffold lnis = null;

    @Override
    public String getSystemName(int i) {
        return "NS" + i;
    }

    @Test
    public void testNceSensorCreate() {
        Assert.assertNotNull("exists", l);
    }

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();

        // prepare an interface
        lnis = new NceInterfaceScaffold();
        Assert.assertNotNull("exists", lnis);

        // create and register the manager object
        l = new NceSensorManager(lnis, "N");
        jmri.InstanceManager.setSensorManager(l);
    }

    @After
    public void tearDown() {
        l.dispose();
        JUnitUtil.tearDown();
    }

}
