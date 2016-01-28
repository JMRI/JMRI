// SensorTableDataModel.java
package jmri.jmrit.beantable.sensor;

import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableColumn;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Manager;
import jmri.NamedBean;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrit.beantable.BeanTableDataModel;
import jmri.util.swing.XTableColumnModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data model for a SensorTable
 *
 * @author	Bob Jacobsen Copyright (C) 2003, 2009
 * @version $Revision$
 */
public class SensorTableDataModel extends BeanTableDataModel {

    /**
     *
     */
    private static final long serialVersionUID = 9025503488977960491L;
    static public final int INVERTCOL = NUMCOLUMN;
    static public final int EDITCOL = INVERTCOL + 1;
    static public final int USEGLOBALDELAY = EDITCOL + 1;
    static public final int ACTIVEDELAY = USEGLOBALDELAY + 1;
    static public final int INACTIVEDELAY = ACTIVEDELAY + 1;

    SensorManager senManager = InstanceManager.sensorManagerInstance();

    public SensorTableDataModel() {
        super();
    }

    public SensorTableDataModel(SensorManager manager) {
        super();
        getManager().removePropertyChangeListener(this);
        if (sysNameList != null) {
            for (int i = 0; i < sysNameList.size(); i++) {
                // if object has been deleted, it's not here; ignore it
                NamedBean b = getBySystemName(sysNameList.get(i));
                if (b != null) {
                    b.removePropertyChangeListener(this);
                }
            }
        }
        senManager = manager;
        getManager().addPropertyChangeListener(this);
        updateNameList();
    }

    public String getValue(String name) {
        int val = senManager.getBySystemName(name).getKnownState();
        switch (val) {
            case Sensor.ACTIVE:
                return Bundle.getMessage("SensorStateActive");
            case Sensor.INACTIVE:
                return Bundle.getMessage("SensorStateInactive");
            case Sensor.UNKNOWN:
                return Bundle.getMessage("BeanStateUnknown");
            case Sensor.INCONSISTENT:
                return Bundle.getMessage("BeanStateInconsistent");
            default:
                return "Unexpected value: " + val;
        }
    }

    protected void setManager(SensorManager manager) {
        getManager().removePropertyChangeListener(this);
        if (sysNameList != null) {
            for (int i = 0; i < sysNameList.size(); i++) {
                // if object has been deleted, it's not here; ignore it
                NamedBean b = getBySystemName(sysNameList.get(i));
                if (b != null) {
                    b.removePropertyChangeListener(this);
                }
            }
        }
        senManager = manager;
        getManager().addPropertyChangeListener(this);
        updateNameList();
    }

    protected Manager getManager() {
        if (senManager == null) {
            senManager = InstanceManager.sensorManagerInstance();
        }
        return senManager;
    }

    protected NamedBean getBySystemName(String name) {
        return senManager.getBySystemName(name);
    }

    protected NamedBean getByUserName(String name) {
        return senManager.getByUserName(name);
    }

    protected String getMasterClassName() {
        return getClassName();
    }

    protected void clickOn(NamedBean t) {
        try {
            int state = ((Sensor) t).getKnownState();
            if (state == Sensor.INACTIVE) {
                ((Sensor) t).setKnownState(Sensor.ACTIVE);
            } else {
                ((Sensor) t).setKnownState(Sensor.INACTIVE);
            }
        } catch (JmriException e) {
            log.warn("Error setting state: " + e);
        }
    }

    public int getColumnCount() {
        return INACTIVEDELAY + 1;
    }

    public String getColumnName(int col) {
        if (col == INVERTCOL) {
            return Bundle.getMessage("Inverted");
        }
        if (col == EDITCOL) {
            return "";
        }
        if (col == USEGLOBALDELAY) {
            return Bundle.getMessage("SensorUseGlobalDebounce");
        }
        if (col == ACTIVEDELAY) {
            return Bundle.getMessage("SensorActiveDebounce");
        }
        if (col == INACTIVEDELAY) {
            return Bundle.getMessage("SensorInActiveDebounce");
        } else {
            return super.getColumnName(col);
        }
    }

    public Class<?> getColumnClass(int col) {
        if (col == INVERTCOL) {
            return Boolean.class;
        }
        if (col == EDITCOL) {
            return JButton.class;
        }
        if (col == USEGLOBALDELAY) {
            return Boolean.class;
        }
        if (col == ACTIVEDELAY) {
            return String.class;
        }
        if (col == INACTIVEDELAY) {
            return String.class;
        } else {
            return super.getColumnClass(col);
        }
    }

    public int getPreferredWidth(int col) {
        if (col == INVERTCOL) {
            return new JTextField(4).getPreferredSize().width;
        }
        if (col == USEGLOBALDELAY || col == ACTIVEDELAY || col == INACTIVEDELAY) {
            return new JTextField(8).getPreferredSize().width;
        }
        if (col == EDITCOL) {
            return new JTextField(7).getPreferredSize().width;
        } else {
            return super.getPreferredWidth(col);
        }
    }

    public boolean isCellEditable(int row, int col) {
        if (col == INVERTCOL || col == EDITCOL) {
            return true;
        }
        if (col == USEGLOBALDELAY) {
            return true;
        }
        //Need to do something here to make it disable 
        if (col == ACTIVEDELAY || col == INACTIVEDELAY) {
            String name = sysNameList.get(row);
            if (senManager.getBySystemName(name).useDefaultTimerSettings()) {
                return false;
            } else {
                return true;
            }
        } else {
            return super.isCellEditable(row, col);
        }
    }

    public Object getValueAt(int row, int col) {
        if (row >= sysNameList.size()) {
            log.debug("row is greater than name list");
            return "";
        }
        String name = sysNameList.get(row);
        Sensor s = senManager.getBySystemName(name);
        if (s == null) {
            log.debug("error null sensor!");
            return "error";
        }
        if (col == INVERTCOL) {
            boolean val = s.getInverted();
            return Boolean.valueOf(val);
        } else if (col == USEGLOBALDELAY) {
            boolean val = s.useDefaultTimerSettings();
            return Boolean.valueOf(val);
        } else if (col == ACTIVEDELAY) {
            return s.getSensorDebounceGoingActiveTimer();
        } else if (col == INACTIVEDELAY) {
            return s.getSensorDebounceGoingInActiveTimer();
        } else if (col == EDITCOL) {
            return Bundle.getMessage("ButtonEdit");
        } else {
            return super.getValueAt(row, col);
        }
    }

    public void setValueAt(Object value, int row, int col) {
        if (row >= sysNameList.size()) {
            log.debug("row is greater than name list");
            return;
        }
        String name = sysNameList.get(row);
        Sensor s = senManager.getBySystemName(name);
        if (s == null) {
            log.debug("error null sensor!");
            return;
        }
        if (col == INVERTCOL) {
            boolean b = ((Boolean) value).booleanValue();
            s.setInverted(b);
        } else if (col == USEGLOBALDELAY) {
            boolean b = ((Boolean) value).booleanValue();
            s.useDefaultTimerSettings(b);
        } else if (col == ACTIVEDELAY) {
            String val = (String) value;
            long goingActive = Long.valueOf(val);
            s.setSensorDebounceGoingActiveTimer(goingActive);
        } else if (col == INACTIVEDELAY) {
            String val = (String) value;
            long goingInActive = Long.valueOf(val);
            s.setSensorDebounceGoingInActiveTimer(goingInActive);
        } else if (col == EDITCOL) {
            class WindowMaker implements Runnable {

                Sensor s;

                WindowMaker(Sensor s) {
                    this.s = s;
                }

                public void run() {
                    editButton(s);
                }
            }
            WindowMaker w = new WindowMaker(s);
            javax.swing.SwingUtilities.invokeLater(w);
        } else {
            super.setValueAt(value, row, col);
        }
    }

    protected String getBeanType() {
        return Bundle.getMessage("BeanNameSensor");
    }

    protected boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
        if ((e.getPropertyName().indexOf("inverted") >= 0) || (e.getPropertyName().indexOf("GlobalTimer") >= 0)
                || (e.getPropertyName().indexOf("ActiveTimer") >= 0) || (e.getPropertyName().indexOf("InActiveTimer") >= 0)) {
            return true;
        } else {
            return super.matchPropertyName(e);
        }
    }

    @Override
    public void configureTable(JTable table) {
        this.table = table;
        showDebounce(false);
        super.configureTable(table);
    }

    protected JTable table;

    void editButton(Sensor s) {
        jmri.jmrit.beantable.beanedit.SensorEditAction beanEdit = new jmri.jmrit.beantable.beanedit.SensorEditAction();
        beanEdit.setBean(s);
        beanEdit.actionPerformed(null);
    }

    public void showDebounce(boolean show) {
        XTableColumnModel columnModel = (XTableColumnModel) table.getColumnModel();
        TableColumn column = columnModel.getColumnByModelIndex(USEGLOBALDELAY);
        columnModel.setColumnVisible(column, show);
        column = columnModel.getColumnByModelIndex(ACTIVEDELAY);
        columnModel.setColumnVisible(column, show);
        column = columnModel.getColumnByModelIndex(INACTIVEDELAY);
        columnModel.setColumnVisible(column, show);
    }

    protected String getClassName() {
        return jmri.jmrit.beantable.SensorTableAction.class.getName();
    }

    public static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.beantable.BeanTableBundle");

    public String getClassDescription() {
        return Bundle.getMessage("TitleSensorTable");
    }

    static final Logger log = LoggerFactory.getLogger(SensorTableDataModel.class.getName());
}

/* @(#)SensorTableDataModel.java */
