package jmri.jmrix.oaktree;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the SerialSensorManager class.
 *
 * @author	Bob Jacobsen Copyright 2003
 */
public class SerialSensorManagerTest extends jmri.managers.AbstractSensorMgrTestBase {

    private OakTreeSystemConnectionMemo memo = null;
    private SerialNode n0 = null;
    private SerialNode n1 = null;
    private SerialNode n2 = null;

    @Override
    public String getSystemName(int i) {
        return "OS" + i;
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
        l.provideSensor("OS2006");
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
        // replace the SerialTrafficController to get clean reset
        memo = new OakTreeSystemConnectionMemo("O", "Oak Tree");
        SerialTrafficController t = new SerialTrafficController(memo) {
            SerialTrafficController test() {
                return this;
            }
        }.test();
        memo.setTrafficController(t);
        Assert.assertNotNull("exists", t);

        // construct nodes
        n0 = new SerialNode(0, SerialNode.IO48,memo);
        n1 = new SerialNode(1, SerialNode.IO48,memo);
        n2 = new SerialNode(2, SerialNode.IO24,memo);

        l = new SerialSensorManager();
    }

    @After
    public void tearDown() {
        l.dispose();
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}
