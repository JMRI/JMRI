package jmri.util;

import java.awt.geom.Point2D;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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
        Assert.assertEquals(stringArray[0], reversedStringArray[2]);
        Assert.assertEquals(stringArray[1], reversedStringArray[1]);
        Assert.assertEquals(stringArray[2], reversedStringArray[0]);
        Assert.assertEquals(stringArray.length, reversedStringArray.length);

        var integerArray = new Integer[]{10, 11, 12};
        var reversedIntegerArray = ArrayUtil.reverse(integerArray);
        Assert.assertEquals(integerArray[0], reversedIntegerArray[2]);
        Assert.assertEquals(integerArray[1], reversedIntegerArray[1]);
        Assert.assertEquals(integerArray[2], reversedIntegerArray[0]);
        Assert.assertEquals(integerArray.length, reversedIntegerArray.length);

        var p2dArray = new Point2D[]{new Point2D.Double(0.,0.), new Point2D.Double(1.,1.), new Point2D.Double(2.,2.)};
        var reversedP2DArray = ArrayUtil.reverse(p2dArray);
        Assert.assertEquals(p2dArray[0], reversedP2DArray[2]);
        Assert.assertEquals(p2dArray[1], reversedP2DArray[1]);
        Assert.assertEquals(p2dArray[2], reversedP2DArray[0]);
        Assert.assertEquals(p2dArray.length, reversedP2DArray.length);
    }

    @Test
    public void testReverseArrayInt() {
        var stringArray = new int[]{10, 11, 12};
        var reversedStringArray = ArrayUtil.reverse(stringArray);
        Assert.assertEquals(stringArray[0], reversedStringArray[2]);
        Assert.assertEquals(stringArray[1], reversedStringArray[1]);
        Assert.assertEquals(stringArray[2], reversedStringArray[0]);
        Assert.assertEquals(stringArray.length, reversedStringArray.length);
    }

    @Test
    public void testReverseArrayLong() {
        var stringArray = new long[]{10L, 11L, 12L};
        var reversedStringArray = ArrayUtil.reverse(stringArray);
        Assert.assertEquals(stringArray[0], reversedStringArray[2]);
        Assert.assertEquals(stringArray[1], reversedStringArray[1]);
        Assert.assertEquals(stringArray[2], reversedStringArray[0]);
        Assert.assertEquals(stringArray.length, reversedStringArray.length);
    }

    @Test
    public void testReverseArrayDouble() {
        var stringArray = new double[]{10., 11., 12.};
        var reversedStringArray = ArrayUtil.reverse(stringArray);
        Assert.assertEquals(stringArray[0], reversedStringArray[2], 0.001);
        Assert.assertEquals(stringArray[1], reversedStringArray[1], 0.001);
        Assert.assertEquals(stringArray[2], reversedStringArray[0], 0.001);
        Assert.assertEquals(stringArray.length, reversedStringArray.length);
    }

    @Test
    public void testReverseArrayFloat() {
        var stringArray = new float[]{10.f, 11.f, 12.f};
        var reversedStringArray = ArrayUtil.reverse(stringArray);
        Assert.assertEquals(stringArray[0], reversedStringArray[2], 0.001f);
        Assert.assertEquals(stringArray[1], reversedStringArray[1], 0.001f);
        Assert.assertEquals(stringArray[2], reversedStringArray[0], 0.001f);
        Assert.assertEquals(stringArray.length, reversedStringArray.length);
    }

    @Test
    public void testReverseArrayBoolean() {
        var stringArray = new boolean[]{true, false, false};
        var reversedStringArray = ArrayUtil.reverse(stringArray);
        Assert.assertEquals(stringArray[0], reversedStringArray[2]);
        Assert.assertEquals(stringArray[1], reversedStringArray[1]);
        Assert.assertEquals(stringArray[2], reversedStringArray[0]);
        Assert.assertEquals(stringArray.length, reversedStringArray.length);
    }

    // private final static Logger log = LoggerFactory.getLogger(ArrayUtilTest.class);
}
