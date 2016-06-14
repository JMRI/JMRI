package jmri;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test simple functioning of DccLocoAddress
 *
 * @author	Bob Jacobsen Copyright (C) 2005
 * @version	$Revision$
 */
public class DccLocoAddressTest extends TestCase {

    public void testValue1() {
        DccLocoAddress l = new DccLocoAddress(12, true);
        Assert.assertEquals("number ", l.getNumber(), 12);
        Assert.assertEquals("long/short ", l.isLongAddress(), true);
    }

    public void testValue2() {
        DccLocoAddress l = new DccLocoAddress(12, false);
        Assert.assertEquals("number ", l.getNumber(), 12);
        Assert.assertEquals("long/short ", l.isLongAddress(), false);
    }

    public void testValue3() {
        DccLocoAddress l = new DccLocoAddress(121, true);
        Assert.assertEquals("number ", l.getNumber(), 121);
        Assert.assertEquals("long/short ", l.isLongAddress(), true);
    }

    public void testCopy1() {
        DccLocoAddress l = new DccLocoAddress(new DccLocoAddress(121, true));
        Assert.assertEquals("number ", l.getNumber(), 121);
        Assert.assertEquals("long/short ", l.isLongAddress(), true);
    }

    public void testEquals1() {
        DccLocoAddress l1 = new DccLocoAddress(121, true);
        DccLocoAddress l2 = new DccLocoAddress(121, true);

        Assert.assertTrue("equate ", l1.equals(l2));

        Assert.assertTrue("reflexive 1 ", l1.equals(l1));
        Assert.assertTrue("reflexive 2 ", l2.equals(l2));

        Assert.assertTrue("null 1 ", !l1.equals(null));
        Assert.assertTrue("null 2 ", !l2.equals(null));
        Assert.assertTrue("transitive ", (l2.equals(l1)) == ((l1.equals(l2))));

    }

    public void testEquals2() {
        DccLocoAddress l1 = new DccLocoAddress(4321, true);
        DccLocoAddress l2 = new DccLocoAddress(121, true);

        Assert.assertTrue("equate ", !l1.equals(l2));

        Assert.assertTrue("reflexive 1 ", l1.equals(l1));
        Assert.assertTrue("reflexive 2 ", l2.equals(l2));

        Assert.assertTrue("null 1 ", !l1.equals(null));
        Assert.assertTrue("null 2 ", !l2.equals(null));
        Assert.assertTrue("transitive ", (l2.equals(l1)) == ((l1.equals(l2))));

    }

    public void testEquals3() {
        DccLocoAddress l1 = new DccLocoAddress(121, false);
        DccLocoAddress l2 = new DccLocoAddress(121, true);

        Assert.assertTrue("equate ", !l1.equals(l2));

        Assert.assertTrue("reflexive 1 ", l1.equals(l1));
        Assert.assertTrue("reflexive 2 ", l2.equals(l2));

        Assert.assertTrue("null 1 ", !l1.equals(null));
        Assert.assertTrue("null 2 ", !l2.equals(null));
        Assert.assertTrue("transitive ", (l2.equals(l1)) == ((l1.equals(l2))));

    }

    public void testEquals4() {
        DccLocoAddress l1 = new DccLocoAddress(4321, true);
        DccLocoAddress l2 = new DccLocoAddress(121, false);

        Assert.assertTrue("equate ", !l1.equals(l2));

        Assert.assertTrue("reflexive 1 ", l1.equals(l1));
        Assert.assertTrue("reflexive 2 ", l2.equals(l2));

        Assert.assertTrue("null 1 ", !l1.equals(null));
        Assert.assertTrue("null 2 ", !l2.equals(null));
        Assert.assertTrue("transitive ", (l2.equals(l1)) == ((l1.equals(l2))));

    }

    public void testHash0() {
        DccLocoAddress l1 = new DccLocoAddress(121, true);
        DccLocoAddress l2 = new DccLocoAddress(4321, false);

        Assert.assertTrue("equate self 1", l1.hashCode() == l1.hashCode());
        Assert.assertTrue("equate self 2", l2.hashCode() == l2.hashCode());
    }

    public void testHash1() {
        DccLocoAddress l1 = new DccLocoAddress(121, true);
        DccLocoAddress l2 = new DccLocoAddress(121, true);

        Assert.assertTrue("equate ", l1.hashCode() == l2.hashCode());
    }

    public void testHash2() {
        DccLocoAddress l1 = new DccLocoAddress(4321, true);
        DccLocoAddress l2 = new DccLocoAddress(121, true);

        Assert.assertTrue("equate ", l1.hashCode() != l2.hashCode());
    }

    public void testHash3() {
        DccLocoAddress l1 = new DccLocoAddress(4321, false);
        DccLocoAddress l2 = new DccLocoAddress(4321, true);

        Assert.assertTrue("equate ", l1.hashCode() != l2.hashCode());
    }

    public void testHash4() {
        DccLocoAddress l1 = new DccLocoAddress(4321, false);
        DccLocoAddress l2 = new DccLocoAddress(121, true);

        Assert.assertTrue("equate ", l1.hashCode() != l2.hashCode());
    }

    public void testHash5() {
        DccLocoAddress l1 = new DccLocoAddress(4321, true);
        DccLocoAddress l2 = new DccLocoAddress(4321, true);

        Assert.assertTrue("equate ", l1.hashCode() == l2.hashCode());
    }

    public void testHash6() {
        DccLocoAddress l1 = new DccLocoAddress(4321, false);
        DccLocoAddress l2 = new DccLocoAddress(4321, false);

        Assert.assertTrue("equate ", l1.hashCode() == l2.hashCode());
    }

    // from here down is testing infrastructure
    public DccLocoAddressTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {DccLocoAddressTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite(DccLocoAddressTest.class);
        return suite;
    }

}
