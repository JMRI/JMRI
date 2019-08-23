package jmri.util;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import jmri.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class NamedBeanComparatorTest {

    @Test
    public void testOneLetterCases() {
        NamedBeanComparator<Turnout> t = new NamedBeanComparator<>();

        Turnout it1 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT1");
        Turnout it10 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT10");
        Turnout it2 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT2");
        
        Assert.assertEquals("IT1 == IT1", 0, t.compare(it1, it1));

        Assert.assertEquals("IT1 < IT2", -1, t.compare(it1, it2));
        Assert.assertEquals("IT2 > IT1", +1, t.compare(it2, it1));

        Assert.assertEquals("IT10 > IT2", +1, t.compare(it10, it2));
        Assert.assertEquals("IT2 < IT10", -1, t.compare(it2, it10));
    }

    @Test
    public void testTwoLetterCases() {
        NamedBeanComparator<Turnout> t = new NamedBeanComparator<>();

        Turnout i2t1 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("I2T1");
        Turnout i2t10 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("I2T10");
        Turnout i2t2 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("I2T2");
        
        Assert.assertEquals("I2T1 == I2T1", 0, t.compare(i2t1, i2t1));

        Assert.assertEquals("I2T1 < I2T2", -1, t.compare(i2t1, i2t2));
        Assert.assertEquals("I2T2 > I2T1", +1, t.compare(i2t2, i2t1));

        Assert.assertEquals("I2T10 > I2T2", +1, t.compare(i2t10, i2t2));
        Assert.assertEquals("I2T2 < I2T10", -1, t.compare(i2t2, i2t10));
    }

    @Test
    public void testThreeLetterCases() {
        NamedBeanComparator<Turnout> t = new NamedBeanComparator<>();

        Turnout i23t1 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("I23T1");
        Turnout i23t10 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("I23T10");
        Turnout i23t2 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("I23T2");
        
        Assert.assertEquals("I23T1 == I23T1", 0, t.compare(i23t1, i23t1));

        Assert.assertEquals("I23T1 < I23T2", -1, t.compare(i23t1, i23t2));
        Assert.assertEquals("I23T2 > I23T1", +1, t.compare(i23t2, i23t1));

        Assert.assertEquals("I23T10 > I23T2", +1, t.compare(i23t10, i23t2));
        Assert.assertEquals("I23T2 < I23T10", -1, t.compare(i23t2, i23t10));
    }

    boolean hit = false;
    
    @Test
    public void testSystemSpecificCase() {
        NamedBeanComparator<Turnout> t = new NamedBeanComparator<>();

        // this just checks that the local sort is called
        Turnout it1 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT1");
        Turnout it2 = new jmri.implementation.AbstractTurnout("IT2") {

            @Override
            protected void forwardCommandChangeToLayout(int s) {
            }

            @Override
            protected void turnoutPushbuttonLockout(boolean b) {
            }

            @Override
            public int compareSystemNameSuffix(String suffix1, String suffix2, jmri.NamedBean n) {
                hit = true;
                return super.compareSystemNameSuffix(suffix1, suffix2, n);
            }
        };

        hit = false;
        Assert.assertEquals("IT1 < IT2", -1, t.compare(it1, it2));
        Assert.assertFalse(hit);
        
        hit = false;        
        Assert.assertEquals("IT2 < IT1", +1, t.compare(it2, it1));
        Assert.assertTrue(hit);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(NamedBeanComparatorTest.class);

}
