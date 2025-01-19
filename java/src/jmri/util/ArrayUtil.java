package jmri.util;

import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * Useful array methods.
 *
 * @author Bob Jacobsen 2022
 */
public final class ArrayUtil {

    // Class only supplies static methods
    private ArrayUtil() {}

    /**
      * Reverse an array of objects.
      * <p>
      * Not suitable for primitive types.
      *
      * @param <T> the Type of the array contents
      * @param elementsArray the array
      * @return the reversed array
      */
    public static <T> T[] reverse(@Nonnull T[] elementsArray) {
        T[] elements = Objects.requireNonNull(elementsArray);
        var results = java.util.Arrays.copyOf(elements, elements.length);
        for (int i = 0; i < elements.length; i++) {
            results[i] = elements[elements.length-i-1];
        }
        return results;
    }

    /**
      * Reverse an array of ints.
      *
      * @param elementsArray the array
      * @return the reversed array
      */
    public static int[] reverse(@Nonnull int[] elementsArray) {
        int[] elements = Objects.requireNonNull(elementsArray);
        var results = new int[elements.length];
        for (int i = 0; i < elements.length; i++) {
            results[i] = elements[elements.length-i-1];
        }
        return results;
    }

    /**
      * Reverse an array of longs.
      *
      * @param elementsArray the array
      * @return the reversed array
      */
    public static long[] reverse(@Nonnull long[] elementsArray) {
        long[] elements = Objects.requireNonNull(elementsArray);
        var results = new long[elements.length];
        for (int i = 0; i < elements.length; i++) {
            results[i] = elements[elements.length-i-1];
        }
        return results;
    }

    /**
      * Reverse an array of doubles.
      *
      * @param elementsArray the array
      * @return the reversed array
      */
    public static double[] reverse(@Nonnull double[] elementsArray) {
        double[] elements = Objects.requireNonNull(elementsArray);
        var results = new double[elements.length];
        for (int i = 0; i < elements.length; i++) {
            results[i] = elements[elements.length-i-1];
        }
        return results;
    }

    /**
      * Reverse an array of floats.
      *
      * @param elementsArray the array
      * @return the reversed array
      */
    public static float[] reverse(@Nonnull float[] elementsArray) {
        float[] elements = Objects.requireNonNull(elementsArray);
        var results = new float[elements.length];
        for (int i = 0; i < elements.length; i++) {
            results[i] = elements[elements.length-i-1];
        }
        return results;
    }

    /**
      * Reverse an array of booleans.
      *
      * @param elementsArray the array
      * @return the reversed array
      */
    public static boolean[] reverse(@Nonnull boolean[] elementsArray) {
        boolean[] elements = Objects.requireNonNull(elementsArray);
        var results = new boolean[elements.length];
        for (int i = 0; i < elements.length; i++) {
            results[i] = elements[elements.length-i-1];
        }
        return results;
    }

    // private transient final static Logger log = LoggerFactory.getLogger(ArrayUtil.class);
}
