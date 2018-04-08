package jmri.util.com.dictiography.collections;

import junit.framework.TestCase;

import java.util.*;

/**
 * User: Vitaly Sazanovich
 * Date: 07/02/13
 * Time: 15:01
 * Email: Vitaly.Sazanovich@gmail.com
 */
public class IndexedTreeMapTest extends TestCase {

    public void testIndexedTreeMap() throws Exception {
        long t1 = System.currentTimeMillis();
        for (int i = 0; i < 5; i++) {
            Set<Integer> set = new HashSet<Integer>();
            Random random = new Random(System.currentTimeMillis());
            IndexedNavigableMap<Integer, Integer> m = new IndexedTreeMap<Integer, Integer>();

            while (set.size() < 1000) {
                Integer next = random.nextInt();
                if (!set.contains(next)) {
                    set.add(next);
                    m.put(next, next);
                    ((IndexedTreeMap) m).debug();
                }
            }
            Integer[] ints = set.toArray(new Integer[set.size()]);
            Arrays.sort(ints);
            for (int k = 0; k < ints.length; k++) {
                int ek = m.exactKey(k).intValue();
                int ev = m.exactEntry(k).getValue().intValue();
                int ind = m.keyIndex(ek);

                assertEquals(ints[k].intValue(), ek);
                assertEquals(ints[k].intValue(), ev);
                assertEquals(k, ind);

            }
            Iterator<Integer> it = set.iterator();
            while (it.hasNext()) {
                Integer next = it.next();
                m.remove(next);
                assertEquals(false, m.containsKey(next));
                ((IndexedTreeMap) m).debug();
            }
            assertEquals(0, m.size());
        }

        System.out.println("DONE IN:" + (System.currentTimeMillis() - t1));
    }




    public void testComparePutMap() throws Exception {

        Random random = new Random(System.currentTimeMillis());
        IndexedNavigableMap<Integer, Integer> m1 = new IndexedTreeMap<Integer, Integer>();
        NavigableMap<Integer, Integer> m2 = new TreeMap<Integer, Integer>();

        Set<Integer> set = new HashSet<Integer>();

        while (set.size() < 100000) {
            Integer next = random.nextInt();
            if (!set.contains(next)) {
                set.add(next);
            }
        }

        long t1 = System.currentTimeMillis();
        Iterator<Integer> it = set.iterator();
        while (it.hasNext()) {
            Integer next = it.next();
            m1.put(next, next);
        }
        long t2 = System.currentTimeMillis();

        long t3 = System.currentTimeMillis();
        it = set.iterator();
        while (it.hasNext()) {
            Integer next = it.next();
            m2.put(next, next);
        }
        long t4 = System.currentTimeMillis();

        System.out.println("For " + set.size() + " elements TreeMap wins IndexedTreeMap in put by:" + ((t2 - t1) - (t4 - t3)) + " milliseconds");
    }

    public void testCompareDeleteMap() throws Exception {

        Random random = new Random(System.currentTimeMillis());
        IndexedNavigableMap<Integer, Integer> m1 = new IndexedTreeMap<Integer, Integer>();
        NavigableMap<Integer, Integer> m2 = new TreeMap<Integer, Integer>();

        Set<Integer> set = new HashSet<Integer>();

        while (set.size() < 100000) {
            Integer next = random.nextInt();
            if (!set.contains(next)) {
                set.add(next);
                m1.put(next, next);
                m2.put(next, next);
            }
        }

        long t1 = System.currentTimeMillis();
        Iterator<Integer> it = set.iterator();
        while (it.hasNext()) {
            Integer next = it.next();
            m1.remove(next);
        }
        long t2 = System.currentTimeMillis();

        long t3 = System.currentTimeMillis();
        it = set.iterator();
        while (it.hasNext()) {
            Integer next = it.next();
            m2.remove(next);
        }
        long t4 = System.currentTimeMillis();

        System.out.println("For " + set.size() + " elements TreeMap wins IndexedTreeMap in remove by:" + ((t2 - t1) - (t4 - t3)) + " milliseconds");
    }

}
