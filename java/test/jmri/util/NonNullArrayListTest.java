package jmri.util;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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
    }

    @Test
    public void testNotAddNull() {
        NonNullArrayList<Integer> t = new NonNullArrayList<>();
        Exception ex = Assertions.assertThrows(IllegalArgumentException.class,
            () -> { addNull(t); } );
        Assert.assertEquals("NonNullArrayList.addAll cannot add null item", ex.getMessage());
        Assert.assertEquals(0, t.size());
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings( value = "NP_NONNULL_PARAM_VIOLATION",
        justification = "passing null to non-null to check exception")
    private void addNull(NonNullArrayList<Integer> t){
        t.add(null);
    }

    @Test
    public void testNotAddIndexNull() {
        NonNullArrayList<Integer> t = new NonNullArrayList<>();
        Exception ex = Assertions.assertThrows(IllegalArgumentException.class,
            () -> { addZeroNull(t); } );
        Assert.assertEquals("NonNullArrayList.addAll cannot add null item", ex.getMessage());
        Assert.assertEquals(0, t.size());
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings( value = "NP_NONNULL_PARAM_VIOLATION",
        justification = "passing null to non-null to check exception")
    private void addZeroNull(NonNullArrayList<Integer> t){
        t.add(0, null);
    }

    @Test
    public void testNotSetNull() {
        NonNullArrayList<Integer> t = new NonNullArrayList<>();
        Exception ex = Assertions.assertThrows(IllegalArgumentException.class,
            () -> { setZeroNull(t); } );
        Assert.assertEquals("NonNullArrayList.addAll cannot set item null", ex.getMessage());
        Assert.assertEquals(0, t.size());
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings( value = "NP_NONNULL_PARAM_VIOLATION",
        justification = "passing null to non-null to check exception")
    private void setZeroNull(NonNullArrayList<Integer> t){
        t.set(0, null);
    }

    @Test
    public void testLoop() {
        NonNullArrayList<Integer> t = new NonNullArrayList<>();
        t.add(100);
        for (Integer s : t) {
            Assert.assertNotNull("s", t.get(0));
            Assert.assertNotNull("SpotBugs should complain about dereference", s.toString());
            Assert.assertNotNull(t.get(0).toString());
        }
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
