package jmri.jmrit.throttle.list;

import java.util.*;

import javax.swing.table.AbstractTableModel;

import jmri.*;
import jmri.jmrit.throttle.ThrottleFrameManager;
import jmri.jmrit.throttle.interfaces.ThrottleControllerUI;
import jmri.jmrit.throttle.interfaces.ThrottleControllersUIContainer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A TableModel to display active Throttles in a summary table
 * (see ThrottlesListPanel)
 *
 * @author Lionel Jeanson - 2011
 *
 */

public class ThrottlesTableModel extends AbstractTableModel implements java.beans.PropertyChangeListener, ConsistListListener {
    
    // The JMRI Swing throttles manager
    private final ThrottleFrameManager throttleFrameManager = InstanceManager.getDefault(ThrottleFrameManager.class);

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
    
    public boolean moveThrottleController(ThrottleControllerUI tf, int row_tf, int col_tw ) {
        ThrottleControllersUIContainer prevContainer = tf.getThrottleControllersContainer();
        try {
            tf.setThrottleControllersContainer(throttleFrameManager.getThrottleControllersContainerAt(col_tw));
        } catch ( IllegalArgumentException e) {
            log.warn("Unable to move throttle frame, provided container is not an instance of ThrottleWindow, cancelling move.");
            return false;
        }
        throttleFrameManager.getThrottleControllersContainerAt(col_tw).addThrottleControllerAt(tf, row_tf);        
        prevContainer.removeThrottleController(tf);                
        fireTableStructureChanged();
        return true;
    }
    
    public ThrottleControllersUIContainer getThrottleControllersContainerAt( int col_tw) {
        return throttleFrameManager.getThrottleControllersContainerAt(col_tw);
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
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

    private static final Logger log = LoggerFactory.getLogger(ThrottlesTableModel.class);
}
