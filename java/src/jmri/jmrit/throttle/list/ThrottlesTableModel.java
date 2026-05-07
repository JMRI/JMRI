package jmri.jmrit.throttle.list;

import java.util.*;

import javax.swing.table.AbstractTableModel;

import jmri.*;
import jmri.jmrit.throttle.SimpleThrottleWindow;
import jmri.jmrit.throttle.ThrottleFrameManager;
import jmri.jmrit.throttle.ThrottleWindow;
import jmri.jmrit.throttle.implementation.SimpleThrottlePanel;
import jmri.jmrit.throttle.implementation.ThrottleFrame;
import jmri.jmrit.throttle.interfaces.ThrottleControllerUI;
import jmri.jmrit.throttle.interfaces.ThrottleControllersUIContainer;

/**
 * A TableModel to display active Throttles in a summary table
 * (see ThrottlesListPanel)
 * 
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Lionel Jeanson - 2011-2026
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
    
    public boolean moveThrottleController(ThrottleFrame tcui, int row_tf, int col_tw ) {
        ThrottleControllersUIContainer destination = throttleFrameManager.getThrottleControllersContainerAt(col_tw);
        ThrottleControllersUIContainer source = tcui.getThrottleControllersContainer();
        source.removeThrottleController(tcui);
        if (destination instanceof ThrottleWindow) {
            tcui.setThrottleControllersContainer(destination);
            destination.addThrottleControllerAt(tcui, row_tf);
        }
        if (destination instanceof SimpleThrottleWindow) {
            // create a new window with the same address as the one provided in parameter
            InstanceManager.getDefault(ThrottleFrameManager.class).createSimpleThrottleFrame(tcui.getAddress());
        }
        fireTableStructureChanged();
        return true;
    }

    public boolean moveThrottleController(SimpleThrottlePanel tcui, int row_tf, int col_tw ) {
        ThrottleControllersUIContainer destination = throttleFrameManager.getThrottleControllersContainerAt(col_tw);
        ThrottleControllersUIContainer source = tcui.getThrottleControllersContainer();
        if (destination instanceof ThrottleWindow) {
            ThrottleFrame tf = new ThrottleFrame( (ThrottleWindow) destination);
            tf.setAddress(tcui.getAddress());            
            destination.addThrottleControllerAt(tf, row_tf);
            source.dispose();
        }
        // nothing to do for destination instanceof SimpleThrottleWindow                       
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

//    private static final Logger log = LoggerFactory.getLogger(ThrottlesTableModel.class);
}
