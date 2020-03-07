package jmri.util.com.dictiography.collections;

import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.util.*;

/**
 * User: Vitaly Sazanovich
 * Date: 2/10/13
 * Time: 12:14 PM
 */
public class IndexedTreeSetTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    
    @Test
    public void testQuickCheck() {
        IndexedNavigableSet<String> s = new IndexedTreeSet<String>();
        s.add("Z");
        s.add("A");
        s.add("B");
        Assert.assertEquals("A", s.exact(0));
        Assert.assertEquals("B", s.exact(1));
        Assert.assertEquals("Z", s.exact(2));    
    }
    
    @Test
    public void testQuickComparator() {
        IndexedNavigableSet<String> s = new IndexedTreeSet<String>(new java.util.Comparator<String>(){
            @Override
            public int compare(String e1, String e2) { return - e1.toString().compareTo(e2.toString()); } // note minus sign
        });
        s.add("Z");
        s.add("A");
        s.add("B");
        Assert.assertEquals("Z", s.exact(0));    
        Assert.assertEquals("B", s.exact(1));
        Assert.assertEquals("A", s.exact(2));
    }
    
    @Test
    public void testIndexedTreeSet() throws Exception {
        long t1 = System.currentTimeMillis();
        for (int i = 0; i < 5; i++) {
            Set<Integer> set = new HashSet<Integer>();
            Random random = new Random(System.currentTimeMillis());
            IndexedNavigableSet<Integer> s = new IndexedTreeSet<Integer>();

            while (set.size() < 1000) {
                Integer next = random.nextInt();
                if (!set.contains(next)) {
                    set.add(next);
                    s.add(next);
                    ((IndexedTreeSet) s).debug();
                }
            }
            Integer[] ints = set.toArray(new Integer[set.size()]);
            Arrays.sort(ints);
            for (int k = 0; k < ints.length; k++) {
                int ev = s.exact(k).intValue();
                int ind = s.entryIndex(ints[k]);

                Assert.assertEquals(ints[k].intValue(), ev);
                Assert.assertEquals(k, ind);

            }
            Iterator<Integer> it = set.iterator();
            while (it.hasNext()) {
                Integer next = it.next();
                s.remove(next);
                ((IndexedTreeSet) s).debug();
            }
        }
        log.debug("DONE IN:" + (System.currentTimeMillis() - t1));
    }

    // Although JMRI doesn't use this kind of serialization,
    // we're leaving the test in place for any downstream uses.
    @Test
    @SuppressWarnings("unchecked") // this is 3rd party code
    public void testPersistence() throws Exception {
        long t1 = System.currentTimeMillis();
        for (int i = 0; i < 1; i++) {
            Set<String> set = new HashSet<String>();
            Random random = new Random(System.currentTimeMillis());
            IndexedNavigableSet<String> m = new IndexedTreeSet<String>();

            while (set.size() < 100000) {
                Integer next = random.nextInt();
                if (!set.contains(String.valueOf(next))) {
                    set.add(String.valueOf(next));
                    m.add(String.valueOf(next));
//                    ((IndexedTreeSet) m).debug();
                }
            }

            log.debug("adding:" + (System.currentTimeMillis() - t1));
            t1 = System.currentTimeMillis();

            int hash = System.identityHashCode(m);
            
            File f = folder.newFile();  // temporary
            
            store(f, m);
            m = (IndexedTreeSet<String>) load(f);  // unchecked conversion here
            Assert.assertNotSame(hash,System.identityHashCode(m));

            log.debug("saving - restoring:" + (System.currentTimeMillis() - t1));

            String[] ints = set.toArray(new String[set.size()]);
            Arrays.sort(ints);

            t1 = System.currentTimeMillis();

            for (int k = 0; k < ints.length; k++) {
                String ek = m.exact(k);
                int ind = m.entryIndex(ek);

                Assert.assertEquals(ints[k], ek);
                Assert.assertEquals(k, ind);

            }
            Iterator<String> it = set.iterator();
            while (it.hasNext()) {
                String next = it.next();
                m.remove(next);
                Assert.assertEquals(false, m.contains(next));
//                ((IndexedTreeSet) m).debug();
            }
            Assert.assertEquals(0, m.size());
        }

        log.debug("checking - removing:" + (System.currentTimeMillis() - t1));
    }


    public boolean store(File f, Object o) {
        try {
            FileOutputStream fos = new FileOutputStream(f);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(o);
            oos.close();
            fos.close();
        } catch (IOException ex) {
            return false;
        }
        return true;
    }

    public Object load(File f) {
        try {
            FileInputStream fis = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Object o = ois.readObject();
            ois.close();
            fis.close();
            return o;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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
