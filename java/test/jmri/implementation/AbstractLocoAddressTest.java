// AbstractLocoAddressTest.java

package jmri.implementation;

import org.apache.log4j.Logger;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Assert;

/**
 * Test simple functioning of AbstractLocoAddress 
 *
 * @author			Bob Jacobsen Copyright (C) 2005
 * @version			$Revision$
 */

public class AbstractLocoAddressTest extends TestCase {

	public void testValue1() {
        AbstractLocoAddress l = new AbstractLocoAddress(12, true);
		Assert.assertEquals("number ", l.getNumber(), 12);
		Assert.assertEquals("long/short ", l.isLongAddress(), true);
	}
        
	public void testValue2() {
        AbstractLocoAddress l = new AbstractLocoAddress(12, false);
		Assert.assertEquals("number ", l.getNumber(), 12);
		Assert.assertEquals("long/short ", l.isLongAddress(), false);
	}
        
	public void testValue3() {
        AbstractLocoAddress l = new AbstractLocoAddress(121, true);
		Assert.assertEquals("number ", l.getNumber(), 121);
		Assert.assertEquals("long/short ", l.isLongAddress(), true);
	}
        
	public void testCopy1() {
        AbstractLocoAddress l = new AbstractLocoAddress(new AbstractLocoAddress(121, true));
		Assert.assertEquals("number ", l.getNumber(), 121);
		Assert.assertEquals("long/short ", l.isLongAddress(), true);
	}
        
	public void testEquals1() {
        AbstractLocoAddress l1 = new AbstractLocoAddress(121, true);
        AbstractLocoAddress l2 = new AbstractLocoAddress(121, true);

		Assert.assertTrue("equate ", l1.equals(l2));

		Assert.assertTrue("reflexive 1 ", l1.equals(l1));
		Assert.assertTrue("reflexive 2 ", l2.equals(l2));
		
		Assert.assertTrue("null 1 ", !l1.equals(null));
		Assert.assertTrue("null 2 ", !l2.equals(null));
		Assert.assertTrue("transitive ", (l2.equals(l1)) == ((l1.equals(l2))));

	}
        
	public void testEquals2() {
        AbstractLocoAddress l1 = new AbstractLocoAddress(4321, true);
        AbstractLocoAddress l2 = new AbstractLocoAddress(121, true);

		Assert.assertTrue("equate ", !l1.equals(l2));

		Assert.assertTrue("reflexive 1 ", l1.equals(l1));
		Assert.assertTrue("reflexive 2 ", l2.equals(l2));
		
		Assert.assertTrue("null 1 ", !l1.equals(null));
		Assert.assertTrue("null 2 ", !l2.equals(null));
		Assert.assertTrue("transitive ", (l2.equals(l1)) == ((l1.equals(l2))));

	}
        
	public void testEquals3() {
        AbstractLocoAddress l1 = new AbstractLocoAddress(121, false);
        AbstractLocoAddress l2 = new AbstractLocoAddress(121, true);

		Assert.assertTrue("equate ", !l1.equals(l2));

		Assert.assertTrue("reflexive 1 ", l1.equals(l1));
		Assert.assertTrue("reflexive 2 ", l2.equals(l2));
		
		Assert.assertTrue("null 1 ", !l1.equals(null));
		Assert.assertTrue("null 2 ", !l2.equals(null));
		Assert.assertTrue("transitive ", (l2.equals(l1)) == ((l1.equals(l2))));

	}
        
	public void testEquals4() {
        AbstractLocoAddress l1 = new AbstractLocoAddress(4321, true);
        AbstractLocoAddress l2 = new AbstractLocoAddress(121, false);

		Assert.assertTrue("equate ", !l1.equals(l2));

		Assert.assertTrue("reflexive 1 ", l1.equals(l1));
		Assert.assertTrue("reflexive 2 ", l2.equals(l2));
		
		Assert.assertTrue("null 1 ", !l1.equals(null));
		Assert.assertTrue("null 2 ", !l2.equals(null));
		Assert.assertTrue("transitive ", (l2.equals(l1)) == ((l1.equals(l2))));

	}
        
	public void testHash0() {
        AbstractLocoAddress l1 = new AbstractLocoAddress(121, true);
        AbstractLocoAddress l2 = new AbstractLocoAddress(4321, false);

		Assert.assertTrue("equate self 1", l1.hashCode() == l1.hashCode());
		Assert.assertTrue("equate self 2", l2.hashCode() == l2.hashCode());
	}

	public void testHash1() {
        AbstractLocoAddress l1 = new AbstractLocoAddress(121, true);
        AbstractLocoAddress l2 = new AbstractLocoAddress(121, true);

		Assert.assertTrue("equate ", l1.hashCode() == l2.hashCode());
	}
        
	public void testHash2() {
        AbstractLocoAddress l1 = new AbstractLocoAddress(4321, true);
        AbstractLocoAddress l2 = new AbstractLocoAddress(121, true);

		Assert.assertTrue("equate ", l1.hashCode() != l2.hashCode());
	}
        
	public void testHash3() {
        AbstractLocoAddress l1 = new AbstractLocoAddress(4321, false);
        AbstractLocoAddress l2 = new AbstractLocoAddress(4321, true);

		Assert.assertTrue("equate ", l1.hashCode() != l2.hashCode());
	}
        
	public void testHash4() {
        AbstractLocoAddress l1 = new AbstractLocoAddress(4321, false);
        AbstractLocoAddress l2 = new AbstractLocoAddress(121, true);

		Assert.assertTrue("equate ", l1.hashCode() != l2.hashCode());
	}
        
	public void testHash5() {
        AbstractLocoAddress l1 = new AbstractLocoAddress(4321, true);
        AbstractLocoAddress l2 = new AbstractLocoAddress(4321, true);

		Assert.assertTrue("equate ", l1.hashCode() == l2.hashCode());
	}
        
	public void testHash6() {
        AbstractLocoAddress l1 = new AbstractLocoAddress(4321, false);
        AbstractLocoAddress l2 = new AbstractLocoAddress(4321, false);

		Assert.assertTrue("equate ", l1.hashCode() == l2.hashCode());
	}
        

	// from here down is testing infrastructure
	public AbstractLocoAddressTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {AbstractLocoAddressTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		apps.tests.AllTest.initLogging();
		TestSuite suite = new TestSuite(AbstractLocoAddressTest.class);
		return suite;
	}

	static Logger log = Logger.getLogger(AbstractLocoAddressTest.class.getName());

}
