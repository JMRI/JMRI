package jmri.jmrit.beantable;

import java.time.LocalDateTime;

import jmri.*;

import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.time.TimeProvider;
import jmri.time.TimeProviderManager;

/**
 * TableDataModel for the Clock Table.
 *
 * Split from {@link TimeProviderTableAction}
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Steve Young Copyright (C) 2021
 */
public class TimeProviderTableDataModel extends BeanTableDataModel<TimeProvider> {

    /**
     * Create a new Time Provider Table Data Model.
     * @param mgr Time provider manager to use in the model, default ClockManager always used.
     */
    public TimeProviderTableDataModel(Manager<TimeProvider> mgr){
        super();
        setManager(mgr);
    }

    /**
     * Constructor for use by preferences and messages system
     */
    public TimeProviderTableDataModel(){
        super();
        setManager(InstanceManager.getDefault(TimeProviderManager.class));
    }

    /** {@inheritDoc} */
    @Override
    public String getValue(String name) {
        TimeProvider clock = InstanceManager.getDefault(TimeProviderManager.class).getBySystemName(name);
        if (clock == null) {
            return "?";
        }
        LocalDateTime time = clock.getTime();
        if (time != null) {
            return time.toString();
        } else {
            return "";
        }
    }

    /** {@inheritDoc} */
    @Override
    public Manager<TimeProvider> getManager() {
        return InstanceManager.getDefault(TimeProviderManager.class);
    }

    /** {@inheritDoc} */
    @Override
    public TimeProvider getBySystemName(@Nonnull String name) {
        return InstanceManager.getDefault(TimeProviderManager.class).getBySystemName(name);
    }

    /** {@inheritDoc} */
    @Override
    public TimeProvider getByUserName(@Nonnull String name) {
        return InstanceManager.getDefault(TimeProviderManager.class).getByUserName(name);
    }

    /** {@inheritDoc} */
    @Override
    protected String getMasterClassName() {
        return jmri.jmrit.beantable.TimeProviderTableAction.class.getName();
    }

    /** {@inheritDoc} */
    @Override
    public void clickOn(TimeProvider t) {
        // don't do anything on click; not used in this class, because
        // we override setValueAt
    }

    /*.* {@inheritDoc} *./
    @Override
    public void setValueAt(Object value, int row, int col) {
        if (col == VALUECOL) {
            getBySystemName(sysNameList.get(row)).setValue(value);
            fireTableRowsUpdated(row, row);
        } else {
            super.setValueAt(value, row, col);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getColumnName(int col) {
        if (col == VALUECOL) {
            return Bundle.getMessage("BlockValue");
        }
        return super.getColumnName(col);
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> getColumnClass(int col) {
        if (col == VALUECOL) {
            return String.class;
        } else {
            return super.getColumnClass(col);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void configValueColumn(JTable table) {
        // value column isn't button, so config is null
    }

    /** {@inheritDoc} */
    @Override
    protected boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
        return true;
        // return (e.getPropertyName().indexOf("alue")>=0);
    }

    /** {@inheritDoc} */
    @Override
    public JButton configureButton() {
        log.error("configureButton should not have been called");
        return null;
    }

    /**
     * Additionally provide Clock value Class in value column.
     * {@inheritDoc}
     */
    @Override
    public String getCellToolTip(JTable table, int row, int col) {
        switch (col) {
            case VALUECOL:
//                Clock m = getBySystemName(sysNameList.get(row));
//                LocalDateTime time = m.getTime();
//                return (time == null ? null : time.getClass().getName());
                return LocalDateTime.class.getName();
            default:
                return super.getCellToolTip(table, row, col);
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TimeProviderTableDataModel.class);

}
