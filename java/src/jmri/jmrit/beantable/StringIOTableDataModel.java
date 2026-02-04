package jmri.jmrit.beantable;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.StringIO;
import jmri.StringIOManager;


/**
 * Data model for a StringIO Table.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2024
 * @author Steve Young Copyright (C) 2021
 */
public class StringIOTableDataModel extends BeanTableDataModel<StringIO> {

    static final int KNOWNCOL = NUMCOLUMN;
    
    public StringIOTableDataModel(Manager<StringIO> mgr){
        super();
        setManager(mgr);
    }

    private StringIOManager stringIOManager;

    /**
     * {@inheritDoc}
     */
    @Override
    @CheckForNull
    public String getValue(String name) {
        StringIO r = getManager().getBySystemName(name);
        if (r == null) {
            return "";
        }
        return r.getCommandedStringValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setManager(Manager<StringIO> rm) {
        if (!(rm instanceof StringIOManager)) {
            return;
        }
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
        stringIOManager = (StringIOManager) rm;
        getManager().addPropertyChangeListener(this);
        updateNameList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StringIOManager getManager() {
        return ( stringIOManager == null ? InstanceManager.getDefault(StringIOManager.class): stringIOManager);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StringIO getBySystemName(@Nonnull String name) {
        return getManager().getBySystemName(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StringIO getByUserName(@Nonnull String name) {
        return getManager().getByUserName(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getMasterClassName() {
        return StringIOTableAction.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clickOn(StringIO t) {
        // don't do anything on click; not used in this class, because
        // we override setValueAt
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int getPreferredWidth(int col) {
        if (col == KNOWNCOL) {
            return new JTextField(15).getPreferredSize().width; // TODO I18N using Bundle.getMessage()
        }
        return super.getPreferredWidth(col);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValueAt(int row, int col) {
        switch (col) {
            case KNOWNCOL:
                StringIO r = getManager().getBySystemName(sysNameList.get(row));
                if (r == null) {
                    return "";
                }
                return r.getKnownStringValue();
            default:
                return super.getValueAt(row, col);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValueAt(Object value, int row, int col) {
        switch (col) {
            case VALUECOL:
                try {
                    getBySystemName(sysNameList.get(row)).setCommandedStringValue(value.toString());
                } catch (jmri.JmriException ex) {
                    log.error("Exception trying to set value at {},{} to {}", row, col, value);
                }
                fireTableRowsUpdated(row, row);
                break;
            default:
                super.setValueAt(value, row, col);
                break;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getColumnCount() {
        return NUMCOLUMN + getPropertyColumnCount() + 1;  // +1 for known column
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getColumnName(int col) {
        switch (col) {
            case VALUECOL:
                return Bundle.getMessage("StringIOCommanded");
            case KNOWNCOL:
                return Bundle.getMessage("StringIOKnown");
            default:
                return super.getColumnName(col);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case VALUECOL:
                return String.class;
            case KNOWNCOL:
                return String.class;
            default:
                return super.getColumnClass(col);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configValueColumn(JTable table) {
        // value column isn't button, so config is null
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JButton configureButton() {
        log.error("configureButton should not have been called");
        return null;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StringIOTableDataModel.class);

}
