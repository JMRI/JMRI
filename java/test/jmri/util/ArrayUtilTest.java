package jmri.util;

import java.awt.geom.Point2D;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.python.modules.math;

/**
 * Test simple functioning of ArrayUtil
 *
 * @author Bob Jacobsen
 */
public class ArrayUtilTest {

    @Test
    public void testReverseArray() {
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

    // private final static Logger log = LoggerFactory.getLogger(ArrayUtilTest.class);
}
