package jmri.util.com.dictiography.collections;

import junit.framework.TestCase;

import java.io.*;
import java.util.*;

/**
 * User: Vitaly Sazanovich
 * Date: 2/10/13
 * Time: 12:14 PM
 */
public class IndexedTreeSetTest  extends TestCase {

    public void testQuickCheck() {
        IndexedNavigableSet<String> s = new IndexedTreeSet<String>();
        s.add("Z");
        s.add("A");
        s.add("B");
        assertEquals("A", s.exact(0));
        assertEquals("B", s.exact(1));
        assertEquals("Z", s.exact(2));    
    }
    
    public void testQuickComparator() {
        IndexedNavigableSet<String> s = new IndexedTreeSet<String>(new java.util.Comparator<String>(){
            public int compare(String e1, String e2) { return - e1.toString().compareTo(e2.toString()); } // note minus sign
        });
        s.add("Z");
        s.add("A");
        s.add("B");
        assertEquals("Z", s.exact(0));    
        assertEquals("B", s.exact(1));
        assertEquals("A", s.exact(2));
    }
    
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

                assertEquals(ints[k].intValue(), ev);
                assertEquals(k, ind);

            }
            Iterator<Integer> it = set.iterator();
            while (it.hasNext()) {
                Integer next = it.next();
                s.remove(next);
                ((IndexedTreeSet) s).debug();
            }
        }
        System.out.println("DONE IN:" + (System.currentTimeMillis() - t1));
    }

    @SuppressWarnings("unchecked") // this is 3rd party code
    public void testPersistence() throws Exception {
        long t1 = System.currentTimeMillis();
        for (int i = 0; i < 1; i++) {
            Set<String> set = new HashSet<String>();
            Random random = new Random(System.currentTimeMillis());
            IndexedNavigableSet<String> m = new IndexedTreeSet<String>();

            while (set.size() < 100000) {
                Integer next = random.nextInt();
                if (!set.contains(next)) {
                    set.add(String.valueOf(next));
                    m.add(String.valueOf(next));
//                    ((IndexedTreeSet) m).debug();
                }
            }

            System.out.println("adding:" + (System.currentTimeMillis() - t1));
            t1 = System.currentTimeMillis();

            int hash = System.identityHashCode(m);
            File f = new File("tmp.ser");
            if (f.exists()){
                f.delete();
            }
            store(f, m);
            m = (IndexedTreeSet<String>) load(f);  // unchecked conversion here
            assertNotSame(hash,System.identityHashCode(m));

            System.out.println("saving - restoring:" + (System.currentTimeMillis() - t1));


            String[] ints = set.toArray(new String[set.size()]);
            Arrays.sort(ints);

            t1 = System.currentTimeMillis();

            for (int k = 0; k < ints.length; k++) {
                String ek = m.exact(k);
                int ind = m.entryIndex(ek);

                assertEquals(ints[k], ek);
                assertEquals(k, ind);

            }
            Iterator<String> it = set.iterator();
            while (it.hasNext()) {
                String next = it.next();
                m.remove(next);
                assertEquals(false, m.contains(next));
//                ((IndexedTreeSet) m).debug();
            }
            assertEquals(0, m.size());
        }

        System.out.println("checking - removing:" + (System.currentTimeMillis() - t1));
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
}
