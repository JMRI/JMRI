package jmri.jmrit.throttle;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import javax.swing.table.AbstractTableModel;
import jmri.DccLocoAddress;
import jmri.Throttle;

/**
 * A TableModel to display active Throttles in a summary table
 * (see ThrottlesListPanel)
 *
 * @author Lionel Jeanson - 2011
 *
 */

public class ThrottlesTableModel extends AbstractTableModel implements java.beans.PropertyChangeListener {

    private final List<ThrottleFrame> throttleFrames = new ArrayList<>(5);

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

    public void addThrottleFrame(ThrottleFrame tf) {
        throttleFrames.add(tf);
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
                (e.getPropertyName().equals(Throttle.ISFORWARD))) {
            fireTableDataChanged();
        }
    }

}
