package jmri.jmrit.beantable;

import jmri.*;
import jmri.jmrit.beantable.routetable.RouteEditFrame;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.event.MouseEvent;

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
        if (col == VALUECOL) {
            return "";  // no heading on "Set"
        }
        if (col == SETCOL) {
            return "";  // no heading on "Edit"
        }
        if (col == ENABLECOL) {
            return Bundle.getMessage("ColumnHeadEnabled");
        }
        if (col == LOCKCOL) {
            return Bundle.getMessage("Locked");
        } else {
            return super.getColumnName(col);
        }
    }

    @Override
    public Class<?> getColumnClass(int col) {
        if (col == SETCOL) {
            return JButton.class;
        }
        if (col == ENABLECOL) {
            return Boolean.class;
        }
        if (col == LOCKCOL) {
            return Boolean.class;
        } else {
            return super.getColumnClass(col);
        }
    }

    @Override
    public int getPreferredWidth(int col) {
        if (col == SETCOL) {
            return new JTextField(6).getPreferredSize().width;
        }
        if (col == ENABLECOL) {
            return new JTextField(6).getPreferredSize().width;
        }
        if (col == LOCKCOL) {
            return new JTextField(6).getPreferredSize().width;
        } else {
            return super.getPreferredWidth(col);
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        if (col == USERNAMECOL) {
            return true;
        }
        if (col == SETCOL) {
            return true;
        }
        if (col == ENABLECOL) {
            return true;
        }
        // Route lock is available if turnouts are lockable
        if (col == LOCKCOL) {
            Route r = (Route) getValueAt(row, SYSNAMECOL);
            return r.canLock();
        } else {
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
                // set up to edit. Use separate Runnable so window is created on top
                class WindowMaker implements Runnable {

                    int row;

                    WindowMaker(int r) {
                        row = r;
                    }

                    @Override
                    public void run() {
                        JmriJFrame editFrame = new RouteEditFrame(((Route) getValueAt(row, SYSNAMECOL)).getSystemName());
                        editFrame.setVisible(true);
                    }

                }
                WindowMaker t = new WindowMaker(row);
                javax.swing.SwingUtilities.invokeLater(t);
                break;
            case ENABLECOL: {
                // alternate
                Route r = (Route) getValueAt(row, SYSNAMECOL);
                boolean v = r.getEnabled();
                r.setEnabled(!v);
                break;
            }
            case LOCKCOL: {
                // alternate
                Route r = (Route) getValueAt(row, SYSNAMECOL);
                boolean v = r.getLocked();
                r.setLocked(!v);
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
        if (e.getPropertyName().equals("Enabled")) { // NOI18N
            return true;
        }
        if (e.getPropertyName().equals("Locked")) { // NOI18N
            return true;
        } else {
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

    @Override
    protected String getBeanType() {
        return Bundle.getMessage("BeanNameRoute");
    }

    private static final Logger log = LoggerFactory.getLogger(RouteTableDataModel.class);

}
