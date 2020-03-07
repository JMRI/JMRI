package jmri.jmrix.cmri.serial;

import java.util.Iterator;
import java.util.TreeSet;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import jmri.Turnout;
import jmri.implementation.AbstractTurnoutTestBase;
import jmri.util.JUnitUtil;
import jmri.util.NamedBeanComparator;

/**
 * Tests for the jmri.jmrix.cmri.serial.SerialTurnout class
 *
 * @author	Bob Jacobsen
 */
public class SerialTurnoutTest extends AbstractTurnoutTestBase {

    private jmri.jmrix.cmri.CMRISystemConnectionMemo memo = null;
    private SerialTrafficControlScaffold tcis = null;
    private SerialNode n = null;

    @Test
    public void testCtor() {
        new SerialTurnout("5", "to5", memo);
    }
    
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        
        // prepare an interface
        tcis = new SerialTrafficControlScaffold();
        memo = new jmri.jmrix.cmri.CMRISystemConnectionMemo();
        memo.setTrafficController(tcis);
        n = new SerialNode(0, SerialNode.SMINI,tcis);
        Assert.assertNotNull("node exists", n);
        startingNumListeners = tcis.numListeners();
        
        t = memo.getTurnoutManager().provideTurnout("4");
        Assert.assertNotNull("turnout exists", t);
    }

    @After
    public void tearDown() {
        if (tcis != null) tcis.terminateThreads();
        tcis = null;
        memo = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

    int startingNumListeners; // number at creation, before tests start allocating them.
    
    @Override
    public int numListeners() {
        return tcis.numListeners() - startingNumListeners;
    }

    @Override
    public void checkThrownMsgSent() {

//                tcis.sendSerialMessage(tcis.nextWrite(), null); // force outbound message; normally done by poll loop
//		Assert.assertTrue("message sent", tcis.outbound.size()>0);
//		Assert.assertEquals("content", "41 54 08", tcis.outbound.elementAt(tcis.outbound.size()-1).toString());  // THROWN message
    }

    @Override
    public void checkClosedMsgSent() {
//                tcis.sendSerialMessage(tcis.nextWrite(), null); // force outbound message; normally done by poll loop
//		Assert.assertTrue("message sent", tcis.outbound.size()>0);
//		Assert.assertEquals("content", "41 54 00", tcis.outbound.elementAt(tcis.outbound.size()-1).toString());  // CLOSED message
    }

    @Test
    public void testSystemSpecificComparisonOfStandardNames() {
        NamedBeanComparator<Turnout> t = new NamedBeanComparator<>();
        
        Turnout t1 = new SerialTurnout("CT1", "to1", memo);
        Turnout t2 = new SerialTurnout("CT2", "to2", memo);
        Turnout t10 = new SerialTurnout("CT10", "to10", memo);

        Assert.assertEquals("T1 == T1", 0, t.compare(t1, t1));

        Assert.assertEquals("T1 < T2", -1, t.compare(t1, t2));
        Assert.assertEquals("T2 > T1", +1, t.compare(t2, t1));

        Assert.assertEquals("T10 > T2", +1, t.compare(t10, t2));
        Assert.assertEquals("T2 < T10", -1, t.compare(t2, t10));    
    }

    @Test
    public void testSystemSpecificComparisonOfSpecificFormats() {
        // test by putting into a tree set, then extracting and checking order
        TreeSet<Turnout> set = new TreeSet<>(new NamedBeanComparator<>());
        
        set.add(new SerialTurnout("CT3B4",    "to3004", memo));
        set.add(new SerialTurnout("CT3003",    "to3003", memo));
        set.add(new SerialTurnout("CT3B2",    "to3002", memo));
        set.add(new SerialTurnout("CT3001",    "to3001", memo));

        set.add(new SerialTurnout("CT005",    "to1", memo));

        set.add(new SerialTurnout("CT1:5",    "to1005", memo));
        set.add(new SerialTurnout("CT01004",    "to1004", memo));
        set.add(new SerialTurnout("CT1003",    "to1003", memo));
        set.add(new SerialTurnout("CT1002",    "to1002", memo));
        set.add(new SerialTurnout("CT01001",    "to1001", memo));

        set.add(new SerialTurnout("CT2",    "to2", memo));
        set.add(new SerialTurnout("CT10",   "to10", memo));
        set.add(new SerialTurnout("CT1",    "to1", memo));
        
        
        Iterator<Turnout> it = set.iterator();
        
        Assert.assertEquals("CT1", it.next().getSystemName());
        Assert.assertEquals("CT2", it.next().getSystemName());
        Assert.assertEquals("CT005", it.next().getSystemName());
        Assert.assertEquals("CT10", it.next().getSystemName());

        Assert.assertEquals("CT01001", it.next().getSystemName());
        Assert.assertEquals("CT1002", it.next().getSystemName());
        Assert.assertEquals("CT1003", it.next().getSystemName());
        Assert.assertEquals("CT01004", it.next().getSystemName());
        Assert.assertEquals("CT1:5", it.next().getSystemName());

        Assert.assertEquals("CT3001", it.next().getSystemName());
        Assert.assertEquals("CT3B2", it.next().getSystemName());
        Assert.assertEquals("CT3003", it.next().getSystemName());
        Assert.assertEquals("CT3B4", it.next().getSystemName());
    }

}
