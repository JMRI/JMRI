package jmri.util;

import org.junit.jupiter.api.*;

import jmri.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * @author Paul Bender Copyright (C) 2017
 */
public class NamedBeanPreferNumericComparatorTest {

    @Test
    public void testOneLetterCases() {
        NamedBeanPreferNumericComparator<Turnout> t = new NamedBeanPreferNumericComparator<>();

        Turnout it1 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT1");
        Turnout it10 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT10");
        Turnout it2 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT2");
        Turnout it01 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT01");
        Turnout itUpper = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("ITFOO");
        Turnout itLower = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("ITfoo");
        Turnout itMixed = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("ITFoo");

        assertEquals( 0, t.compare(it1, it1), "IT1 == IT1");

        assertEquals( -1, t.compare(it1, it2), "IT1 < IT2");
        assertEquals( +1, t.compare(it2, it1), "IT2 > IT1");

        assertEquals( +1, t.compare(it10, it2), "IT10 > IT2");
        assertEquals( -1, t.compare(it2, it10), "IT2 < IT10");

        assertNotEquals( 0, t.compare(it1, it01), "IT1 != IT01");
        assertEquals( -1, t.compare(it01, it1), "IT01 < IT1");
        assertEquals( +1, t.compare(it1, it01), "IT1 > IT01");

        assertNotEquals( 0, t.compare(itUpper, itMixed), "ITFOO != ITFoo");
        assertNotEquals( 0, t.compare(itUpper, itLower), "ITFOO != ITfoo");
        assertNotEquals( 0, t.compare(itLower, itMixed), "ITfoo != ITFoo");
        assertEquals( -1, t.compare(itUpper, itMixed), "ITFOO < ITFoo");
        assertEquals( -1, t.compare(itUpper, itLower), "ITFOO < ITfoo");
        assertEquals( -1, t.compare(itMixed, itLower), "ITFoo < ITfoo");
        assertEquals( +1, t.compare(itLower, itMixed), "ITfoo > ITFoo");
        assertEquals( +1, t.compare(itLower, itUpper), "ITfoo > ITFOO");
        assertEquals( +1, t.compare(itMixed, itUpper), "ITFoo > ITFOO");
    }

    @Test
    public void testTwoLetterCases() {
        NamedBeanPreferNumericComparator<Turnout> t = new NamedBeanPreferNumericComparator<>();

        Turnout i2t1 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("I2T1");
        Turnout i2t10 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("I2T10");
        Turnout i2t2 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("I2T2");

        assertEquals( 0, t.compare(i2t1, i2t1), "I2T1 == I2T1");

        assertEquals( -1, t.compare(i2t1, i2t2), "I2T1 < I2T2");
        assertEquals( +1, t.compare(i2t2, i2t1), "I2T2 > I2T1");

        assertEquals( +1, t.compare(i2t10, i2t2), "I2T10 > I2T2");
        assertEquals( -1, t.compare(i2t2, i2t10), "I2T2 < I2T10");
    }

    @Test
    public void testThreeLetterCases() {
        NamedBeanPreferNumericComparator<Turnout> t = new NamedBeanPreferNumericComparator<>();

        Turnout i23t1 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("I23T1");
        Turnout i23t10 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("I23T10");
        Turnout i23t2 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("I23T2");

        assertEquals( 0, t.compare(i23t1, i23t1), "I23T1 == I23T1");

        assertEquals( -1, t.compare(i23t1, i23t2), "I23T1 < I23T2");
        assertEquals( +1, t.compare(i23t2, i23t1), "I23T2 > I23T1");

        assertEquals( +1, t.compare(i23t10, i23t2), "I23T10 > I23T2");
        assertEquals( -1, t.compare(i23t2, i23t10), "I23T2 < I23T10");
    }

    private boolean hit = false;

    /**
     * With the {@link NamedBeanPreferNumericComparator},
     * {@link NamedBean#compareSystemNameSuffix(String, String, NamedBean)}
     * should not be called.
     */
    @Test
    public void testSystemSpecificCase() {
        NamedBeanPreferNumericComparator<Turnout> t = new NamedBeanPreferNumericComparator<>();

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
        assertEquals( -1, t.compare(it1, it2), "IT1 < IT2");
        assertFalse(hit);

        hit = false;
        assertEquals( +1, t.compare(it2, it1), "IT2 < IT1");
        assertFalse(hit);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log =
    // LoggerFactory.getLogger(NamedBeanComparatorTest.class);

}
