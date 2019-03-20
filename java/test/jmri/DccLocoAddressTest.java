package jmri;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test simple functioning of DccLocoAddress
 *
 * @author Bob Jacobsen Copyright (C) 2005
 */
public class DccLocoAddressTest {

    @Test
    public void testValue1() {
        DccLocoAddress l = new DccLocoAddress(12, true);
        Assert.assertEquals("number ", l.getNumber(), 12);
        Assert.assertEquals("long/short ", l.isLongAddress(), true);
    }

    @Test
    public void testValue2() {
        DccLocoAddress l = new DccLocoAddress(12, false);
        Assert.assertEquals("number ", l.getNumber(), 12);
        Assert.assertEquals("long/short ", l.isLongAddress(), false);
    }

    @Test
    public void testValue3() {
        DccLocoAddress l = new DccLocoAddress(121, true);
        Assert.assertEquals("number ", l.getNumber(), 121);
        Assert.assertEquals("long/short ", l.isLongAddress(), true);
    }

    @Test
    public void testCopy1() {
        DccLocoAddress l = new DccLocoAddress(new DccLocoAddress(121, true));
        Assert.assertEquals("number ", l.getNumber(), 121);
        Assert.assertEquals("long/short ", l.isLongAddress(), true);
    }

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
    public void testHash0() {
        DccLocoAddress l1 = new DccLocoAddress(121, true);
        DccLocoAddress l2 = new DccLocoAddress(4321, false);

        Assert.assertTrue("equate self 1", l1.hashCode() == l1.hashCode());
        Assert.assertTrue("equate self 2", l2.hashCode() == l2.hashCode());
    }

    @Test
    public void testHash1() {
        DccLocoAddress l1 = new DccLocoAddress(121, true);
        DccLocoAddress l2 = new DccLocoAddress(121, true);

        Assert.assertTrue("equate ", l1.hashCode() == l2.hashCode());
    }

    @Test
    public void testHash2() {
        DccLocoAddress l1 = new DccLocoAddress(4321, true);
        DccLocoAddress l2 = new DccLocoAddress(121, true);

        Assert.assertTrue("equate ", l1.hashCode() != l2.hashCode());
    }

    @Test
    public void testHash3() {
        DccLocoAddress l1 = new DccLocoAddress(4321, false);
        DccLocoAddress l2 = new DccLocoAddress(4321, true);

        Assert.assertTrue("equate ", l1.hashCode() != l2.hashCode());
    }

    @Test
    public void testHash4() {
        DccLocoAddress l1 = new DccLocoAddress(4321, false);
        DccLocoAddress l2 = new DccLocoAddress(121, true);

        Assert.assertTrue("equate ", l1.hashCode() != l2.hashCode());
    }

    @Test
    public void testHash5() {
        DccLocoAddress l1 = new DccLocoAddress(4321, true);
        DccLocoAddress l2 = new DccLocoAddress(4321, true);

        Assert.assertTrue("equate ", l1.hashCode() == l2.hashCode());
    }

    @Test
    public void testHash6() {
        DccLocoAddress l1 = new DccLocoAddress(4321, false);
        DccLocoAddress l2 = new DccLocoAddress(4321, false);

        Assert.assertTrue("equate ", l1.hashCode() == l2.hashCode());
    }

}
