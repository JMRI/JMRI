// IdTagTableAction.java
package jmri.jmrit.beantable;

import org.apache.log4j.Logger;
import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.util.Date;

import javax.swing.BoxLayout;
import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import jmri.Reporter;
import jmri.IdTag;
import jmri.IdTagManager;

import jmri.util.JmriJFrame;

/**
 * Swing action to create and register a
 * IdTagTable GUI.
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @author      Matthew Harris  Copyright (C) 2011
 * @version     $Revision$
 * @since       2.11.4
 */
public class IdTagTableAction extends AbstractTableAction {

    /**
     * Create an action with a specific title.
     * <P>
     * Note that the argument is the Action title, not the title of the
     * resulting frame.  Perhaps this should be changed?
     * @param actionName
     */
    public IdTagTableAction(String actionName) {
        super(actionName);

        // disable ourself if there is no primary IdTag manager available
        if (InstanceManager.getDefault(IdTagManager.class) == null) {
            setEnabled(false);
        }

    }

    public IdTagTableAction() {
        this("Id Tag Table");
    }

    /**
     * Create the JTable DataModel, along with the changes
     * for the specific case of IdTag objects
     */
    protected void createModel() {
        m = new BeanTableDataModel() {

            public static final int WHERECOL = NUMCOLUMN;
            public static final int WHENCOL = WHERECOL + 1;
            public static final int CLEARCOL = WHENCOL + 1;

            public String getValue(String name) {
                IdTag tag = InstanceManager.getDefault(IdTagManager.class).getBySystemName(name);
                if (tag == null) {
                    return "?";
                }
                Object t = tag.getTagID();
                if (t != null) {
                    return t.toString();
                } else {
                    return "";
                }
            }

            public Manager getManager() {
                IdTagManager m = InstanceManager.getDefault(IdTagManager.class);
                if (!m.isInitialised()) m.init();
                return m;
            }

            public NamedBean getBySystemName(String name) {
                return InstanceManager.getDefault(IdTagManager.class).getBySystemName(name);
            }

            public NamedBean getByUserName(String name) {
                return InstanceManager.getDefault(IdTagManager.class).getByUserName(name);
            }
            /*public int getDisplayDeleteMsg() { return InstanceManager.getDefault(jmri.UserPreferencesManager.class).getWarnMemoryInUse(); }
            public void setDisplayDeleteMsg(int boo) { InstanceManager.getDefault(jmri.UserPreferencesManager.class).setWarnMemoryInUse(boo); }*/

            public void clickOn(NamedBean t) {
                // don't do anything on click; not used in this class, because
                // we override setValueAt
            }

            @Override
            public void setValueAt(Object value, int row, int col) {
                if (col == CLEARCOL) {
                    IdTag t = (IdTag) getBySystemName(sysNameList.get(row));
                    if (log.isDebugEnabled()) {
                        log.debug("Clear where & when last seen for " + t.getSystemName());
                    }
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
                        return rb.getString("ColumnIdTagID");
                    case WHERECOL:
                        return rb.getString("ColumnIdWhere");
                    case WHENCOL:
                        return rb.getString("ColumnIdWhen");
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
                        t = (IdTag) getBySystemName(sysNameList.get(row));
                        return (t != null) ? (((r = t.getWhereLastSeen()) != null) ? r.getSystemName() : null) : null;
                    case WHENCOL:
                        Date d;
                        t = (IdTag) getBySystemName(sysNameList.get(row));
                        return (t != null) ? (((d = t.getWhenLastSeen()) != null) ?
                            DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(d) : null) : null;
                    case CLEARCOL:
                        return rb.getString("ButtonClear");
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
                        return new JButton(rb.getString("ButtonClear")).getPreferredSize().width + 4;
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
                BeanTableDataModel.log.error("configureButton should not have been called");
                return null;
            }

            @Override
            protected String getMasterClassName() {
                return getClassName();
            }
            
            protected String getBeanType(){
                return "ID Tag";
            }
        };
    }

    protected void setTitle() {
        f.setTitle(f.rb.getString("TitleIdTagTable"));
    }

    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.IdTagTable";
    }
    JmriJFrame addFrame = null;
    JTextField sysName = new JTextField(12);
    JTextField userName = new JTextField(15);
    JCheckBox isStateStored = new JCheckBox(rb.getString("IdStoreState"));
    JCheckBox isFastClockUsed = new JCheckBox(rb.getString("IdUseFastClock"));

    protected void addPressed(ActionEvent e) {
        if (addFrame == null) {
            addFrame = new JmriJFrame(rb.getString("TitleAddIdTag"), false, true);
            addFrame.addHelpMenu("package.jmri.jmrit.beantable.IdTagAddEdit", true);
            addFrame.getContentPane().setLayout(new BoxLayout(addFrame.getContentPane(), BoxLayout.Y_AXIS));

            ActionListener listener = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    okPressed(e);
                }
            };
            addFrame.add(new AddNewDevicePanel(sysName, userName, "ButtonOK", listener));
        }
        addFrame.pack();
        addFrame.setVisible(true);
    }

    void okPressed(ActionEvent e) {
        String user = userName.getText();
        if (user.equals("")) {
            user = null;
        }
//        String tag = tagID.getText();
//        if (tag.equals("")) tag=null;
        String sName = sysName.getText();
        try {
            InstanceManager.getDefault(IdTagManager.class).newIdTag(sName, user);
//            InstanceManager.idTagManagerInstance().newIdTag(sName, user, tag);
        } catch (IllegalArgumentException ex) {
            // user input no good
            handleCreateException(sName);
            return; // without creating       
        }
    }
    //private boolean noWarn = false;

    void handleCreateException(String sysName) {
        javax.swing.JOptionPane.showMessageDialog(addFrame,
                java.text.MessageFormat.format(
                rb.getString("ErrorIdTagAddFailed"),
                new Object[]{sysName}),
                rb.getString("ErrorTitle"),
                javax.swing.JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public String getClassDescription() {
        return f.rb.getString("TitleIdTagTable");
    }

    @Override
    public void addToFrame(BeanTableFrame f) {
        f.addToBottomBox(isStateStored, this.getClass().getName());
        isStateStored.setSelected(InstanceManager.getDefault(IdTagManager.class).isStateStored());
        isStateStored.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                InstanceManager.getDefault(IdTagManager.class).setStateStored(isStateStored.isSelected());
            }
        });
        f.addToBottomBox(isFastClockUsed, this.getClass().getName());
        isFastClockUsed.setSelected(InstanceManager.getDefault(IdTagManager.class).isFastClockUsed());
        isFastClockUsed.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                InstanceManager.getDefault(IdTagManager.class).setFastClockUsed(isFastClockUsed.isSelected());
            }
        });
        log.debug("Added CheckBox in addToFrame method");
    }

    @Override
    public void addToPanel(AbstractTableTabAction f) {
        f.addToBottomBox(isStateStored, this.getClass().getName());
        isStateStored.setSelected(InstanceManager.getDefault(IdTagManager.class).isStateStored());
        isStateStored.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                InstanceManager.getDefault(IdTagManager.class).setStateStored(isStateStored.isSelected());
            }
        });
        f.addToBottomBox(isFastClockUsed, this.getClass().getName());
        isFastClockUsed.setSelected(InstanceManager.getDefault(IdTagManager.class).isFastClockUsed());
        isFastClockUsed.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                InstanceManager.getDefault(IdTagManager.class).setFastClockUsed(isFastClockUsed.isSelected());
            }
        });
        log.debug("Added CheckBox in addToPanel method");
    }

    protected String getClassName() {
        return IdTagTableAction.class.getName();
    }
    private static final Logger log = Logger.getLogger(IdTagTableAction.class.getName());
}

/* @(#)IdTagTableAction.java */
