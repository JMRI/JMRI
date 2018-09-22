package jmri.jmrit.symbolicprog;

import java.util.Comparator;
import jmri.util.AlphanumComparator;

/*
 * Compare CV names.
 * <p>
 * <ul>
 * <li>Groups are separated by periods. If there are 
 *     different numbers of groups, more is later
 * <li>Each group is ordered, left group first, by an AlphanumComparator
 *
 */
public class CVNameComparator extends AlphanumComparator {

    @Override
    public int compare(String s1, String s2) {
        if (s1.indexOf('.')>=0 || s2.indexOf('.')>=0) {
            String ts1 = s1;
            String ts2 = s2;
            while (!ts1.isEmpty() && !ts2.isEmpty() ) {
                int index1 = ts1.indexOf('.');
                int index2 = ts2.indexOf('.');

                // depending on how many dots:
                if (index1<0 && index2<0) return super.compare(ts1, ts2);
                if (index1<0) return -1;
                if (index2<0) return +1;

                // now extract chunks
                String c1 = "";
                if (index1 > 0) {
                    c1 = ts1.substring(0, index1);
                    ts1 = ts1.substring(index1+1, ts1.length());
                } else {
                    c1 = ts1;
                    ts1 = "";
                }

                String c2 = "";
                if (index2 > 0) {
                    c2 = ts2.substring(0, index2);
                    ts2 = ts2.substring(index2+1, ts2.length());
                } else {
                    c2 = ts2;
                    ts2 = "";
                }

                if (c1.isEmpty() && c2.isEmpty()) return 0;
                if (c1.isEmpty()) return +1;
                if (c2.isEmpty()) return -1;
                
                int retval = super.compare(c1, c2);
                if (retval != 0) return retval;
            }
        }
        return super.compare(s1, s2);
    }
}
