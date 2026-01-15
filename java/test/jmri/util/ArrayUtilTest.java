package jmri.util;

import java.awt.geom.Point2D;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test simple functioning of ArrayUtil
 *
 * @author Bob Jacobsen
 */
public class ArrayUtilTest {

    @Test
    public void testReverseArrayObjects() {
        var stringArray = new String[]{"a", "b", "c"};
        var reversedStringArray = ArrayUtil.reverse(stringArray);
        assertEquals(stringArray[0], reversedStringArray[2]);
        assertEquals(stringArray[1], reversedStringArray[1]);
        assertEquals(stringArray[2], reversedStringArray[0]);
        assertEquals(stringArray.length, reversedStringArray.length);

        var integerArray = new Integer[]{10, 11, 12};
        var reversedIntegerArray = ArrayUtil.reverse(integerArray);
        assertEquals(integerArray[0], reversedIntegerArray[2]);
        assertEquals(integerArray[1], reversedIntegerArray[1]);
        assertEquals(integerArray[2], reversedIntegerArray[0]);
        assertEquals(integerArray.length, reversedIntegerArray.length);

        var p2dArray = new Point2D[]{new Point2D.Double(0.,0.), new Point2D.Double(1.,1.), new Point2D.Double(2.,2.)};
        var reversedP2DArray = ArrayUtil.reverse(p2dArray);
        assertEquals(p2dArray[0], reversedP2DArray[2]);
        assertEquals(p2dArray[1], reversedP2DArray[1]);
        assertEquals(p2dArray[2], reversedP2DArray[0]);
        assertEquals(p2dArray.length, reversedP2DArray.length);
    }

    @Test
    public void testReverseArrayInt() {
        var stringArray = new int[]{10, 11, 12};
        var reversedStringArray = ArrayUtil.reverse(stringArray);
        assertEquals(stringArray[0], reversedStringArray[2]);
        assertEquals(stringArray[1], reversedStringArray[1]);
        assertEquals(stringArray[2], reversedStringArray[0]);
        assertEquals(stringArray.length, reversedStringArray.length);
    }

    @Test
    public void testReverseArrayLong() {
        var stringArray = new long[]{10L, 11L, 12L};
        var reversedStringArray = ArrayUtil.reverse(stringArray);
        assertEquals(stringArray[0], reversedStringArray[2]);
        assertEquals(stringArray[1], reversedStringArray[1]);
        assertEquals(stringArray[2], reversedStringArray[0]);
        assertEquals(stringArray.length, reversedStringArray.length);
    }

    @Test
    public void testReverseArrayDouble() {
        var stringArray = new double[]{10., 11., 12.};
        var reversedStringArray = ArrayUtil.reverse(stringArray);
        assertEquals(stringArray[0], reversedStringArray[2], 0.001);
        assertEquals(stringArray[1], reversedStringArray[1], 0.001);
        assertEquals(stringArray[2], reversedStringArray[0], 0.001);
        assertEquals(stringArray.length, reversedStringArray.length);
    }

    @Test
    public void testReverseArrayFloat() {
        var stringArray = new float[]{10.f, 11.f, 12.f};
        var reversedStringArray = ArrayUtil.reverse(stringArray);
        assertEquals(stringArray[0], reversedStringArray[2], 0.001f);
        assertEquals(stringArray[1], reversedStringArray[1], 0.001f);
        assertEquals(stringArray[2], reversedStringArray[0], 0.001f);
        assertEquals(stringArray.length, reversedStringArray.length);
    }

    @Test
    public void testReverseArrayBoolean() {
        var stringArray = new boolean[]{true, false, false};
        var reversedStringArray = ArrayUtil.reverse(stringArray);
        assertEquals(stringArray[0], reversedStringArray[2]);
        assertEquals(stringArray[1], reversedStringArray[1]);
        assertEquals(stringArray[2], reversedStringArray[0]);
        assertEquals(stringArray.length, reversedStringArray.length);
    }

    @Test
    public void testAppendIntArrays() {
        int[] arrayA = new int[]{0};
        int[] arrayB = new int[]{1};
        int[] t = ArrayUtil.appendArray(arrayA, arrayB);
        assertEquals(2, t.length);
        assertEquals( 0, t[0] );
        assertEquals( 1, t[1] );
    }

    @Test
    public void testAppendStringArrays() {
        String[] arrayA = new String[]{"A"};
        String[] arrayB = new String[]{"B"};
        String[] t = ArrayUtil.appendArray(arrayA, arrayB);
        assertEquals(2, t.length);
        assertEquals( "A", t[0] );
        assertEquals( "B", t[1] );
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ArrayUtilTest.class);
}
