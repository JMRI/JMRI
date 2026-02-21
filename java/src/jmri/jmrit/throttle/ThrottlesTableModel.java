package jmri.jmrit.throttle;

import java.util.*;

import javax.swing.table.AbstractTableModel;

import jmri.*;


/**
 * A TableModel to display active Throttles in a summary table
 * (see ThrottlesListPanel)
 *
 * @author Lionel Jeanson - 2011
 *
 */

public class ThrottlesTableModel extends AbstractTableModel implements java.beans.PropertyChangeListener, ConsistListListener{
    
    // The JMRI Swing throttles manager
    private final ThrottleControllersUIContainersManager throttleFrameManager = InstanceManager.getDefault(ThrottleFrameManager.class);

    @Override
    public int getRowCount() {
        int max = 0;
        Iterator<ThrottleControllersUIContainer> twi = throttleFrameManager.iterator();
        while (twi.hasNext()) {
            max = Math.max(max, twi.next().getNbThrottlesControllers());
        }
        return max;
    }

    @Override
    public int getColumnCount() {
        return throttleFrameManager.getNbThrottleControllersContainers();        
    }

    @Override
    public ThrottleControllerUI getValueAt(int row_tf, int col_tw) {
        ThrottleControllersUIContainer tw = throttleFrameManager.getThrottleControllersContainerAt(col_tw);
        if (tw == null) {
            return null;
        }
        //log.debug("{} selected", tw.getThrottleFrameAt(row_tf).getTitle());
        return tw.getThrottleControllerAt(row_tf);
    }
    
    public void moveThrottleController(ThrottleControllerUI tf, int row_tf, int col_tw ) {
        tf.getThrottleControllersContainer().removeThrottleController(tf);
        tf.setThrottleControllersContainer(throttleFrameManager.getThrottleControllersContainerAt(col_tw));
        throttleFrameManager.getThrottleControllersContainerAt(col_tw).addThrottleControllerAt(tf, row_tf);        
        fireTableStructureChanged();
    }
    
    public ThrottleControllersUIContainer getThrottleControllersContainerAt( int col_tw) {
        return throttleFrameManager.getThrottleControllersContainerAt(col_tw);
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if ((e.getPropertyName().equals(Throttle.SPEEDSETTING)) ||
                (e.getPropertyName().equals(Throttle.SPEEDSTEPS)) ||
                (e.getPropertyName().equals(Throttle.ISFORWARD))) {
            fireTableDataChanged();
        }
        if (e.getPropertyName().equals("ThrottleFrameChanged")) {
            fireTableDataChanged();
        } else if (e.getPropertyName().startsWith("ThrottleFrame")) {
            fireTableStructureChanged();
        }
    }

    @Override
    public void notifyConsistListChanged() {
        fireTableDataChanged();
    }

}
