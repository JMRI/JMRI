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
 * @version     $Revision: 1.8 $
 */

public class SensorTableDataModel extends BeanTableDataModel {

    static public final int INVERTCOL = NUMCOLUMN;
    static public final int USEGLOBALDELAY = INVERTCOL+1;
    static public final int ACTIVEDELAY = USEGLOBALDELAY+1;
    static public final int INACTIVEDELAY = ACTIVEDELAY+1;

    public boolean showDebounce = false;
    
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

    protected String getMasterClassName() { return getClassName(); }
    protected void clickOn(NamedBean t) {
        try {
            int state = ((Sensor)t).getKnownState();
            if (state==Sensor.INACTIVE) ((Sensor)t).setKnownState(Sensor.ACTIVE);
            else ((Sensor)t).setKnownState(Sensor.INACTIVE);
        } catch (JmriException e) { log.warn("Error setting state: "+e); }
    }

    public int getColumnCount( ){
        if(showDebounce)
            return INACTIVEDELAY+1;
        return NUMCOLUMN+1;
    }

    public String getColumnName(int col) {
        if(col==INVERTCOL) return rb.getString("Inverted");
        if(col==USEGLOBALDELAY) return rb.getString("SensorUseGlobalDebounce");
        if(col==ACTIVEDELAY) return rb.getString("SensorActiveDebounce");
        if(col==INACTIVEDELAY) return rb.getString("SensorInActiveDebounce");
        else return super.getColumnName(col);
    }
    public Class<?> getColumnClass(int col) {
        if (col==INVERTCOL) return Boolean.class;
        if(col==USEGLOBALDELAY) return Boolean.class;
        if(col==ACTIVEDELAY) return String.class;
        if(col==INACTIVEDELAY) return String.class;
        else return super.getColumnClass(col);
    }
    public int getPreferredWidth(int col) {
        if (col==INVERTCOL) return new JTextField(4).getPreferredSize().width;
        if(col==USEGLOBALDELAY) return new JTextField(4).getPreferredSize().width;
        if(col==ACTIVEDELAY) return new JTextField(4).getPreferredSize().width;
        if(col==INACTIVEDELAY) return new JTextField(4).getPreferredSize().width;
        else return super.getPreferredWidth(col);
    }
    public boolean isCellEditable(int row, int col) {
        if (col==INVERTCOL) return true;
        if(col==USEGLOBALDELAY) return true;
        //Need to do something here to make it disable 
        if(col==ACTIVEDELAY||col==INACTIVEDELAY){
            String name = sysNameList.get(row);
            if(senManager.getBySystemName(name).useDefaultTimerSettings())
                return false;
            else
                return true;
        }
        else return super.isCellEditable(row,col);
    }    		

    public Object getValueAt(int row, int col) {
        if (row >= sysNameList.size()){
            log.debug("row is greater than name list");
            return "";
        }
        String name = sysNameList.get(row);
        Sensor s = senManager.getBySystemName(name);
        if (s == null){
            log.debug("error null sensor!");
            return "error";
        }
        if (col==INVERTCOL) {
            boolean val = s.getInverted();
            return Boolean.valueOf(val);
        } 
        else if(col==USEGLOBALDELAY){
            boolean val = s.useDefaultTimerSettings();
            return Boolean.valueOf(val);
        }
        else if(col==ACTIVEDELAY){
            return s.getSensorDebounceGoingActiveTimer();
        }
        else if(col==INACTIVEDELAY){
            return s.getSensorDebounceGoingInActiveTimer();
        }
        else return super.getValueAt(row, col);
    }    		
    
    public void setValueAt(Object value, int row, int col) {
        if (row >= sysNameList.size()){
            log.debug("row is greater than name list");
            return;
        }
        String name = sysNameList.get(row);
        Sensor s = senManager.getBySystemName(name);
        if (s == null){
            log.debug("error null sensor!");
            return;
        }
        if (col==INVERTCOL) {
            boolean b = ((Boolean)value).booleanValue();
            s.setInverted(b);
        } else if(col==USEGLOBALDELAY){
            boolean b = ((Boolean)value).booleanValue();
            s.useDefaultTimerSettings(b);
        }
        else if(col==ACTIVEDELAY){
            String val = (String)value;
            long goingActive = Long.valueOf(val);
            s.setSensorDebounceGoingActiveTimer(goingActive);
        }
        else if(col==INACTIVEDELAY){
            String val = (String)value;
            long goingInActive = Long.valueOf(val);
            s.setSensorDebounceGoingInActiveTimer(goingInActive);
        }
        else super.setValueAt(value, row, col);
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
