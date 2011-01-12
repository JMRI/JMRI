// SensorTableDataModel.java

package jmri.jmrit.beantable.sensor;

import jmri.*;

import jmri.jmrit.beantable.BeanTableDataModel;
import javax.swing.*;
import java.util.ResourceBundle;
import jmri.InstanceManager;

/**
 * Data model for a SensorTable
 *
 * @author	Bob Jacobsen    Copyright (C) 2003, 2009
 * @version     $Revision: 1.6 $
 */

public class SensorTableDataModel extends BeanTableDataModel {

    static public final int INVERTCOL = NUMCOLUMN;

    SensorManager senManager = InstanceManager.sensorManagerInstance();
    public SensorTableDataModel() {
        super();
    }
    
    public SensorTableDataModel(SensorManager manager) {
        super();
        getManager().removePropertyChangeListener(this);
        senManager = manager;
        getManager().addPropertyChangeListener(this);
        updateNameList();
    }
    
    public String getValue(String name) {
        int val = senManager.getBySystemName(name).getKnownState();
        switch (val) {
        case Sensor.ACTIVE: return rbean.getString("SensorStateActive");
        case Sensor.INACTIVE: return rbean.getString("SensorStateInactive");
        case Sensor.UNKNOWN: return rbean.getString("BeanStateUnknown");
        case Sensor.INCONSISTENT: return rbean.getString("BeanStateInconsistent");
        default: return "Unexpected value: "+val;
        }
    }
    protected void setManager(SensorManager manager) { 
        getManager().removePropertyChangeListener(this);
        senManager = manager;
        getManager().addPropertyChangeListener(this);
        updateNameList();
        }
    protected Manager getManager() { 
        if (senManager==null)
            senManager=InstanceManager.sensorManagerInstance();
        return senManager;
    }
    protected NamedBean getBySystemName(String name) { return senManager.getBySystemName(name);}
    protected NamedBean getByUserName(String name) { return senManager.getByUserName(name);}
    /*public int getDisplayDeleteMsg() { return InstanceManager.getDefault(jmri.UserPreferencesManager.class).getMultipleChoiceOption(getClassName(),"deleteInUse"); }
    public void setDisplayDeleteMsg(int boo) { InstanceManager.getDefault(jmri.UserPreferencesManager.class).setMultipleChoiceOption(getClassName(), "deleteInUSe", boo); }*/
    protected String getMasterClassName() { return getClassName(); }
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
            boolean val = senManager.getBySystemName(name).getInverted();
            return Boolean.valueOf(val);
        } else return super.getValueAt(row, col);
    }    		
    
    public void setValueAt(Object value, int row, int col) {
        if (col==INVERTCOL) {
            String name = sysNameList.get(row);
            boolean b = ((Boolean)value).booleanValue();
            senManager.getBySystemName(name).setInverted(b);
        } else super.setValueAt(value, row, col);
    }
    
    protected boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().indexOf("inverted")>=0) return true;
        else return super.matchPropertyName(e);
    }
    
    protected String getClassName() { return jmri.jmrit.beantable.SensorTableAction.class.getName(); }
    
    public static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.beantable.BeanTableBundle");
    public String getClassDescription() { return rb.getString("TitleSensorTable"); }

    static final ResourceBundle rbean = ResourceBundle.getBundle("jmri.NamedBeanBundle");
    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SensorTableDataModel.class.getName());
}

/* @(#)SensorTableDataModel.java */
