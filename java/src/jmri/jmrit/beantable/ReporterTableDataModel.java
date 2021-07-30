package jmri.jmrit.beantable;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.Reportable;
import jmri.Reporter;
import jmri.ReporterManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data model for a Reporter Table.
 * Code originally within ReporterTableAction.
 * 
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Steve Young Copyright (C) 2021
 */
public class ReporterTableDataModel extends BeanTableDataModel<Reporter> {
    
    public static final int LASTREPORTCOL = NUMCOLUMN;
    
    public ReporterTableDataModel(Manager<Reporter> mgr){
        super();
        setManager(mgr);
    }
    
    private ReporterManager reporterManager;
    
    /**
     * {@inheritDoc}
     */
    @Override
    @CheckForNull
    public String getValue(String name) {
        Object value;
        Reporter r = getManager().getBySystemName(name);
        if (r == null) {
            return "";
        }
        value = r.getCurrentReport();
        if (value == null) {
            return null;
        } else if (value instanceof Reportable) {
            return ((Reportable) value).toReportString();
        } else {
            return value.toString();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setManager(Manager<Reporter> rm) {
        if (!(rm instanceof ReporterManager)) {
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
        reporterManager = (ReporterManager) rm;
        getManager().addPropertyChangeListener(this);
        updateNameList();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ReporterManager getManager() {
        return ( reporterManager == null ? InstanceManager.getDefault(ReporterManager.class): reporterManager);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Reporter getBySystemName(@Nonnull String name) {
        return getManager().getBySystemName(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Reporter getByUserName(@Nonnull String name) {
        return getManager().getByUserName(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getMasterClassName() {
        return ReporterTableAction.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clickOn(Reporter t) {
        // don't do anything on click; not used in this class, because
        // we override setValueAt
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValueAt(Object value, int row, int col) {
        switch (col) {
            case VALUECOL:
                getBySystemName(sysNameList.get(row)).setReport(value);
                fireTableRowsUpdated(row, row);
                break;
            case LASTREPORTCOL: // do nothing
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
        return LASTREPORTCOL + getPropertyColumnCount() +1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getColumnName(int col) {
        switch (col) {
            case VALUECOL:
                return Bundle.getMessage("LabelReport");
            case LASTREPORTCOL:
                return Bundle.getMessage("LabelLastReport");
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
            case LASTREPORTCOL:
                return String.class;
            default:
                return super.getColumnClass(col);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCellEditable(int row, int col) {
        if (col == LASTREPORTCOL) {
            return false;
        } else
        return super.isCellEditable(row, col);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValueAt(int row, int col) {
        if (col == LASTREPORTCOL) {
            return getValue(sysNameList.get(row));
        } else
        return super.getValueAt(row, col);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPreferredWidth(int col) {
        if (col == LASTREPORTCOL) {
            return super.getPreferredWidth(VALUECOL);
        } else
        return super.getPreferredWidth(col);
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
        // return (e.getPropertyName().indexOf("Report")>=0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JButton configureButton() {
        log.error("configureButton should not have been called");
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getBeanType() {
        return Bundle.getMessage("BeanNameReporter");
    }

    private final static Logger log = LoggerFactory.getLogger(ReporterTableDataModel.class);
    
}
