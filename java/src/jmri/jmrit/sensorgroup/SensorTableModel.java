// SensorTableModel.java

package jmri.jmrit.sensorgroup;

import org.apache.log4j.Logger;
import jmri.InstanceManager;
import jmri.Manager;

import java.beans.PropertyChangeListener;
/**
 * Model for a simple Sensor JTable
 *
 * @author Bob Jacobsen Copyright (C) 2007 
 * @author Pete Cressman Copyright (C) 2009
 *
 * @version     $Revision$
 */

public class SensorTableModel extends BeanTableModel implements PropertyChangeListener
{
    String[] _sysNameList;
    Boolean[] _includedSensors;

    public SensorTableModel() {
        init();
        getManager().addPropertyChangeListener(this);
    }

    private void init() {
        _sysNameList = getManager().getSystemNameArray();

        _includedSensors = new Boolean[_sysNameList.length];
        for (int i = 0; i<_sysNameList.length; i++)
            _includedSensors[i] = Boolean.FALSE;
    }

    public void dispose() {
        getManager().removePropertyChangeListener(this);
    }

    public Manager getManager() {
         return InstanceManager.sensorManagerInstance();
    }

    public int getRowCount () {
        return _sysNameList.length;
    }

    public Object getValueAt (int r,int c) {
        if (r >=_sysNameList.length) {
            return null;
        }
        switch (c) {
            case INCLUDE_COLUMN:  // expect to be overriden
                return _includedSensors[r];
            case SNAME_COLUMN:
                return _sysNameList[r];
            case UNAME_COLUMN:
                return InstanceManager.sensorManagerInstance().provideSensor(_sysNameList[r]).getUserName();
            default:
                return super.getValueAt(r,c);
        }
    }

    public void setValueAt(Object type,int r,int c) {
        if (r>_sysNameList.length) {
            return;
        }
        switch (c) {
            case INCLUDE_COLUMN:  
                _includedSensors[r] = (Boolean)type;
                return;
            default:
                log.warn("default hit in setValueAt");
        }
    }

    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("length")) {
            // a new NamedBean is available in the manager
            init();
            fireTableDataChanged();
        }
    }

    static final Logger log = Logger.getLogger(SensorTableModel.class.getName());

}
/* @(#)SensorTableModel.java */
