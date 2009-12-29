// SignalMastTableDataModel.java

package jmri.jmrit.beantable.signalmast;

import jmri.*;

import jmri.jmrit.beantable.BeanTableDataModel;

import javax.swing.*;
import java.util.*;

/**
 * Data model for a SignalMastTable
 *
 * @author	Bob Jacobsen    Copyright (C) 2003, 2009
 * @version     $Revision: 1.3 $
 */

public class SignalMastTableDataModel extends BeanTableDataModel {

    public String getValue(String name) {
        return InstanceManager.signalMastManagerInstance().getBySystemName(name).getAspect();
    }
    protected Manager getManager() { return InstanceManager.signalMastManagerInstance(); }
    protected NamedBean getBySystemName(String name) { return InstanceManager.signalMastManagerInstance().getBySystemName(name);}
    protected NamedBean getByUserName(String name) { return InstanceManager.signalMastManagerInstance().getByUserName(name);}
    protected void clickOn(NamedBean t) {
//         try {
//             int state = ((Sensor)t).getKnownState();
//             if (state==Sensor.INACTIVE) ((Sensor)t).setKnownState(Sensor.ACTIVE);
//             else ((Sensor)t).setKnownState(Sensor.INACTIVE);
//         } catch (JmriException e) { log.warn("Error setting state: "+e); }
    }

    public Object getValueAt(int row, int col) {
        return super.getValueAt(row, col);
    }    		
 
    public void setValueAt(Object value, int row, int col) {
        if (col==VALUECOL) {
            NamedBean t = getBySystemName(sysNameList.get(row));
            ((SignalMast)t).setAspect((String)value);
            fireTableRowsUpdated(row, row);
        } else super.setValueAt(value, row, col);
    }
    
    protected boolean matchPropertyName(java.beans.PropertyChangeEvent e) { return true; }
    
    public String getColumnName(int col) {
        if (col==VALUECOL) return "Aspect";
        else return super.getColumnName(col);
    }

    public Class<?> getColumnClass(int col) {
        if (col==VALUECOL) return JComboBox.class;
        else return super.getColumnClass(col);
    }

    static final ResourceBundle rbean = ResourceBundle.getBundle("jmri.NamedBeanBundle");
    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SignalMastTableDataModel.class.getName());

}

/* @(#)SignalMastTableDataModel.java */
