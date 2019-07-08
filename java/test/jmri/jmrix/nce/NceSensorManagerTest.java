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

    @Test
    public void testValidateSystemNameFormat() {
        Assert.assertEquals("NS4:3", l.validateSystemNameFormat("NS4:3", false));
        Assert.assertEquals("NS50", l.validateSystemNameFormat("NS50", false));
        Assert.assertEquals("NS0", l.validateSystemNameFormat("NS0", false));
        try {
            l.validateSystemNameFormat("NS0:0", false);
            Assert.fail("Expected exception not thrown");
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals("\"NS0:0\" must use an AIU address from 1 to 63", ex.getMessage());
        }
        try {
            l.validateSystemNameFormat("NS64:7", false);
            Assert.fail("Expected exception not thrown");
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals("\"NS64:7\" must use an AIU address from 1 to 63", ex.getMessage());
        }
        try {
            l.validateSystemNameFormat("NS2:15", false);
            Assert.fail("Expected exception not thrown");
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals("\"NS2:15\" must use an AIU pin from 1 to 14", ex.getMessage());
        }
        try {
            l.validateSystemNameFormat("NS14", false);
            Assert.fail("Expected exception not thrown");
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals("\"NS14\" must use an AIU pin from 1 to 14", ex.getMessage());
        }
        try {
            l.validateSystemNameFormat("NS47", false);
            Assert.fail("Expected exception not thrown");
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals("\"NS47\" must use an AIU pin from 1 to 14", ex.getMessage());
        }
        try {
            l.validateSystemNameFormat("NS1006", false);
            Assert.fail("Expected exception not thrown");
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals("\"NS1006\" must use an AIU pin from 1 to 14", ex.getMessage());
        }
        try {
            l.validateSystemNameFormat("NS1008", false);
            Assert.fail("Expected exception not thrown");
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals("\"NS1008\" must use an AIU address from 1 to 63", ex.getMessage());
        }
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
