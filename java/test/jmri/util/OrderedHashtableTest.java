// OrderedHashtableTest.java
package jmri.util;

import java.util.Enumeration;
import jmri.Turnout;
import jmri.implementation.AbstractTurnout;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.util.OrderedHashtable class.
 *
 * @author	Bob Jacobsen Copyright 2008
 * @version	$Revision$
 */
public class OrderedHashtableTest extends TestCase {

    public void testCtor() {
        new OrderedHashtable<Object, Object>();
        new OrderedHashtable<String, Object>();
    }

    public void testPut() {
        OrderedHashtable<String, Turnout> oht = new OrderedHashtable<String, Turnout>();

        Turnout t1 = new AbstractTurnout("t1") {
            /**
             *
             */
            private static final long serialVersionUID = 2727946283337403138L;

            protected void forwardCommandChangeToLayout(int s) {
            }

            protected void turnoutPushbuttonLockout(boolean locked) {
            }
        };
        Turnout t2 = new TestTurnout("t2");
        Turnout t3 = new TestTurnout("t3");

        // load
        oht.put("1", t1);
        oht.put("2", t2);
        oht.put("3", t3);

        // check order
        Enumeration<String> en = oht.keys();
        String t;

        Assert.assertTrue(en.hasMoreElements());
        t = en.nextElement();
        Assert.assertNotNull(t);
        Assert.assertTrue(t.equals("1"));

        Assert.assertTrue(en.hasMoreElements());
        t = en.nextElement();
        Assert.assertNotNull(t);
        Assert.assertTrue(t.equals("2"));

        Assert.assertTrue(en.hasMoreElements());
        t = en.nextElement();
        Assert.assertNotNull(t);
        Assert.assertTrue(t.equals("3"));

        Assert.assertTrue(!en.hasMoreElements());

    }

    public void testRemove() {
        OrderedHashtable<String, Turnout> oht = new OrderedHashtable<String, Turnout>();

        Turnout t1 = new TestTurnout("t1");
        Turnout t2 = new TestTurnout("t2");
        Turnout t3 = new TestTurnout("t3");
        Turnout t4 = new TestTurnout("t4");

        // load
        oht.put("1", t1);
        oht.put("2", t2);
        oht.put("3", t3);
        oht.put("4", t4);

        // remove
        oht.remove("3");

        // check order
        Enumeration<String> en = oht.keys();
        String t;

        Assert.assertTrue(en.hasMoreElements());
        t = en.nextElement();
        Assert.assertNotNull(t);
        Assert.assertTrue(t.equals("1"));

        Assert.assertTrue(en.hasMoreElements());
        t = en.nextElement();
        Assert.assertNotNull(t);
        Assert.assertTrue(t.equals("2"));

        Assert.assertTrue(en.hasMoreElements());
        t = en.nextElement();
        Assert.assertNotNull(t);
        Assert.assertTrue(t.equals("4"));

        Assert.assertTrue(!en.hasMoreElements());

    }

    public void testEquals() {
        OrderedHashtable<String, Turnout> oht1 = new OrderedHashtable<String, Turnout>();
        OrderedHashtable<String, Turnout> oht2 = new OrderedHashtable<String, Turnout>();

        Turnout t1 = new TestTurnout("t1");
        Turnout t2 = new TestTurnout("t2");
        Turnout t3 = new TestTurnout("t3");
        Turnout t4 = new TestTurnout("t4");
        Turnout t5 = new TestTurnout("t5");

        oht1.put("1", t1);
        oht2.put("1", t1);
        oht1.put("2", t2);
        oht2.put("2", t2);
        Assert.assertTrue("initial content", oht1.equals(oht2));

        oht1.put("3", t3);
        Assert.assertFalse("after add to one", oht1.equals(oht2));
        oht2.put("3", t3);
        Assert.assertTrue("after add to both", oht1.equals(oht2));

        oht1.put("4", t4);
        oht1.put("5", t5);
        oht2.put("5", t5);
        oht2.put("4", t4);
        Assert.assertFalse("check order matters", oht1.equals(oht2));

    }

    public void testClone() {
        OrderedHashtable<String, Turnout> oht1 = new OrderedHashtable<String, Turnout>();

        Turnout t1 = new TestTurnout("t1");
        Turnout t2 = new TestTurnout("t2");
        Turnout t4 = new TestTurnout("t4");

        oht1.put("1", t1);
        oht1.put("2", t2);
        
        @SuppressWarnings("unchecked")
        OrderedHashtable<String, Turnout> oht2 = (OrderedHashtable<String, Turnout>)oht1.clone();
        
        Assert.assertTrue("content equals", oht1.equals(oht2));
        Assert.assertFalse("different object", oht1 == oht2);
        
        oht1.put("4", t4);
        Assert.assertFalse("content no longer equals", oht1.equals(oht2));
    }
    
    // from here down is testing infrastructure
    public OrderedHashtableTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", OrderedHashtableTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(OrderedHashtableTest.class);
        return suite;
    }

    class TestTurnout extends AbstractTurnout {

        /**
         *
         */
        private static final long serialVersionUID = -4925238066324681487L;

        TestTurnout(String s) {
            super(s);
        }

        protected void forwardCommandChangeToLayout(int s) {
        }

        protected void turnoutPushbuttonLockout(boolean locked) {
        }
    }

}
