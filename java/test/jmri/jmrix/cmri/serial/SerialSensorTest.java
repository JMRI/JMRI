package jmri.jmrix.cmri.serial;

import jmri.util.JUnitUtil;
import jmri.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class SerialSensorTest {

    private jmri.jmrix.cmri.CMRISystemConnectionMemo memo = null;
    private SerialTrafficControlScaffold tcis = null;

    @Test
    public void testCTor() {
        SerialSensor t = new SerialSensor("CS4");
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void test2ParamCTor() {
        SerialSensor t = new SerialSensor("CS4","Test");
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testSystemSpecificComparisonOfStandardNames() {
        jmri.util.NamedBeanComparator t = new jmri.util.NamedBeanComparator();
        
        Sensor t1 = new SerialSensor("CS1");
        Sensor t2 = new SerialSensor("CS2");
        Sensor t10 = new SerialSensor("CS10");

        Assert.assertEquals("S1 == S1", 0, t.compare(t1, t1));

        Assert.assertEquals("S1 < S2", -1, t.compare(t1, t2));
        Assert.assertEquals("S2 > S1", +1, t.compare(t2, t1));

        Assert.assertEquals("S10 > S2", +1, t.compare(t10, t2));
        Assert.assertEquals("S2 < S10", -1, t.compare(t2, t10));    
    }

    @Test
    public void testSystemSpecificComparisonOfSpecificFormats() {
        // test by putting into a tree set, then extracting and checking order
        java.util.TreeSet<Sensor> set = new java.util.TreeSet<>(new jmri.util.NamedBeanComparator());
        
        set.add(new SerialSensor("CS3B4"));
        set.add(new SerialSensor("CS3003"));
        set.add(new SerialSensor("CS3B2"));
        set.add(new SerialSensor("CS3001"));

        set.add(new SerialSensor("CS005"));

        set.add(new SerialSensor("CS1:5"));
        set.add(new SerialSensor("CS01004"));
        set.add(new SerialSensor("CS1003"));
        set.add(new SerialSensor("CS1002"));
        set.add(new SerialSensor("CS01001"));

        set.add(new SerialSensor("CS2"));
        set.add(new SerialSensor("CS10"));
        set.add(new SerialSensor("CS1"));
        
        
        java.util.Iterator<Sensor> it = set.iterator();
        
        Assert.assertEquals("CS1", it.next().getSystemName());
        Assert.assertEquals("CS2", it.next().getSystemName());
        Assert.assertEquals("CS005", it.next().getSystemName());
        Assert.assertEquals("CS10", it.next().getSystemName());

        Assert.assertEquals("CS01001", it.next().getSystemName());
        Assert.assertEquals("CS1002", it.next().getSystemName());
        Assert.assertEquals("CS1003", it.next().getSystemName());
        Assert.assertEquals("CS01004", it.next().getSystemName());
        Assert.assertEquals("CS1:5", it.next().getSystemName());

        Assert.assertEquals("CS3001", it.next().getSystemName());
        Assert.assertEquals("CS3B2", it.next().getSystemName());
        Assert.assertEquals("CS3003", it.next().getSystemName());
        Assert.assertEquals("CS3B4", it.next().getSystemName());
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
        JUnitUtil.tearDown();
    }

}
