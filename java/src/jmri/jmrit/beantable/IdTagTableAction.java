package jmri.jmrit.beantable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.util.Date;
import javax.annotation.Nonnull;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import jmri.IdTag;
import jmri.IdTagManager;
import jmri.InstanceManager;
import jmri.Manager;
import jmri.Reporter;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a IdTagTable GUI.
 *
 * @author  Bob Jacobsen Copyright (C) 2003
 * @author  Matthew Harris Copyright (C) 2011
 * @since 2.11.4
 */
public class IdTagTableAction extends AbstractTableAction<IdTag> implements PropertyChangeListener {

    /**
     * Create an action with a specific title.
     * <p>
     * Note that the argument is the Action title, not the title of the
     * resulting frame. Perhaps this should be changed?
     *
     * @param actionName title of the action
     */
    public IdTagTableAction(String actionName) {
        super(actionName);
        tagManager.addPropertyChangeListener(this);
    }
    
    @Nonnull
    protected IdTagManager tagManager = InstanceManager.getDefault(jmri.IdTagManager.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void setManager(@Nonnull Manager<IdTag> t) {
        if(tagManager!=null){
            tagManager.removePropertyChangeListener(this);
        }
        if (t instanceof IdTagManager) {
            tagManager = (IdTagManager) t;
            if (m != null) {
                m.setManager(tagManager);
            }
        }
        // if t is not an instance of IdTagManager, tagManager may not change.
        if(tagManager!=null){
            tagManager.addPropertyChangeListener(this);
        }
    }

    public IdTagTableAction() {
        this(Bundle.getMessage("TitleIdTagTable"));
    }

    /**
     * Create the JTable DataModel, along with the changes for the specific case
     * of IdTag objects
     */
    @Override
    protected void createModel() {
        m = new BeanTableDataModel<IdTag>() {

            public static final int WHERECOL = NUMCOLUMN;
            public static final int WHENCOL = WHERECOL + 1;
            public static final int CLEARCOL = WHENCOL + 1;

            @Override
            public String getValue(String name) {
                IdTag tag = tagManager.getBySystemName(name);
                if (tag == null) {
                    return "?";
                }
                return tag.getTagID();
            }

            @Override
            public Manager<IdTag> getManager() {
                return tagManager;
            }

            @Override
            public IdTag getBySystemName(String name) {
                return tagManager.getBySystemName(name);
            }

            @Override
            public IdTag getByUserName(String name) {
                return tagManager.getByUserName(name);
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
                        return (t != null) ? (((r = t.getWhereLastSeen()) != null) ? r.getSystemName() : null) : null;
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
                return getClassName();
            }

            @Override
            protected String getBeanType() {
                return "ID Tag";
            }
        };
    }

    @Override
    protected void setTitle() {
        f.setTitle(Bundle.getMessage("TitleIdTagTable"));
    }

    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.IdTagTable";
    }

    JmriJFrame addFrame = null;
    JTextField sysName = new JTextField(12);
    JTextField userName = new JTextField(15);
    JCheckBox isStateStored = new JCheckBox(Bundle.getMessage("IdStoreState"));
    JCheckBox isFastClockUsed = new JCheckBox(Bundle.getMessage("IdUseFastClock"));

    @Override
    protected void addPressed(ActionEvent e) {
        if (addFrame == null) {
            addFrame = new JmriJFrame(Bundle.getMessage("TitleAddIdTag"), false, true);
            addFrame.addHelpMenu("package.jmri.jmrit.beantable.IdTagAddEdit", true);
            addFrame.getContentPane().setLayout(new BoxLayout(addFrame.getContentPane(), BoxLayout.Y_AXIS));

            ActionListener okListener = (ActionEvent ev) -> {
                okPressed(ev);
            };
            ActionListener cancelListener = (ActionEvent ev) -> {
                cancelPressed(ev);
            };
            addFrame.add(new AddNewDevicePanel(sysName, userName, "ButtonOK", okListener, cancelListener));
        }
        addFrame.pack();
        addFrame.setVisible(true);
    }

    void cancelPressed(ActionEvent e) {
        addFrame.setVisible(false);
        addFrame.dispose();
        addFrame = null;
    }

    void okPressed(ActionEvent e) {
        String user = userName.getText();
        if (user.equals("")) {
            user = null;
        }
        String sName = sysName.getText();
        try {
            tagManager.newIdTag(sName, user);
        } catch (IllegalArgumentException ex) {
            // user input no good
            handleCreateException(sName);
        }
    }
    //private boolean noWarn = false;

    void handleCreateException(String sysName) {
        JOptionPane.showMessageDialog(addFrame,
                Bundle.getMessage("ErrorIdTagAddFailed", sysName) + "\n" + Bundle.getMessage("ErrorAddFailedCheck"),
                Bundle.getMessage("ErrorTitle"),
                JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public String getClassDescription() {
        return Bundle.getMessage("TitleIdTagTable");
    }

    @Override
    public void addToFrame(BeanTableFrame f) {
        f.addToBottomBox(isStateStored, this.getClass().getName());
        isStateStored.setSelected(tagManager.isStateStored());
        isStateStored.addActionListener((ActionEvent e) -> {
            tagManager.setStateStored(isStateStored.isSelected());
        });
        f.addToBottomBox(isFastClockUsed, this.getClass().getName());
        isFastClockUsed.setSelected(tagManager.isFastClockUsed());
        isFastClockUsed.addActionListener((ActionEvent e) -> {
            tagManager.setFastClockUsed(isFastClockUsed.isSelected());
        });
        log.debug("Added CheckBox in addToFrame method");
    }

    @Override
    public void addToPanel(AbstractTableTabAction<IdTag> f) {
        String connectionName = tagManager.getMemo().getUserName();
        if (tagManager instanceof jmri.managers.ProxyIdTagManager) {
            connectionName = "All";
        } else if (connectionName == null && (tagManager instanceof jmri.managers.DefaultRailComManager)) {
            connectionName = "RailCom"; // NOI18N (proper name).
        }
        f.addToBottomBox(isStateStored, connectionName);
        isStateStored.setSelected(tagManager.isStateStored());
        isStateStored.addActionListener((ActionEvent e) -> {
            tagManager.setStateStored(isStateStored.isSelected());
        });
        f.addToBottomBox(isFastClockUsed, connectionName);
        isFastClockUsed.setSelected(tagManager.isFastClockUsed());
        isFastClockUsed.addActionListener((ActionEvent e) -> {
            tagManager.setFastClockUsed(isFastClockUsed.isSelected());
        });
        log.debug("Added CheckBox in addToPanel method for system {}", connectionName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (e.getPropertyName().equals("StateStored")) {
           isStateStored.setSelected(tagManager.isStateStored());
        } else if (e.getPropertyName().equals("UseFastClock")) {
           isFastClockUsed.setSelected(tagManager.isFastClockUsed()); 
        }
    }

    @Override
    protected String getClassName() {
        return IdTagTableAction.class.getName();
    }
    private static final Logger log = LoggerFactory.getLogger(IdTagTableAction.class);

}
