package jmri.jmrix.grapevine;

import jmri.util.JUnitUtil;

import java.beans.PropertyVetoException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the SerialSensorManager class.
 *
 * @author	Bob Jacobsen Copyright 2003, 2007, 2008
 * @author Paul Bender Copyright (C) 2016
 */
public class SerialSensorManagerTest extends jmri.managers.AbstractSensorMgrTestBase {

    private GrapevineSystemConnectionMemo memo = null; 
    private SerialNode n1 = null;
    private SerialNode n2 = null;
    private SerialNode n3 = null;

    @Override
    public String getSystemName(int i) {
        return "GS" + i;
    }

    @Test
    public void testSensorCreationAndRegistration() {
        Assert.assertTrue("none expected A1", !(n1.getSensorsActive()));
        Assert.assertTrue("none expected A2", !(n2.getSensorsActive()));
        Assert.assertTrue("none expected A3", !(n3.getSensorsActive()));
        l.provideSensor("1003");
        Assert.assertTrue("UA 1", n1.getSensorsActive());
        Assert.assertTrue("2nd none expected A2", !(n2.getSensorsActive()));
        Assert.assertTrue("2nd none expected A3", !(n3.getSensorsActive()));
        l.provideSensor("1011");
        l.provideSensor("1008");
        l.provideSensor("1009");
        l.provideSensor("1011");
        l.provideSensor("GS2006");
        Assert.assertTrue("2nd UA 1", n1.getSensorsActive());
        Assert.assertTrue("2nd UA 2", n2.getSensorsActive());
        Assert.assertTrue("2nd none expected UA 3", !(n3.getSensorsActive()));
        l.provideSensor("1010");
        l.provideSensor("3001");
        Assert.assertTrue("3rd UA 1", n1.getSensorsActive());
        Assert.assertTrue("3rd UA 2", n2.getSensorsActive());
        Assert.assertTrue("3nd UA 3", n3.getSensorsActive());
        l.provideSensor("1007");
        l.provideSensor("2007");
        l.provideSensor("3007");
        Assert.assertTrue("4th UA 1", n1.getSensorsActive());
        Assert.assertTrue("4th UA 2", n2.getSensorsActive());
        Assert.assertTrue("4th UA 3", n3.getSensorsActive());

        // some equality tests
        // TODO: should these be the same bean, or should different beans point to same physical device?
        Assert.assertNotEquals("GS1p7 == GS1007", l.getSensor("GS1p7"), l.getSensor("GS1007"));
        Assert.assertNotEquals("GS1B7 == GS1007", l.getSensor("GS1B7"), l.getSensor("GS1007"));
    }

    @Override
    @Test
    public void testRegisterDuplicateSystemName() throws PropertyVetoException, NoSuchFieldException,
            NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        testRegisterDuplicateSystemName(l,
                l.makeSystemName("1007"),
                l.makeSystemName("1009"));
    }

    @Override
    @Test
    public void testMakeSystemName() {
        String s = l.makeSystemName("1007");
        Assert.assertNotNull(s);
        Assert.assertFalse(s.isEmpty());
    }

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        memo = new GrapevineSystemConnectionMemo();
        SerialTrafficController t = new SerialTrafficControlScaffold(memo);
        memo.setTrafficController(t);
        Assert.assertNotNull("exists", t);

        // construct nodes
        n1 = new SerialNode(1, SerialNode.NODE2002V6, t);
        n2 = new SerialNode(2, SerialNode.NODE2002V6, t);
        n3 = new SerialNode(3, SerialNode.NODE2002V1, t);

        l = new SerialSensorManager(memo);
    }

    // The minimal setup for log4J
    @After
    public void tearDown() {
        l.dispose();
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

    @Override
    /**
     * Number of sensor to test. Made a separate method so it can be overridden
     * in subclasses that do or don't support various numbers
     */
    protected int getNumToTest1() {
        return 1009;
    }

    @Override
    protected int getNumToTest2() {
        return 1007;
    }

}
