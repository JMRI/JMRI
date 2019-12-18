package jmri.jmrix.acela;

import jmri.InstanceManager;
import jmri.Light;
import jmri.Sensor;
import jmri.Turnout;
import jmri.jmrix.AbstractMRMessage;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * JUnit tests for the AcelaNode class
 *
 * @author Bob Coleman Copyright 2008
 */
public class AcelaNodeTest {

    private AcelaTrafficControlScaffold tcis = null;

    @Test
    public void testInitialization1() {
        Assert.assertEquals("StartingSensorAddress TB", 0, a1.getStartingSensorAddress());
        Assert.assertEquals("EndingSensorAddress TB", 3, a1.getEndingSensorAddress());
        Assert.assertEquals("StartingSensorAddress D8", -1, a2.getStartingSensorAddress());
        Assert.assertEquals("EndingSensorAddress D8", -1, a2.getEndingSensorAddress());
        Assert.assertEquals("StartingSensorAddress SY", 4, a3.getStartingSensorAddress());
        Assert.assertEquals("EndingSensorAddress SY", 19, a3.getEndingSensorAddress());
    }

    @Test
    public void testInitialization2() {
        Assert.assertEquals("StartingOutputAddress TB", 0, a1.getStartingOutputAddress());
        Assert.assertEquals("EndingOutputAddress TB", 3, a1.getEndingOutputAddress());
        Assert.assertEquals("StartingOutputAddress D8", 4, a2.getStartingOutputAddress());
        Assert.assertEquals("EndingOutputAddress D8", 11, a2.getEndingOutputAddress());
        Assert.assertEquals("StartingOutputAddress SY", -1, a3.getStartingOutputAddress());
        Assert.assertEquals("EndingOutputAddress SY", -1, a3.getEndingOutputAddress());
    }

    @Test
    public void testOutputBits1() {
        Assert.assertTrue("must Send", a1.mustSend());
        a1.resetMustSend();
        Assert.assertTrue("must Send off", !(a1.mustSend()));
        a1.setOutputBit(0, false);
        a1.setOutputBit(1, true);
        a1.setOutputBit(2, true);
        a1.setOutputBit(3, false);

        Assert.assertEquals("Out StartingOutputAddress D8", 4, a2.getStartingOutputAddress());
        Assert.assertEquals("Out EndingOutputAddress D8", 11, a2.getEndingOutputAddress());

        a2.setOutputBit(4, true);
        a2.setOutputBit(5, false);
        a2.setOutputBit(6, false);
        a2.setOutputBit(7, true);
        a2.setOutputBit(8, true);
        a2.setOutputBit(9, false);
        a2.setOutputBit(10, false);
        a2.setOutputBit(11, true);

        Assert.assertTrue("must Send on", a1.mustSend());

        AbstractMRMessage m1 = a1.createOutPacket();
        Assert.assertEquals("m1 packet size", 4, m1.getNumDataElements());
        Assert.assertEquals("m1 command", 7, m1.getElement(0) & 0xff);
        Assert.assertEquals("m1 address high", 0, m1.getElement(1) & 0xff); // 'T'        
        Assert.assertEquals("m1 address low", 0, (m1.getElement(2) & 0xff));
        Assert.assertEquals("m1 value", 6, (m1.getElement(3) & 0xff));

        AbstractMRMessage m2 = a2.createOutPacket();
        Assert.assertEquals("m2 packet size", 4, m2.getNumDataElements());
        Assert.assertEquals("m2 command", 8, m2.getElement(0) & 0xff);
        Assert.assertEquals("m2 address high", 0, m2.getElement(1) & 0xff); // 'T'        
        Assert.assertEquals("m2 address low", 4, (m2.getElement(2) & 0xff));
        Assert.assertEquals("m2 value", 153, (m2.getElement(3) & 0xff));
    }

    @Test
    @Ignore("Disabled in JUnit 3")
    public void testMarkChangesInitial() {
        /* TODO: what kind of sensor does the Acela support,
                or are these tests copied from somewhere else that should be removed?
        SerialSensor s1 = new SerialSensor("CS1", "a");
        Assert.assertEquals("check bit number", 1, SerialAddress.getBitFromSystemName("CS1"));
        SerialSensor s2 = new SerialSensor("CS2", "ab");
        SerialSensor s3 = new SerialSensor("CS3", "abc");
        b.registerSensor(s1, 0);
        b.registerSensor(s2, 1);
        b.registerSensor(s3, 2);
        Assert.assertTrue("check sensors active", b.getSensorsActive());
        // from UNKNOWN, 1st poll goes to new state
        SerialReply r = new SerialReply();
        r.setElement(2, '2');
        b.markChanges(r);
        Assert.assertEquals("check s1", Sensor.INACTIVE, s1.getKnownState());
        Assert.assertEquals("check s2", Sensor.ACTIVE, s2.getKnownState());
        Assert.assertEquals("check s3", Sensor.INACTIVE, s3.getKnownState());
        */
    }

    @Test
    @Ignore("Disabled in JUnit 3")
    public void testMarkChangesDebounce() {
        /* TODO: what kind of sensor does the Acela support,
                or are these tests copied from somewhere else that should be removed?
        SerialSensor s1 = new SerialSensor("CS1", "a");
        SerialSensor s2 = new SerialSensor("CS2", "ab");
        SerialSensor s3 = new SerialSensor("CS3", "abc");
        SerialSensor s4 = new SerialSensor("CS4", "abcd");
        b.registerSensor(s1, 0);
        b.registerSensor(s2, 1);
        b.registerSensor(s3, 2);
        b.registerSensor(s4, 3);
        // from UNKNOWN, 1st poll goes to new state
        SerialReply r;
        r = new SerialReply();
        r.setElement(2, '5');
        b.markChanges(r);
        Assert.assertEquals("poll0 s1", Sensor.ACTIVE, s1.getKnownState());
        Assert.assertEquals("poll0 s2", Sensor.INACTIVE, s2.getKnownState());
        Assert.assertEquals("poll0 s3", Sensor.ACTIVE, s3.getKnownState());
        Assert.assertEquals("poll0 s4", Sensor.INACTIVE, s4.getKnownState());
        // stabilize startup
        b.markChanges(r);
        b.markChanges(r);
        b.markChanges(r);
        Assert.assertEquals("poll1 s1", Sensor.ACTIVE, s1.getKnownState());
        Assert.assertEquals("poll1 s2", Sensor.INACTIVE, s2.getKnownState());
        Assert.assertEquals("poll1 s3", Sensor.ACTIVE, s3.getKnownState());
        Assert.assertEquals("poll1 s4", Sensor.INACTIVE, s4.getKnownState());
        // single poll shouldn't change
        r = new SerialReply();
        r.setElement(2, '0' + 10);
        b.markChanges(r);
        Assert.assertEquals("poll2 s1", Sensor.ACTIVE, s1.getKnownState());
        Assert.assertEquals("poll2 s2", Sensor.INACTIVE, s2.getKnownState());
        Assert.assertEquals("poll2 s3", Sensor.ACTIVE, s3.getKnownState());
        Assert.assertEquals("poll2 s4", Sensor.INACTIVE, s4.getKnownState());
        // 2nd poll should, but only if same
        r = new SerialReply();
        r.setElement(2, '6');
        b.markChanges(r);
        Assert.assertEquals("poll3 s1", Sensor.INACTIVE, s1.getKnownState());
        Assert.assertEquals("poll3 s2", Sensor.ACTIVE, s2.getKnownState());
        Assert.assertEquals("poll3 s3", Sensor.ACTIVE, s3.getKnownState());
        Assert.assertEquals("poll3 s4", Sensor.INACTIVE, s4.getKnownState());
        // 3rd poll changes last two
        r = new SerialReply();
        r.setElement(2, '5');
        b.markChanges(r);
        Assert.assertEquals("poll4 s1", Sensor.INACTIVE, s1.getKnownState());
        Assert.assertEquals("poll4 s2", Sensor.ACTIVE, s2.getKnownState());
        Assert.assertEquals("poll4 s3", Sensor.ACTIVE, s3.getKnownState());
        Assert.assertEquals("poll4 s4", Sensor.INACTIVE, s4.getKnownState());
        */
    }

    AcelaNode a0, a1, a2, a3;

    Turnout t1, t2, t3;
    Sensor s1, s2, s3, s4, s5;
    Light l1, l2, l3;

    @Before
    public void setUp() {
        JUnitUtil.setUp();

        tcis = new AcelaTrafficControlScaffold();

        // We need to delete the nodes so we can re-allocate them
        // otherwise we get another set of nodes for each test case
        // which really messes up the addresses.
        // We also seem to need to explicitly init each node.
        if (tcis.getNumNodes() > 0) {
            //    tcis.deleteNode(3);
            //    tcis.deleteNode(2);
            //    tcis.deleteNode(1);
            //    tcis.deleteNode(0);
            tcis.resetStartingAddresses();
        }
        if (tcis.getNumNodes() > 0) {
            Assert.fail("didn't clear nodes, found "+tcis.getNumNodes());
        }
        
        a0 = new AcelaNode(0, AcelaNode.AC, tcis);
        a0.initNode();
        a1 = new AcelaNode(1, AcelaNode.TB, tcis);
        a1.initNode();
        a2 = new AcelaNode(2, AcelaNode.D8, tcis);
        a2.initNode();
        a3 = new AcelaNode(3, AcelaNode.SY, tcis);
        a3.initNode();

        jmri.util.JUnitUtil.resetInstanceManager();

        t1 = InstanceManager.turnoutManagerInstance().newTurnout("IT99", "99");
        s1 = InstanceManager.sensorManagerInstance().newSensor("IS98", "98");

    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
