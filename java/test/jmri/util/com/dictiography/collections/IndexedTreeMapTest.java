package jmri.util.com.dictiography.collections;

import org.junit.*;

import java.util.*;

/**
 * User: Vitaly Sazanovich
 * Date: 07/02/13
 * Time: 15:01
 * Email: Vitaly.Sazanovich@gmail.com
 */
public class IndexedTreeMapTest {

    @Test
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

                Assert.assertEquals(ints[k].intValue(), ek);
                Assert.assertEquals(ints[k].intValue(), ev);
                Assert.assertEquals(k, ind);

            }
            Iterator<Integer> it = set.iterator();
            while (it.hasNext()) {
                Integer next = it.next();
                m.remove(next);
                Assert.assertEquals(false, m.containsKey(next));
                ((IndexedTreeMap) m).debug();
            }
            Assert.assertEquals(0, m.size());
        }

        log.debug("DONE IN:" + (System.currentTimeMillis() - t1));
    }

    @Test
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

        log.debug("For " + set.size() + " elements TreeMap wins IndexedTreeMap in put by:" + ((t2 - t1) - (t4 - t3)) + " milliseconds");
    }

    @Test
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

        log.debug("For " + set.size() + " elements TreeMap wins IndexedTreeMap in remove by:" + ((t2 - t1) - (t4 - t3)) + " milliseconds");
    }
    
    @BeforeClass
    static public void setUpClass() {
          jmri.util.JUnitUtil.setUp();
    }

    @AfterClass
    static public void tearDownClass() {
          jmri.util.JUnitUtil.tearDown();
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IndexedTreeSetTest.class);

}
