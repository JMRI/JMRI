package jmri.jmrix.powerline;

import jmri.util.JUnitUtil;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.junit.Assert;

/**
 * JUnit tests for the X10Sequence class.
 *
 * @author	Bob Jacobsen Copyright 2003, 2007, 2008
 * @author	Dave Duchamp multi-node extensions 2003
 */
public class X10SequenceTest {

    @Test
    public void testCtors() {
        new X10Sequence();
    }

    @Test
    public void testSequence() {
        X10Sequence s = new X10Sequence();
        s.addAddress(1, 2);
        s.addFunction(1, 3, 0);

        // 
        s.reset();
        X10Sequence.Command a1 = s.getCommand();
        Assert.assertTrue("1 is address", a1.isAddress());
        Assert.assertTrue("1 is not function", !a1.isFunction());

        X10Sequence.Command a2 = s.getCommand();
        Assert.assertTrue("2 is not address", !a2.isAddress());
        Assert.assertTrue("2 is function", a2.isFunction());

        X10Sequence.Command a3 = s.getCommand();
        Assert.assertTrue("3 is null", a3 == null);

    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
