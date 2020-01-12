package jmri.jmrix.cmri.serial;

import java.util.Iterator;
import java.util.TreeSet;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import jmri.Light;
import jmri.util.JUnitUtil;
import jmri.util.NamedBeanComparator;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class SerialLightTest {

    private jmri.jmrix.cmri.CMRISystemConnectionMemo memo = null;
    private SerialTrafficControlScaffold tcis = null;

    @Test
    public void test2ParamCTor() {
        SerialLight t = new SerialLight("CL4",memo);
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void test3ParamCTor() {
        SerialLight t = new SerialLight("CL4","t4",memo);
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testSystemSpecificComparisonOfStandardNames() {
        NamedBeanComparator<Light> t = new NamedBeanComparator<>();
        
        Light t1 = new SerialLight("CL1", "to1", memo);
        Light t2 = new SerialLight("CL2", "to2", memo);
        Light t10 = new SerialLight("CL10", "to10", memo);

        Assert.assertEquals("L1 == L1", 0, t.compare(t1, t1));

        Assert.assertEquals("L1 < L2", -1, t.compare(t1, t2));
        Assert.assertEquals("L2 > L1", +1, t.compare(t2, t1));

        Assert.assertEquals("L10 > L2", +1, t.compare(t10, t2));
        Assert.assertEquals("L2 < L10", -1, t.compare(t2, t10));    
    }

    @Test
    public void testSystemSpecificComparisonOfSpecificFormats() {
        // test by putting into a tree set, then extracting and checking order
        TreeSet<Light> set = new TreeSet<>(new NamedBeanComparator<>());
        
        set.add(new SerialLight("CL3B4",    "to3004", memo));
        set.add(new SerialLight("CL3003",    "to3003", memo));
        set.add(new SerialLight("CL3B2",    "to3002", memo));
        set.add(new SerialLight("CL3001",    "to3001", memo));

        set.add(new SerialLight("CL005",    "to1", memo));

        // Lights don't do : notation
        set.add(new SerialLight("CL01004",    "to1004", memo));
        set.add(new SerialLight("CL1003",    "to1003", memo));
        set.add(new SerialLight("CL1002",    "to1002", memo));
        set.add(new SerialLight("CL01001",    "to1001", memo));

        set.add(new SerialLight("CL2",    "to2", memo));
        set.add(new SerialLight("CL10",   "to10", memo));
        set.add(new SerialLight("CL1",    "to1", memo));
        
        
        Iterator<Light> it = set.iterator();
        
        Assert.assertEquals("CL1", it.next().getSystemName());
        Assert.assertEquals("CL2", it.next().getSystemName());
        Assert.assertEquals("CL005", it.next().getSystemName());
        Assert.assertEquals("CL10", it.next().getSystemName());

        Assert.assertEquals("CL01001", it.next().getSystemName());
        Assert.assertEquals("CL1002", it.next().getSystemName());
        Assert.assertEquals("CL1003", it.next().getSystemName());
        Assert.assertEquals("CL01004", it.next().getSystemName());

        Assert.assertEquals("CL3001", it.next().getSystemName());
        Assert.assertEquals("CL3B2", it.next().getSystemName());
        Assert.assertEquals("CL3003", it.next().getSystemName());
        Assert.assertEquals("CL3B4", it.next().getSystemName());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        // prepare an interface
        tcis = new SerialTrafficControlScaffold();
        memo = new jmri.jmrix.cmri.CMRISystemConnectionMemo();
        memo.setTrafficController(tcis);
        new SerialNode(0, SerialNode.SMINI,tcis);
    }

    @After
    public void tearDown() {
        if (tcis != null) tcis.terminateThreads();
        tcis = null;
        memo = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
