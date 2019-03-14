package jmri.implementation;

import jmri.*;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
    public void testCtorDouble() {
        new DefaultLogix("IX 1", "IX 1 user name");
    }

    @Test
    public void testCtorSingle() {
        new DefaultLogix("IX 2");
    }

    @Test
    public void testBasicBeanOperations() {
        Logix ix1 = new DefaultLogix("IX 3", "IX 3 user name");

        Logix ix2 = new DefaultLogix("IX 4");

        Assert.assertTrue("object not equals", !ix1.equals(ix2));
        Assert.assertTrue("object not equals reverse", !ix2.equals(ix1));

        Assert.assertTrue("hash not equals", ix1.hashCode() != ix2.hashCode());

    }

    @Before
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
    }

    @After
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.tearDown();
    }
}
