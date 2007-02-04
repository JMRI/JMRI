// SensorTableModel.java

package jmri.jmrit.sensorgroup;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.Sensor;

/**
 * Model for a simple Sensor JTable
 *
 * @author Bob Jacobsen Copyright (C) 2007 
 *
 * @version     $Revision: 1.1 $
 */

public class SensorTableModel extends BeanTableModel
{

    public Manager getManager() {
         return InstanceManager.sensorManagerInstance();
    }

    public Object getValueAt (int r,int c) {
        switch (c) {
        case INCLUDE_COLUMN:  // expect to be overriden
                return Boolean.FALSE;
        case UNAME_COLUMN: 
            String sName = (String)getManager().getSystemNameList().get(r);
            return InstanceManager.sensorManagerInstance().provideSensor(sName).getUserName();
        default:
            return super.getValueAt(r,c);
        }
    }

    public void setValueAt(Object type,int r,int c) {
        switch (c) {
            case INCLUDE_COLUMN:  
                return;
            default:
                log.warn("default hit in setValueAt");
        }
    }
    static final org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SensorTableModel.class.getName());

}
/* @(#)SensorTableModel.java */
