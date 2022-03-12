package jmri.jmrit.beantable;

import java.text.DateFormat;
import java.util.Date;

import jmri.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.swing.*;

/**
 * TableDataModel for an IdTag Table.
 *
 * Split from {@link IdTagTableAction}
 *
 * @author  Bob Jacobsen Copyright (C) 2003
 * @author  Matthew Harris Copyright (C) 2011
 * @author Steve Young Copyright (C) 2021
 */
public class IdTagTableDataModel extends BeanTableDataModel<IdTag> {

    /**
     * Create a new IdTag Table Data Model.
     * @param mgr IdTag manager to use in the model, default IdTag Manager always used.
     */
    public IdTagTableDataModel(Manager<IdTag> mgr){
        super();
        setManager(mgr);
    }
    
    @Override
    protected final void setManager(Manager<IdTag> mgr){
        if ( mgr instanceof IdTagManager ){
            tagManager = (IdTagManager)mgr;
        }
    }
    
    private IdTagManager tagManager;
    
    public static final int WHERECOL = NUMCOLUMN;
    public static final int WHENCOL = WHERECOL + 1;
    public static final int CLEARCOL = WHENCOL + 1;

    @Override
    public String getValue(String name) {
        IdTag tag = getManager().getBySystemName(name);
        if (tag == null) {
            return "?";
        }
        return tag.getTagID();
    }

    @Override
    public Manager<IdTag> getManager() {
        return ( tagManager != null ? tagManager : InstanceManager.getDefault(IdTagManager.class));
    }

    @Override
    public IdTag getBySystemName(@Nonnull String name) {
        return getManager().getBySystemName(name);
    }

    @Override
    public IdTag getByUserName(@Nonnull String name) {
        return getManager().getByUserName(name);
    }

    @Override
    public void clickOn(IdTag t) {
        // don't do anything on click; not used in this class, because
        // we override setValueAt
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        if (col == CLEARCOL) {
            IdTag t = getBySystemName(sysNameList.get(row));
            log.debug("Clear where & when last seen for {}", t.getSystemName());
            t.setWhereLastSeen(null);
            fireTableRowsUpdated(row, row);
        } else {
            super.setValueAt(value, row, col);
        }
    }

    @Override
    public int getColumnCount() {
        return CLEARCOL + 1;
    }

    @Override
    public String getColumnName(int col) {
        switch (col) {
            case VALUECOL:
                return Bundle.getMessage("ColumnIdTagID");
            case WHERECOL:
                return Bundle.getMessage("ColumnIdWhere");
            case WHENCOL:
                return Bundle.getMessage("ColumnIdWhen");
            case CLEARCOL:
                return "";
            default:
                return super.getColumnName(col);
        }
    }

    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case VALUECOL:
            case WHERECOL:
            case WHENCOL:
                return String.class;
            case CLEARCOL:
                return JButton.class;
            default:
                return super.getColumnClass(col);
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        switch (col) {
            case VALUECOL:
            case WHERECOL:
            case WHENCOL:
                return false;
            case CLEARCOL:
                return true;
            default:
                return super.isCellEditable(row, col);
        }
    }

    @Override
    public Object getValueAt(int row, int col) {
        IdTag t;
        switch (col) {
            case WHERECOL:
                Reporter r;
                t = getBySystemName(sysNameList.get(row));
                if ( t !=null ){
                    r = t.getWhereLastSeen();
                    if (r!=null){
                        return r.getDisplayName();                            
                    }
                }
                return null;
            case WHENCOL:
                Date d;
                t = getBySystemName(sysNameList.get(row));
                return (t != null) ? (((d = t.getWhenLastSeen()) != null)
                        ? DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(d) : null) : null;
            case CLEARCOL:
                return Bundle.getMessage("ButtonClear");
            default:
                return super.getValueAt(row, col);
        }
    }

    @Override
    public int getPreferredWidth(int col) {
        switch (col) {
            case SYSNAMECOL:
            case WHERECOL:
            case WHENCOL:
                return new JTextField(12).getPreferredSize().width;
            case VALUECOL:
                return new JTextField(10).getPreferredSize().width;
            case CLEARCOL:
                return new JButton(Bundle.getMessage("ButtonClear")).getPreferredSize().width + 4;
            default:
                return super.getPreferredWidth(col);
        }
    }

    @Override
    public void configValueColumn(JTable table) {
        // value column isn't button, so config is null
    }

    @Override
    protected boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
        return true;
        // return (e.getPropertyName().indexOf("alue")>=0);
    }

    @Override
    public JButton configureButton() {
        log.error("configureButton should not have been called");
        return null;
    }

    @Override
    protected String getMasterClassName() {
        return IdTagTableAction.class.getName();
    }
    
    private static final Logger log = LoggerFactory.getLogger(IdTagTableDataModel.class);

}
