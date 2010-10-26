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
 * @version     $Revision: 1.5 $
 */

public class SignalMastTableDataModel extends BeanTableDataModel {

    static public final int LITCOL = NUMCOLUMN;
    static public final int HELDCOL = LITCOL+1;

    public String getValue(String name) {
        return InstanceManager.signalMastManagerInstance().getBySystemName(name).getAspect();
    }

    public int getColumnCount( ){ return NUMCOLUMN+2;}
    public String getColumnName(int col) {
        if (col==VALUECOL) return "Aspect";
        else if (col==LITCOL) return "Lit";
        else if (col==HELDCOL) return "Held";
        else return super.getColumnName(col);
    }
    public Class<?> getColumnClass(int col) {
        if (col==VALUECOL) return JComboBox.class;
        else if (col==LITCOL) return Boolean.class;
        else if (col==HELDCOL) return Boolean.class;
        else return super.getColumnClass(col);
    }
    public int getPreferredWidth(int col) {
        if (col==LITCOL) return new JTextField(4).getPreferredSize().width;
        else if (col==HELDCOL) return new JTextField(4).getPreferredSize().width;
        else return super.getPreferredWidth(col);
    }
    public boolean isCellEditable(int row, int col) {
        if (col==LITCOL) return true;
        else if (col==HELDCOL) return true;
        else return super.isCellEditable(row,col);
    }

    protected Manager getManager() { return InstanceManager.signalMastManagerInstance(); }
    protected NamedBean getBySystemName(String name) { return InstanceManager.signalMastManagerInstance().getBySystemName(name);}
    protected NamedBean getByUserName(String name) { return InstanceManager.signalMastManagerInstance().getByUserName(name);}
    public int getDisplayDeleteMsg() { return InstanceManager.getDefault(jmri.UserPreferencesManager.class).getWarnSignalMastInUse(); }
    public void setDisplayDeleteMsg(int boo) { InstanceManager.getDefault(jmri.UserPreferencesManager.class).setWarnSignalMastInUse(boo); }
    
    protected void clickOn(NamedBean t) {
//         try {
//             int state = ((Sensor)t).getKnownState();
//             if (state==Sensor.INACTIVE) ((Sensor)t).setKnownState(Sensor.ACTIVE);
//             else ((Sensor)t).setKnownState(Sensor.INACTIVE);
//         } catch (JmriException e) { log.warn("Error setting state: "+e); }
    }

    public Object getValueAt(int row, int col) {
        // some error checking
        if (row >= sysNameList.size()){
            log.debug("row is greater than name list");
            return "error";
        }
        String name = sysNameList.get(row);
        SignalMast s = InstanceManager.signalMastManagerInstance().getBySystemName(name);
        if (s==null) return Boolean.valueOf(false); // if due to race condition, the device is going away
        if (col==LITCOL) {
            boolean val = s.getLit();
            return Boolean.valueOf(val);
        }
        else if (col==HELDCOL) {
            boolean val = s.getHeld();
            return Boolean.valueOf(val);
        }
        else return super.getValueAt(row, col);
    }
 
    public void setValueAt(Object value, int row, int col) {
        String name = sysNameList.get(row);
        SignalMast s = InstanceManager.signalMastManagerInstance().getBySystemName(name);
        if (s==null) return;  // device is going away anyway

        if (col==VALUECOL) {
            s.setAspect((String)value);
            fireTableRowsUpdated(row, row);
        } 
        else if (col==LITCOL) {
            boolean b = ((Boolean)value).booleanValue();
            s.setLit(b);
        }
        else if (col==HELDCOL) {
            boolean b = ((Boolean)value).booleanValue();
            s.setHeld(b);
        }
        else super.setValueAt(value, row, col);
    }
    
    protected boolean matchPropertyName(java.beans.PropertyChangeEvent e) { return true; }
    
    static final ResourceBundle rbean = ResourceBundle.getBundle("jmri.NamedBeanBundle");
    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SignalMastTableDataModel.class.getName());

}

/* @(#)SignalMastTableDataModel.java */
