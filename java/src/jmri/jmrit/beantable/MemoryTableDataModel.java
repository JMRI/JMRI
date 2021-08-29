package jmri.jmrit.beantable;

import jmri.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.swing.*;

/**
 * TableDataModel for the Memory Table.
 *
 * Split from {@link MemoryTableAction}
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Steve Young Copyright (C) 2021
 */
public class MemoryTableDataModel extends BeanTableDataModel<Memory> {

    /**
     * Create a new Memory Table Data Model.
     * @param mgr Memory manager to use in the model, default MemoryManager always used.
     */
    public MemoryTableDataModel(Manager<Memory> mgr){
        super();
        setManager(mgr);
    }
    
    /** {@inheritDoc} */
    @Override
    public String getValue(String name) {
        Memory mem = InstanceManager.getDefault(MemoryManager.class).getBySystemName(name);
        if (mem == null) {
            return "?";
        }
        Object m = mem.getValue();
        if (m != null) {
            if ( m instanceof Reportable) {
                return ((Reportable) m).toReportString();
            }
            else {
                return m.toString();
            }
        } else {
            return "";
        }
    }

    /** {@inheritDoc} */
    @Override
    public Manager<Memory> getManager() {
        return InstanceManager.getDefault(MemoryManager.class);
    }

    /** {@inheritDoc} */
    @Override
    public Memory getBySystemName(@Nonnull String name) {
        return InstanceManager.getDefault(MemoryManager.class).getBySystemName(name);
    }

    /** {@inheritDoc} */
    @Override
    public Memory getByUserName(@Nonnull String name) {
        return InstanceManager.getDefault(MemoryManager.class).getByUserName(name);
    }

    /** {@inheritDoc} */
    @Override
    protected String getMasterClassName() {
        return this.getClass().getName();
    }

    /** {@inheritDoc} */
    @Override
    public void clickOn(Memory t) {
        // don't do anything on click; not used in this class, because
        // we override setValueAt
    }

    /** {@inheritDoc} */
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
    
    private static final Logger log = LoggerFactory.getLogger(MemoryTableDataModel.class);

}
