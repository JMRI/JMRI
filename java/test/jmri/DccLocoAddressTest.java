package jmri;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test simple functioning of DccLocoAddress
 *
 * @author Bob Jacobsen Copyright (C) 2005
 */
public class DccLocoAddressTest {

    @Test
    public void testValue1() {
        DccLocoAddress l = new DccLocoAddress(12, true);
        assertEquals(12, l.getNumber(), "number ");
        assertTrue( l.isLongAddress(), "long/short ");
    }

    @Test
    public void testValue2() {
        DccLocoAddress l = new DccLocoAddress(12, false);
        assertEquals(12, l.getNumber(), "number ");
        assertFalse( l.isLongAddress(), "long/short ");
    }

    @Test
    public void testValue3() {
        DccLocoAddress l = new DccLocoAddress(121, true);
        Assertions.assertEquals(121, l.getNumber(), "number ");
        Assertions.assertTrue( l.isLongAddress(), "long/short ");
    }

    @Test
    public void testCopy1() {
        DccLocoAddress l = new DccLocoAddress(new DccLocoAddress(121, true));
        Assertions.assertEquals(121, l.getNumber(), "number ");
        Assertions.assertTrue( l.isLongAddress(), "long/short ");
    }

    @Test
    public void testEquals1() {
        DccLocoAddress l1 = new DccLocoAddress(121, true);
        DccLocoAddress l2 = new DccLocoAddress(121, true);

        assertTrue( l1.equals(l2), "equate ");

        assertTrue( l1.equals(l1), "reflexive 1 ");
        assertTrue( l2.equals(l2), "reflexive 2 ");

        assertNotNull( l1, "null 1 ");
        assertNotNull( l2, "null 2 ");
        assertTrue( (l2.equals(l1)) == ((l1.equals(l2))), "transitive ");

    }

    @Test
    public void testEquals2() {
        DccLocoAddress l1 = new DccLocoAddress(4321, true);
        DccLocoAddress l2 = new DccLocoAddress(121, true);

        assertFalse( l1.equals(l2), "equate ");

        assertTrue( l1.equals(l1), "reflexive 1 ");
        assertTrue( l2.equals(l2), "reflexive 2 ");

        assertNotNull( l1, "null 1 ");
        assertNotNull( l2, "null 2 ");
        assertTrue( (l2.equals(l1)) == ((l1.equals(l2))), "transitive ");

    }

    @Test
    public void testEquals3() {
        DccLocoAddress l1 = new DccLocoAddress(121, false);
        DccLocoAddress l2 = new DccLocoAddress(121, true);

        assertFalse( l1.equals(l2), "equate ");

        assertTrue( l1.equals(l1), "reflexive 1 ");
        assertTrue( l2.equals(l2), "reflexive 2 ");

        assertNotNull( l1, "null 1 ");
        assertNotNull( l2, "null 2 ");
        assertTrue( (l2.equals(l1)) == ((l1.equals(l2))), "transitive ");

    }

    @Test
    public void testEquals4() {
        DccLocoAddress l1 = new DccLocoAddress(4321, true);
        DccLocoAddress l2 = new DccLocoAddress(121, false);

        assertFalse( l1.equals(l2), "equate ");

        assertTrue( l1.equals(l1), "reflexive 1 ");
        assertTrue( l2.equals(l2), "reflexive 2 ");

        assertNotNull( l1, "null 1 ");
        assertNotNull( l2, "null 2 ");
        assertTrue( (l2.equals(l1)) == ((l1.equals(l2))), "transitive ");

    }

    @Test
    public void testHash0() {
        DccLocoAddress l1 = new DccLocoAddress(121, true);
        DccLocoAddress l2 = new DccLocoAddress(4321, false);

        assertTrue( l1.hashCode() == l1.hashCode(), "equate self 1");
        assertTrue( l2.hashCode() == l2.hashCode(), "equate self 2");
    }

    @Test
    public void testHash1() {
        DccLocoAddress l1 = new DccLocoAddress(121, true);
        DccLocoAddress l2 = new DccLocoAddress(121, true);

        assertTrue( l1.hashCode() == l2.hashCode(), "equate ");
    }

    @Test
    public void testHash2() {
        DccLocoAddress l1 = new DccLocoAddress(4321, true);
        DccLocoAddress l2 = new DccLocoAddress(121, true);

        assertTrue( l1.hashCode() != l2.hashCode(), "equate ");
    }

    @Test
    public void testHash3() {
        DccLocoAddress l1 = new DccLocoAddress(4321, false);
        DccLocoAddress l2 = new DccLocoAddress(4321, true);

        assertTrue( l1.hashCode() != l2.hashCode(), "equate ");
    }

    @Test
    public void testHash4() {
        DccLocoAddress l1 = new DccLocoAddress(4321, false);
        DccLocoAddress l2 = new DccLocoAddress(121, true);

        assertTrue( l1.hashCode() != l2.hashCode(), "equate ");
    }

    @Test
    public void testHash5() {
        DccLocoAddress l1 = new DccLocoAddress(4321, true);
        DccLocoAddress l2 = new DccLocoAddress(4321, true);

        assertTrue( l1.hashCode() == l2.hashCode(), "equate ");
    }

    @Test
    public void testHash6() {
        DccLocoAddress l1 = new DccLocoAddress(4321, false);
        DccLocoAddress l2 = new DccLocoAddress(4321, false);

        assertTrue( l1.hashCode() == l2.hashCode(), "equate ");
    }

}
