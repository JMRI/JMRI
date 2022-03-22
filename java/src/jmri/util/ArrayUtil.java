package jmri.util;

import javax.annotation.Nonnull;

/**
 * Useful array methods.
 *
 * @author Bob Jacobsen 2022
 */
public final class ArrayUtil {

    /**
      * Reverse an array of objects.
      * <p>
      * Not suitable for primitive types.
      *
      * @param <T> the Type of the array contents
      * @param elements the array
      * @return the reversed array
      */
    public static <T> T[] reverse(@Nonnull T[] elements) {
        assert (elements != null);
        var results = java.util.Arrays.copyOf(elements, elements.length);
        for (int i = 0; i < elements.length; i++) {
            results[i] = elements[elements.length-i-1];
        }
        return results;
    }

    /**
      * Reverse an array of ints.
      *
      * @param elements the array
      * @return the reversed array
      */
    public static int[] reverse(@Nonnull int[] elements) {
        assert (elements != null);
        var results = new int[elements.length];
        for (int i = 0; i < elements.length; i++) {
            results[i] = elements[elements.length-i-1];
        }
        return results;
    }

    /**
      * Reverse an array of longs.
      *
      * @param elements the array
      * @return the reversed array
      */
    public static long[] reverse(@Nonnull long[] elements) {
        assert (elements != null);
        var results = new long[elements.length];
        for (int i = 0; i < elements.length; i++) {
            results[i] = elements[elements.length-i-1];
        }
        return results;
    }

    /**
      * Reverse an array of doubles.
      *
      * @param elements the array
      * @return the reversed array
      */
    public static double[] reverse(@Nonnull double[] elements) {
        assert (elements != null);
        var results = new double[elements.length];
        for (int i = 0; i < elements.length; i++) {
            results[i] = elements[elements.length-i-1];
        }
        return results;
    }

    /**
      * Reverse an array of floats.
      *
      * @param elements the array
      * @return the reversed array
      */
    public static float[] reverse(@Nonnull float[] elements) {
        assert (elements != null);
        var results = new float[elements.length];
        for (int i = 0; i < elements.length; i++) {
            results[i] = elements[elements.length-i-1];
        }
        return results;
    }

    /**
      * Reverse an array of booleans.
      *
      * @param elements the array
      * @return the reversed array
      */
    public static boolean[] reverse(@Nonnull boolean[] elements) {
        assert (elements != null);
        var results = new boolean[elements.length];
        for (int i = 0; i < elements.length; i++) {
            results[i] = elements[elements.length-i-1];
        }
        return results;
    }

    // private transient final static Logger log = LoggerFactory.getLogger(ArrayUtil.class);
}
