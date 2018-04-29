package jmri.jmrix.secsi;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the SerialSensorManager class.
 *
 * @author	Bob Jacobsen Copyright 2003, 2007
 * @author Paul Bender Copyright (C) 2016
 */
public class SerialSensorManagerTest extends jmri.managers.AbstractSensorMgrTestBase {

    private SerialTrafficControlScaffold tcis = null;
    private SecsiSystemConnectionMemo memo = null;

    private SerialNode n0 = null;
    private SerialNode n1 = null;
    private SerialNode n2 = null;

    @Override
    public String getSystemName(int i) {
        return "VS" + i;
    }

    @Test
    public void testSensorCreationAndRegistration() {
        Assert.assertTrue("none expected A0", !(n0.getSensorsActive()));
        Assert.assertTrue("none expected A1", !(n1.getSensorsActive()));
        Assert.assertTrue("none expected A2", !(n2.getSensorsActive()));
        l.provideSensor("3");
        Assert.assertTrue("UA 0", n0.getSensorsActive());
        Assert.assertTrue("2nd none expected A1", !(n1.getSensorsActive()));
        Assert.assertTrue("2nd none expected A2", !(n2.getSensorsActive()));
        l.provideSensor("11");
        l.provideSensor("8");
        l.provideSensor("9");
        l.provideSensor("13");
        l.provideSensor("VS2006");
        Assert.assertTrue("2nd UA 0", n0.getSensorsActive());
        Assert.assertTrue("3rd none expected UA 1", !(n1.getSensorsActive()));
        Assert.assertTrue("UA 2", n2.getSensorsActive());
        l.provideSensor("15");
        l.provideSensor("1001");
        Assert.assertTrue("3rd UA 0", n0.getSensorsActive());
        Assert.assertTrue("UA 1", n1.getSensorsActive());
        Assert.assertTrue("2nd UA 2", n0.getSensorsActive());
        l.provideSensor("7");
        l.provideSensor("1007");
        l.provideSensor("2007");
        Assert.assertTrue("4th UA 0", n0.getSensorsActive());
        Assert.assertTrue("2nd UA 1", n1.getSensorsActive());
        Assert.assertTrue("3rd UA 2", n0.getSensorsActive());
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        tcis = new SerialTrafficControlScaffold();
        memo = new SecsiSystemConnectionMemo();
        memo.setTrafficController(tcis);

        // construct nodes
        n0 = new SerialNode(0, SerialNode.DAUGHTER,tcis);
        n1 = new SerialNode(1, SerialNode.DAUGHTER,tcis);
        n2 = new SerialNode(2, SerialNode.CABDRIVER,tcis);

        l = new SerialSensorManager(memo);
    }

    @After
    public void tearDown() {
        l.dispose();
        JUnitUtil.tearDown();
    }

}
