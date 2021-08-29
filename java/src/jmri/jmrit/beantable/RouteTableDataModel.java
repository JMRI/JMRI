package jmri.jmrit.beantable;

import jmri.*;
import jmri.jmrit.beantable.routetable.RouteEditFrame;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.swing.*;

/**
 * TableDataModel for the Route Table.
 *
 * Split from {@link RouteTableAction}
 *
 * @author Dave Duchamp Copyright (C) 2004
 * @author Bob Jacobsen Copyright (C) 2007
 * @author Simon Reader Copyright (C) 2008
 * @author Pete Cressman Copyright (C) 2009
 * @author Egbert Broerse Copyright (C) 2016
 * @author Paul Bender Colyright (C) 2020
 */
public class RouteTableDataModel extends BeanTableDataModel<Route> {

    private static final int ENABLECOL = NUMCOLUMN;
    private static final int LOCKCOL = ENABLECOL + 1;
    private static final int SETCOL = ENABLECOL + 2;

    @Override
    public int getColumnCount() {
        return NUMCOLUMN + 3;
    }

    @Override
    public String getColumnName(int col) {
        switch (col) {
            case VALUECOL: // no heading on "Edit"
            case SETCOL: // no heading on "Set"
                return "";  
            case ENABLECOL:
                return Bundle.getMessage("ColumnHeadEnabled");
            case LOCKCOL:
                return Bundle.getMessage("Locked");
            default:
                return super.getColumnName(col);
        }
    }

    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case SETCOL:
                return JButton.class;
            case ENABLECOL:
            case LOCKCOL:
                return Boolean.class;
            default:
                return super.getColumnClass(col);
        }
    }

    @Override
    public int getPreferredWidth(int col) {
        switch (col) {
            case SETCOL:
            case ENABLECOL:
            case LOCKCOL:
                return new JTextField(6).getPreferredSize().width;
            default:
                return super.getPreferredWidth(col);
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        switch (col) {
            case USERNAMECOL:
            case SETCOL:
            case ENABLECOL:
                return true;
            case LOCKCOL: // Route lock is available if turnouts are lockable
                return ((Route) getValueAt(row, SYSNAMECOL)).canLock();
            default:
                return super.isCellEditable(row, col);
        }
    }

    @Override
    public Object getValueAt(int row, int col) {
        switch (col) {
            case SETCOL:
                return Bundle.getMessage("ButtonEdit");
            case ENABLECOL:
                return ((Route) getValueAt(row, SYSNAMECOL)).getEnabled();
            case LOCKCOL:
                Route r = (Route) getValueAt(row, SYSNAMECOL);
                if (r.canLock()) {
                    return r.getLocked();
                } else {
                    // this covers the case when route was locked and lockable turnouts were removed from the route
                    r.setLocked(false);
                    return false;
                }
            default:
                return super.getValueAt(row, col);
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        switch (col) {
            case USERNAMECOL:
                // Directly changing the username should only be possible if the username was previously null or ""
                // check to see if user name already exists
                if (value.equals("")) {
                    value = null;
                } else {
                    Route nB = getByUserName((String) value);
                    if (nB != null) {
                        log.error("User Name is not unique {}", value);
                        String msg;
                        msg = Bundle.getMessage("WarningUserName", ("" + value));
                        JOptionPane.showMessageDialog(null, msg, Bundle.getMessage("WarningTitle"), JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                Route nBean = getBySystemName(sysNameList.get(row));
                nBean.setUserName((String) value);
                fireTableRowsUpdated(row, row);
                break;
            case SETCOL:
                SwingUtilities.invokeLater(() -> {
                    JmriJFrame editFrame = new RouteEditFrame(((Route) getValueAt(row, SYSNAMECOL)).getSystemName());
                    editFrame.setVisible(true);
                });
                break;
            case ENABLECOL: {
                // alternate
                Route r = (Route) getValueAt(row, SYSNAMECOL);
                r.setEnabled(!r.getEnabled());
                break;
            }
            case LOCKCOL: {
                // alternate
                Route r = (Route) getValueAt(row, SYSNAMECOL);
                r.setLocked(!r.getLocked());
                break;
            }
            default:
                super.setValueAt(value, row, col);
                break;
        }
    }

    @Override
    public void configureTable(JTable table) {
        table.setDefaultRenderer(Boolean.class, new EnablingCheckboxRenderer());
        table.setDefaultRenderer(JComboBox.class, new jmri.jmrit.symbolicprog.ValueRenderer());
        table.setDefaultEditor(JComboBox.class, new jmri.jmrit.symbolicprog.ValueEditor());
        super.configureTable(table);
    }

    /**
     * Delete the bean after all the checking has been done.
     * <p>
     * Deactivate the Route, then use the superclass to delete it.
     */
    @Override
    protected void doDelete(Route bean) {
        bean.deActivateRoute();
        super.doDelete(bean);
    }

    // want to update when enabled parameter changes
    @Override
    protected boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
        switch (e.getPropertyName()) {
            case "Enabled": // NOI18N
            case "Locked": // NOI18N
                return true;
            default:
                return super.matchPropertyName(e);
        }
    }

    @Override
    public RouteManager getManager() {
        return InstanceManager.getDefault(RouteManager.class);
    }

    @Override
    public Route getBySystemName(@Nonnull String name) {
        return InstanceManager.getDefault(RouteManager.class).getBySystemName(name);
    }

    @Override
    public Route getByUserName(@Nonnull String name) {
        return InstanceManager.getDefault(RouteManager.class).getByUserName(name);
    }

    @Override
    protected String getMasterClassName() {
        return this.getClass().getName();
    }

    @Override
    public void clickOn(Route t) {
        t.setRoute();
    }

    @Override
    public String getValue(String s) {
        return Bundle.getMessage("Set");
        //Title of Set button in Route table
    }

    private static final Logger log = LoggerFactory.getLogger(RouteTableDataModel.class);

}
