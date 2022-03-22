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
      * @param T the Type of the array contents
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

    // private transient final static Logger log = LoggerFactory.getLogger(ArrayUtil.class);
}
