package jmri.util;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        assertNotNull( t, "exists");
    }

    @Test
    public void testAddAndGet() {
        NonNullArrayList<Integer> t = new NonNullArrayList<>();
        t.add(100);
        assertNotNull( t.get(0), "[0]");
        assertNotNull( t.get(0).toString(), "SpotBugs should complain about dereference");
    }

    @Test
    public void testNotAddNull() {
        NonNullArrayList<Integer> t = new NonNullArrayList<>();
        Exception ex = assertThrows(IllegalArgumentException.class,
            () -> { addNull(t); } );
        assertEquals("NonNullArrayList.addAll cannot add null item", ex.getMessage());
        assertEquals(0, t.size());
    }

    @SuppressWarnings("null") // passing null to non-null to check exception
    private void addNull(NonNullArrayList<Integer> t){
        t.add(null);
    }

    @Test
    public void testNotAddIndexNull() {
        NonNullArrayList<Integer> t = new NonNullArrayList<>();
        Exception ex = Assertions.assertThrows(IllegalArgumentException.class,
            () -> { addZeroNull(t); } );
        assertEquals("NonNullArrayList.addAll cannot add null item", ex.getMessage());
        assertEquals(0, t.size());
    }

    @SuppressWarnings("null") // passing null to non-null to check exception
    private void addZeroNull(NonNullArrayList<Integer> t){
        t.add(0, null);
    }

    @Test
    public void testNotSetNull() {
        NonNullArrayList<Integer> t = new NonNullArrayList<>();
        Exception ex = Assertions.assertThrows(IllegalArgumentException.class,
            () -> { setZeroNull(t); } );
        assertEquals("NonNullArrayList.addAll cannot set item null", ex.getMessage());
        assertEquals(0, t.size());
    }

    @SuppressWarnings("null") // passing null to non-null to check exception
    private void setZeroNull(NonNullArrayList<Integer> t){
        t.set(0, null);
    }

    @Test
    public void testLoop() {
        NonNullArrayList<Integer> t = new NonNullArrayList<>();
        t.add(100);
        for (Integer s : t) {
            assertNotNull( t.get(0), "s");
            assertNotNull( s.toString(), "SpotBugs should complain about dereference");
            assertNotNull(t.get(0).toString());
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
