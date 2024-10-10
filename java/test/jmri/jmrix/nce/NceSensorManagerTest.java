package jmri.jmrix.nce;

import jmri.ProvidingManager;
import jmri.Sensor;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import java.beans.PropertyVetoException;

/**
 * JUnit tests for the NceAIU class.
 *
 * @author Bob Jacobsen Copyright 2002
 * @author Paul Bender Copyright (C) 2016
 */
public class NceSensorManagerTest extends jmri.managers.AbstractSensorMgrTestBase {

    private NceInterfaceScaffold lnis = null;

    @Override
    public String getSystemName(int i) {
        return "NS" + i;
    }

    @Override
    protected int getNumToTest1() {
        return 32;
    }

    @Override
    protected int getNumToTest2() {
        return 45;
    }

    @Override
    protected String getASystemNameWithNoPrefix() {
        return "32";
    }
    
    @Test
    @Override
    public void testRegisterDuplicateSystemName() throws PropertyVetoException,
            NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        ProvidingManager<Sensor> m = l;
        String s1 = getSystemName(getNumToTest1());
        String s2 = getSystemName(getNumToTest2());
        testRegisterDuplicateSystemName(m, s1, s2);
    }

    
    @Test
    public void testNceSensorCreate() {
        Assert.assertNotNull("exists", l);
    }

    @Test
    public void testValidateSystemNameFormat() {
        Assert.assertEquals("NS4:3", l.validateSystemNameFormat("NS4:3"));
        Assert.assertEquals("NS50", l.validateSystemNameFormat("NS50"));
        try {
            l.validateSystemNameFormat("NS0");
            Assert.fail("Expected exception not thrown");
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals("\"NS0\" must use an AIU address from 1 to 63", ex.getMessage());
        }
        try {
            l.validateSystemNameFormat("NS0:0");
            Assert.fail("Expected exception not thrown");
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals("\"NS0:0\" must use an AIU address from 1 to 63", ex.getMessage());
        }
        try {
            l.validateSystemNameFormat("NS64:7");
            Assert.fail("Expected exception not thrown");
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals("\"NS64:7\" must use an AIU address from 1 to 63", ex.getMessage());
        }
        try {
            l.validateSystemNameFormat("NS2:15");
            Assert.fail("Expected exception not thrown");
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals("\"NS2:15\" must use an AIU pin from 1 to 14", ex.getMessage());
        }
        try {
            l.validateSystemNameFormat("NS14");
            Assert.fail("Expected exception not thrown");
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals("\"NS14\" must use an AIU address from 1 to 63", ex.getMessage());
        }
        try {
            l.validateSystemNameFormat("NS47");
            Assert.fail("Expected exception not thrown");
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals("\"NS47\" must use an AIU pin from 1 to 14", ex.getMessage());
        }
        try {
            l.validateSystemNameFormat("NS1006");
            Assert.fail("Expected exception not thrown");
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals("\"NS1006\" must use an AIU pin from 1 to 14", ex.getMessage());
        }
        try {
            l.validateSystemNameFormat("NS1008");
            Assert.fail("Expected exception not thrown");
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals("\"NS1008\" must use an AIU address from 1 to 63", ex.getMessage());
        }
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

        // prepare an interface
        lnis = new NceInterfaceScaffold();
        Assert.assertNotNull("exists", lnis);
        
        NceCmdStationMemory t = new NceCmdStationMemory();
        Assert.assertNotNull("exist", t);
        
        lnis.csm = t;

        // create and register the manager object
        l = new NceSensorManager(lnis.getAdapterMemo());
        jmri.InstanceManager.setSensorManager(l);
    }

    @AfterEach
    public void tearDown() {
        l.dispose();
        lnis.terminateThreads();
        lnis = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
