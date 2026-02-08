package jmri.jmrit.throttle;

import java.util.*;

import javax.annotation.CheckForNull;
import javax.swing.table.AbstractTableModel;

import jmri.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A TableModel to display active Throttles in a summary table
 * (see ThrottlesListPanel)
 *
 * @author Lionel Jeanson - 2011
 *
 */

public class ThrottlesTableModel extends AbstractTableModel implements java.beans.PropertyChangeListener, ConsistListListener{
    
    // The JMRI Swing throttles manager
    private final ThrottleControlersContainersManager throttleFrameManager = InstanceManager.getDefault(ThrottleFrameManager.class);

    @Override
    public int getRowCount() {
        int max = 0;
        Iterator<ThrottleControlersContainer> twi = throttleFrameManager.iterator();
        while (twi.hasNext()) {
            max = Math.max(max, twi.next().getNbThrottlesControlers());
        }
        return max;
    }

    @Override
    public int getColumnCount() {
        return throttleFrameManager.getNbThrottleControlersContainers();        
    }

    @Override
    public ThrottleControler getValueAt(int row_tf, int col_tw) {
        ThrottleControlersContainer tw = throttleFrameManager.getThrottleControlersContainerAt(col_tw);
        if (tw == null) {
            return null;
        }
        //log.debug("{} selected", tw.getThrottleFrameAt(row_tf).getTitle());
        return tw.getThrottleControlerAt(row_tf);
    }
    
    public void moveThrottleControler(ThrottleControler tf, int row_tf, int col_tw ) {
        tf.getThrottleControlersContainer().removeThrottleControler(tf);
        tf.setThrottleControlersContainer(throttleFrameManager.getThrottleControlersContainerAt(col_tw));
        throttleFrameManager.getThrottleControlersContainerAt(col_tw).addThrottleControlerAt(tf, row_tf);        
        fireTableStructureChanged();
    }
    
    public ThrottleControlersContainer getThrottleControlersContainerAt( int col_tw) {
        return throttleFrameManager.getThrottleControlersContainerAt(col_tw);
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if ((e.getPropertyName().equals(Throttle.SPEEDSETTING)) ||
                (e.getPropertyName().equals(Throttle.SPEEDSTEPS)) ||
                (e.getPropertyName().equals(Throttle.ISFORWARD))) {
            fireTableDataChanged();
        }
        if (e.getPropertyName().equals("ThrottleFrame")) {
            fireTableStructureChanged();
        }
    }

    @Override
    public void notifyConsistListChanged() {
        fireTableDataChanged();
    }

    private final static Logger log = LoggerFactory.getLogger(ThrottlesTableModel.class);

}
