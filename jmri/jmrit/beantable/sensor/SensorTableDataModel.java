// SensorTableDataModel.java

package jmri.jmrit.beantable.sensor;

import jmri.*;

import jmri.jmrit.beantable.BeanTableDataModel;

import javax.swing.*;
import java.util.ResourceBundle;

/**
 * Data model for a SensorTable
 *
 * @author	Bob Jacobsen    Copyright (C) 2003, 2009
 * @version     $Revision: 1.3 $
 */

public class SensorTableDataModel extends BeanTableDataModel {

    static public final int INVERTCOL = NUMCOLUMN;
    
    public String getValue(String name) {
        int val = InstanceManager.sensorManagerInstance().getBySystemName(name).getKnownState();
        switch (val) {
        case Sensor.ACTIVE: return rbean.getString("SensorStateActive");
        case Sensor.INACTIVE: return rbean.getString("SensorStateInactive");
        case Sensor.UNKNOWN: return rbean.getString("BeanStateUnknown");
        case Sensor.INCONSISTENT: return rbean.getString("BeanStateInconsistent");
        default: return "Unexpected value: "+val;
        }
    }
    protected Manager getManager() { return InstanceManager.sensorManagerInstance(); }
    protected NamedBean getBySystemName(String name) { return InstanceManager.sensorManagerInstance().getBySystemName(name);}
    protected NamedBean getByUserName(String name) { return InstanceManager.sensorManagerInstance().getByUserName(name);}
    protected int getDisplayDeleteMsg() { return InstanceManager.getDefault(jmri.UserPreferencesManager.class).getWarnSensorInUse(); }
    protected void setDisplayDeleteMsg(int boo) { InstanceManager.getDefault(jmri.UserPreferencesManager.class).setWarnSensorInUse(boo); }
    protected void clickOn(NamedBean t) {
        try {
            int state = ((Sensor)t).getKnownState();
            if (state==Sensor.INACTIVE) ((Sensor)t).setKnownState(Sensor.ACTIVE);
            else ((Sensor)t).setKnownState(Sensor.INACTIVE);
        } catch (JmriException e) { log.warn("Error setting state: "+e); }
    }

    public int getColumnCount( ){ 
        return NUMCOLUMN+1;
    }

    public String getColumnName(int col) {
        if (col==INVERTCOL) return "Inverted";
        else return super.getColumnName(col);
    }
    public Class<?> getColumnClass(int col) {
        if (col==INVERTCOL) return Boolean.class;
        else return super.getColumnClass(col);
    }
    public int getPreferredWidth(int col) {
        if (col==INVERTCOL) return new JTextField(4).getPreferredSize().width;
        else return super.getPreferredWidth(col);
    }
    public boolean isCellEditable(int row, int col) {
        if (col==INVERTCOL) return true;
        else return super.isCellEditable(row,col);
    }    		

    public Object getValueAt(int row, int col) {
        if (col==INVERTCOL) {
            // some error checking
            if (row >= sysNameList.size()){
                log.debug("row is greater than name list");
                return "";
            }
            String name = sysNameList.get(row);
            boolean val = InstanceManager.sensorManagerInstance().getBySystemName(name).getInverted();
            return Boolean.valueOf(val);
        } else return super.getValueAt(row, col);
    }    		
    
    public void setValueAt(Object value, int row, int col) {
        if (col==INVERTCOL) {
            String name = sysNameList.get(row);
            boolean b = ((Boolean)value).booleanValue();
            InstanceManager.sensorManagerInstance().getBySystemName(name).setInverted(b);
        } else super.setValueAt(value, row, col);
    }
    
    protected boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().indexOf("inverted")>=0) return true;
        else return super.matchPropertyName(e);
    }

    static final ResourceBundle rbean = ResourceBundle.getBundle("jmri.NamedBeanBundle");
    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SensorTableDataModel.class.getName());
}

/* @(#)SensorTableDataModel.java */
