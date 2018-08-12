package jmri.util;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * This is here so that SpotBugs can check proper static performance.
 * The {@link NonNullArrayList} class and this test class should always
 * show completely clean in SpotBugs.
 *
 * @author Bob Jacobsen Copyright (C) 2017
 */
public class NonNullArrayListTest {

    @Test
    public void testCTor() {
        NonNullArrayList<String> t = new NonNullArrayList<>();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testAddAndGet() {
        NonNullArrayList<Integer> t = new NonNullArrayList<>();
        t.add(100);
        Assert.assertNotNull("[0]", t.get(0));
        Assert.assertNotNull("SpotBugs should complain about dereference", t.get(0).toString());
        t.get(0);
        t.get(0).toString();
    }

    @Test
    public void testNotAddNull() {
        NonNullArrayList<Integer> t = new NonNullArrayList<>();
        boolean thrown = false;
        try {
            t.add(null);
        } catch (IllegalArgumentException e) {
            thrown = true;
        } finally {
            Assert.assertTrue(thrown);
        }
    }

    @Test
    public void testNotAddIndexNull() {
        NonNullArrayList<Integer> t = new NonNullArrayList<>();
        boolean thrown = false;
        try {
            t.add(0, null);
        } catch (IllegalArgumentException e) {
            thrown = true;
        } finally {
            Assert.assertTrue(thrown);
        }
    }

    @Test
    public void testNotSetNull() {
        NonNullArrayList<Integer> t = new NonNullArrayList<>();
        boolean thrown = false;
        try {
            t.set(0, null);
        } catch (IllegalArgumentException e) {
            thrown = true;
        } finally {
            Assert.assertTrue(thrown);
        }
    }

    @Test
    public void testLoop() {
        NonNullArrayList<Integer> t = new NonNullArrayList<>();
        t.add(100);
        for (Integer s : t) {
            Assert.assertNotNull("s", t.get(0));
            Assert.assertNotNull("SpotBugs should complain about dereference", s.toString());
            t.get(0);
            t.get(0).toString();
        }
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
