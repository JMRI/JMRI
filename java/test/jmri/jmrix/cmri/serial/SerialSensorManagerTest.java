package jmri.jmrix.cmri.serial;

import jmri.Manager.NameValidity;
import jmri.Sensor;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the SerialSensorManager class.
 *
 * @author	Bob Jacobsen Copyright 2003
 * @author	Paul Bender Copyright (C) 2016
 */
public class SerialSensorManagerTest extends jmri.managers.AbstractSensorMgrTestBase {

    private jmri.jmrix.cmri.CMRISystemConnectionMemo memo = null;
    private SerialTrafficControlScaffold stcs = null;
    private SerialNode n0 = null;
    private SerialNode n1 = null;
    private SerialNode n2 = null;

    @Override
    public String getSystemName(int i) {
        return "CS" + i;
    }

    @Test
    public void testSensorCreationAndRegistration() {

        Assert.assertTrue("none expected A0", !(n0.getSensorsActive()));
        Assert.assertTrue("none expected A1", !(n1.getSensorsActive()));
        Assert.assertTrue("none expected A2", !(n2.getSensorsActive()));

        Sensor sensor = l.provideSensor("3");
        Assert.assertNotNull("found sensor", sensor);
        Assert.assertTrue("right name", sensor.getSystemName().equals("CS3"));
        Assert.assertTrue("UA 0", n0.getSensorsActive());
        Assert.assertTrue("2nd none expected A1", !(n1.getSensorsActive()));
        Assert.assertTrue("2nd none expected A2", !(n2.getSensorsActive()));

        l.provideSensor("11");
        l.provideSensor("8");
        l.provideSensor("19");
        l.provideSensor("23");
        l.provideSensor("CS2048");
        Assert.assertTrue("2nd UA 0", n0.getSensorsActive());
        Assert.assertTrue("3rd none expected UA 1", !(n1.getSensorsActive()));
        Assert.assertTrue("UA 2", n2.getSensorsActive());

        l.provideSensor("15");
        l.provideSensor("1001");
        Assert.assertTrue("3rd UA 0", n0.getSensorsActive());
        Assert.assertTrue("UA 1", n1.getSensorsActive());
        Assert.assertTrue("2nd UA 2", n0.getSensorsActive());
        l.provideSensor("17");
        l.provideSensor("1017");
        l.provideSensor("2017");
        Assert.assertTrue("4th UA 0", n0.getSensorsActive());
        Assert.assertTrue("2nd UA 1", n1.getSensorsActive());
        Assert.assertTrue("3rd UA 2", n0.getSensorsActive());
    }

    @Test
    public void testValidSystemNameFormat() {
        Assert.assertEquals(NameValidity.VALID, l.validSystemNameFormat("CS1"));
        Assert.assertEquals(NameValidity.VALID, l.validSystemNameFormat("CS21"));
        Assert.assertEquals(NameValidity.VALID, l.validSystemNameFormat("CS2001"));
        Assert.assertEquals(NameValidity.VALID, l.validSystemNameFormat("CS21001"));

        Assert.assertEquals(NameValidity.INVALID, l.validSystemNameFormat("CSx"));
//        jmri.util.JUnitAppender.assertWarnMessage("invalid character in number field of CMRI system name: CSx");
        
        Assert.assertEquals(NameValidity.VALID_AS_PREFIX_ONLY, l.validSystemNameFormat("CS2000"));
//        jmri.util.JUnitAppender.assertWarnMessage("bit number not in range 1 - 999 in CMRI system name: CS2000");

        Assert.assertEquals(NameValidity.INVALID, l.validSystemNameFormat("CS"));
//        jmri.util.JUnitAppender.assertWarnMessage("invalid character in number field of CMRI system name: CS");
    }
    
    @Test
    public void testDefinitions() {
        Assert.assertEquals("Node definitions match", SerialSensorManager.SENSORSPERUA,
                SerialNode.MAXSENSORS + 1);
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();

        // replace the SerialTrafficController
        stcs = new SerialTrafficControlScaffold();
        memo = new jmri.jmrix.cmri.CMRISystemConnectionMemo();
        memo.setTrafficController(stcs);
        l = new SerialSensorManager(memo);

        n0 = new SerialNode(stcs);
        n1 = new SerialNode(1, SerialNode.SMINI, stcs);
        n2 = new SerialNode(2, SerialNode.USIC_SUSIC, stcs);
        n2.setNumBitsPerCard(24);
        n2.setCardTypeByAddress(0, SerialNode.INPUT_CARD);
        n2.setCardTypeByAddress(1, SerialNode.OUTPUT_CARD);
        n2.setCardTypeByAddress(3, SerialNode.OUTPUT_CARD);
        n2.setCardTypeByAddress(4, SerialNode.INPUT_CARD);
        n2.setCardTypeByAddress(2, SerialNode.OUTPUT_CARD);

    }

    @After
    public void tearDown() {
        l.dispose();
        if (stcs != null) stcs.terminateThreads();
        stcs = null;
        memo = null;
	    JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
