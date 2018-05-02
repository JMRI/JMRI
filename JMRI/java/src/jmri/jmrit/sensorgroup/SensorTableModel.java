package jmri.jmrit.sensorgroup;

import java.beans.PropertyChangeListener;
import jmri.InstanceManager;
import jmri.Manager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Model for a simple Sensor JTable
 *
 * @author Bob Jacobsen Copyright (C) 2007
 * @author Pete Cressman Copyright (C) 2009
 *
 */
public class SensorTableModel extends BeanTableModel implements PropertyChangeListener {

    String[] _sysNameList;
    Boolean[] _includedSensors;

    public SensorTableModel() {
        init();
        getManager().addPropertyChangeListener(this);
    }

    private void init() {
        _sysNameList = getManager().getSystemNameArray();

        _includedSensors = new Boolean[_sysNameList.length];
        for (int i = 0; i < _sysNameList.length; i++) {
            _includedSensors[i] = Boolean.FALSE;
        }
    }

    public void dispose() {
        getManager().removePropertyChangeListener(this);
    }

    @Override
    public Manager getManager() {
        return InstanceManager.sensorManagerInstance();
    }

    @Override
    public int getRowCount() {
        return _sysNameList.length;
    }

    @Override
    public Object getValueAt(int r, int c) {
        if (r >= _sysNameList.length) {
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
                return super.getValueAt(r, c);
        }
    }

    @Override
    public void setValueAt(Object type, int r, int c) {
        if (r > _sysNameList.length) {
            return;
        }
        switch (c) {
            case INCLUDE_COLUMN:
                _includedSensors[r] = (Boolean) type;
                return;
            default:
                log.warn("default hit in setValueAt");
        }
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("length")) {
            // a new NamedBean is available in the manager
            init();
            fireTableDataChanged();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SensorTableModel.class);

}
