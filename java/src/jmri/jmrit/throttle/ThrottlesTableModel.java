package jmri.jmrit.throttle;

import java.util.*;

import javax.swing.table.AbstractTableModel;

import jmri.ConsistListListener;
import jmri.DccLocoAddress;
import jmri.Throttle;

/**
 * A TableModel to display active Throttles in a summary table
 * (see ThrottlesListPanel)
 *
 * @author Lionel Jeanson - 2011
 *
 */

public class ThrottlesTableModel extends AbstractTableModel implements java.beans.PropertyChangeListener, ConsistListListener{

    private final List<ThrottleFrame> throttleFrames = new LinkedList<>();

    @Override
    public int getRowCount() {
        return throttleFrames.size();
    }

    @Override
    public int getColumnCount() {
        return 1;
    }

    @Override
    public Object getValueAt(int i, int i1) {
        return throttleFrames.get(i);
    }

    public Iterator<ThrottleFrame> iterator() {
        return throttleFrames.iterator();
    }

    public void addThrottleFrame(ThrottleWindow tw, ThrottleFrame ntf) {
        int loc = -1;
        int idx = 0;
        // insert it after the last one from its containing throttle window
        for (ThrottleFrame tf: throttleFrames) {
            if ( tf.getThrottleWindow() == tw) {
                loc = idx;
            }
            idx++;
        }
        if (loc != -1) {
            throttleFrames.add(loc+1, ntf);
        } else {
            throttleFrames.add(ntf);
        }        
        fireTableDataChanged();
    }

    public void removeThrottleFrame(ThrottleFrame tf, DccLocoAddress la) {
        throttleFrames.remove(tf);
        fireTableDataChanged();
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if ((e.getPropertyName().equals(Throttle.SPEEDSETTING)) ||
                (e.getPropertyName().equals(Throttle.SPEEDSTEPS)) ||
                (e.getPropertyName().equals(Throttle.ISFORWARD)) ||
                (e.getPropertyName().equals("ThrottleFrame"))) {
            fireTableDataChanged();
        }
    }

    @Override
    public void notifyConsistListChanged() {
        fireTableDataChanged();
    }

}
