// OrderedHashtableTest.java

package jmri.util;

import jmri.*;
import junit.framework.*;
import java.util.*;

/**
 * Tests for the jmri.util.OrderedHashtable class.
 * @author	Bob Jacobsen  Copyright 2008
 * @version	$Revision: 1.1 $
 */
public class OrderedHashtableTest extends TestCase {


    public void testCtor() {
        new OrderedHashtable();
    }

    public void testPut() {
        OrderedHashtable oht = new OrderedHashtable();
        
        Turnout t1 = new AbstractTurnout("t1"){
	        protected void forwardCommandChangeToLayout(int s) {}
	        protected void turnoutPushbuttonLockout(boolean locked) {}
        };
        Turnout t2 = new TestTurnout("t2");
        Turnout t3 = new TestTurnout("t3");
        
        // load
        oht.put("1", t1);
        oht.put("2", t2);
        oht.put("3", t3);
        
        // check order
        Enumeration en = oht.keys();
        String t;
        
        Assert.assertTrue(en.hasMoreElements());
        t = (String) en.nextElement();
        Assert.assertTrue(t != null);
        Assert.assertTrue(t.equals("1"));
        
        Assert.assertTrue(en.hasMoreElements());
        t = (String) en.nextElement();
        Assert.assertTrue(t != null);
        Assert.assertTrue(t.equals("2"));
        
        Assert.assertTrue(en.hasMoreElements());
        t = (String) en.nextElement();
        Assert.assertTrue(t != null);
        Assert.assertTrue(t.equals("3"));
        
        Assert.assertTrue(! en.hasMoreElements());
        
    }

    public void testRemove() {
        OrderedHashtable oht = new OrderedHashtable();
        
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
        Enumeration en = oht.keys();
        String t;
        
        Assert.assertTrue(en.hasMoreElements());
        t = (String) en.nextElement();
        Assert.assertTrue(t != null);
        Assert.assertTrue(t.equals("1"));
        
        Assert.assertTrue(en.hasMoreElements());
        t = (String) en.nextElement();
        Assert.assertTrue(t != null);
        Assert.assertTrue(t.equals("2"));
        
        Assert.assertTrue(en.hasMoreElements());
        t = (String) en.nextElement();
        Assert.assertTrue(t != null);
        Assert.assertTrue(t.equals("4"));
        
        Assert.assertTrue(! en.hasMoreElements());
        
    }

	// from here down is testing infrastructure

	public OrderedHashtableTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {OrderedHashtableTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(OrderedHashtableTest.class);
		return suite;
	}

    class TestTurnout extends AbstractTurnout {
        TestTurnout(String s) { super(s); }
	    protected void forwardCommandChangeToLayout(int s) {}
	    protected void turnoutPushbuttonLockout(boolean locked) {}
    }
    
	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(OrderedHashtableTest.class.getName());

}
