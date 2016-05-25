// IterableEnumeration.java
package jmri.util;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * Provide an {@link Iterable} interface for an Enumeration
 * <p>
 * From
 * <a href="http://www.javaspecialists.eu/archive/Issue107.html">JavaSpecialists
 * issue 107</a>
 *
 * @author rhwood
 */
public class IterableEnumeration<T> implements Iterable<T> {

    private final Enumeration<T> en;

    public IterableEnumeration(Enumeration<T> en) {
        this.en = en;
    }

    public Iterator<T> iterator() {
        return new Iterator<T>() {

            public boolean hasNext() {
                return en.hasMoreElements();
            }

            public T next() {
                return en.nextElement();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public static <T> Iterable<T> make(Enumeration<T> en) {
        return new IterableEnumeration<T>(en);
    }
}
