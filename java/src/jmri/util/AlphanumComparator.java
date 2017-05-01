package jmri.util;

/*
 * The Alphanum Algorithm is an improved sorting algorithm for strings
 * containing numbers.  Instead of sorting numbers in ASCII order like
 * a standard sort, this algorithm sorts numbers in numeric order.
 *
 * The Alphanum Algorithm is discussed at http://www.DaveKoelle.com
 *
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

import java.util.Comparator;

/**
 * This is an updated version with enhancements made by Daniel Migowski,
 * Andre Bogus, and David Koelle
 *
 * To convert to use Templates (Java 1.5+):
 *   - Change "implements Comparator" to {@code "implements Comparator<String>" }
 *   - Change "compare(Object o1, Object o2)" to "compare(String s1, String s2)"
 *   - Remove the type checking and casting in compare().
 *
 * To use this class:
 *   Use the static "sort" method from the java.util.Collections class:
 *   Collections.sort(your list, new AlphanumComparator());
 *
 * Note: this code compares numbers one at a time if those numbers are in
 * chunks of the same size. For example, when comparing abc123 to abc184,
 * 123 and 184 are the same size, so their values are compared digit-by-
 * digit: 1 equals 1, 2 is less than 8, etc. This was done to solve the
 * problem of numeric chunks that are too large to fit in range of values
 * allowed by the programming language for a particular datatype: in Java,
 * an int is limited to 2147483647. The problem with this approach is
 * doesn't properly handle numbers that have leading zeros. For example,
 * 0001 is seem as larger than 1 because it's the longer number. A
 * version that does not compare leading zeros is forthcoming.
 */
public final class AlphanumComparator implements Comparator<String> {

    private final boolean isDigit(char ch) {
        return ch >= 48 && ch <= 57;
    }

    /** Length of string is passed in for improved efficiency (only need to calculate it once) **/
    private final String getChunk(String s, int slength, int marker) {
        StringBuilder chunk = new StringBuilder();
        char c = s.charAt(marker);
        chunk.append(c);
        marker++;
        boolean startIsDigit = isDigit(c);
        while (marker < slength) {
            c = s.charAt(marker);
            if (isDigit(c) != startIsDigit)
                break;
            chunk.append(c);
            marker++;
        }
        return chunk.toString();
    }

    @Override
    //public int compare(Object o1, Object o2) {
    public int compare(String s1, String s2) {
        //String s1 = o1.toString();
        //String s2 = o2.toString();
        int length1 = s1.length();
        int length2 = s2.length();

        int marker1 = 0, marker2 = 0;
        while (marker1 < length1 && marker2 < length2) {
            String chunk1 = getChunk(s1, length1, marker1);
            marker1 += chunk1.length();

            String chunk2 = getChunk(s2, length2, marker2);
            marker2 += chunk2.length();

            // If both chunks contain numeric characters, sort them numerically
            int result = 0;
            if (isDigit(chunk1.charAt(0)) && isDigit(chunk2.charAt(0))) {
                // Simple chunk comparison by length.
                int chunkLength1 = chunk1.length();
                result = chunkLength1 - chunk2.length();
                // If equal, the first different number counts
                if (result == 0) {
                    for (int i = 0; i < chunkLength1; i++) {
                        result = chunk1.charAt(i) - chunk2.charAt(i);
                        if (result != 0) {
                            return result;
                        }
                    }
                }
            } else {
                result = chunk1.compareTo(chunk2);
            }

            if (result != 0) {
                return result;
            }
        }
        return length1 - length2;
    }
}
