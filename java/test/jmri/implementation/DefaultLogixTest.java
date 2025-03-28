package jmri.implementation;

import jmri.*;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test the DefaultLogixTest implementation class
 *
 * @author Bob Jacobsen Copyright (C) 2015
 */
public class DefaultLogixTest extends NamedBeanTest {

    /**
     * Operate parent NamedBeanTest tests.
     */
    @Override
    protected NamedBean createInstance() {
        return new DefaultLogix("IX 0");
    }

    @Test
    public void testDefaultLogixCtorDouble() {
        assertNotNull( new DefaultLogix("IX 1", "IX 1 user name") );
    }

    @Test
    public void testDefaultLogixCtorSingle() {
        assertNotNull( new DefaultLogix("IX 2") );
    }

    @Test
    public void testBasicBeanOperations() {
        Logix ix1 = new DefaultLogix("IX 3", "IX 3 user name");

        Logix ix2 = new DefaultLogix("IX 4");

        assertFalse( ix1.equals(ix2), "object not equals");
        assertFalse( ix2.equals(ix1), "object not equals reverse");

        assertNotEquals( ix1.hashCode(), ix2.hashCode(), "hash not equals");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
