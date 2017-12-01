package jmri.jmrix.ecos.swing.locodatabase;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Hashtable;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowSorter;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import jmri.Manager;
import jmri.NamedBean;
import jmri.jmrit.beantable.AbstractTableAction;
import jmri.jmrit.beantable.BeanTableDataModel;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.swing.GlobalRosterEntryComboBox;
import jmri.jmrix.ecos.EcosLocoAddress;
import jmri.jmrix.ecos.EcosLocoAddressManager;
import jmri.jmrix.ecos.EcosMessage;
import jmri.jmrix.ecos.EcosSystemConnectionMemo;
import jmri.jmrix.ecos.utilities.EcosLocoToRoster;
import jmri.jmrix.ecos.utilities.RemoveObjectFromEcos;
import jmri.util.swing.XTableColumnModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EcosLocoTableAction extends AbstractTableAction {

    /**
     * Create an action with a specific title.
     * <p>
     * Note that the argument is the Action title, not the title of the
     * resulting frame. Perhaps this should be changed?
     */
    public EcosLocoTableAction(String s) {
        super(s);
    }

    public EcosLocoTableAction() {
        this(Bundle.getMessage("EcosLocoTableTitle"));
    }

    public EcosLocoTableAction(String s, EcosSystemConnectionMemo memo) {
        this(s);
        setAdapterMemo(memo);
        includeAddButton = false;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // create the JTable model, with changes for specific NamedBean
        createModel();
        TableRowSorter<BeanTableDataModel> sorter = new TableRowSorter<>(m);
        JTable dataTable = m.makeJTable(getClassName(), m, sorter);
        // create the frame
        f = new jmri.jmrit.beantable.BeanTableFrame(m, helpTarget(), dataTable) {
        };
        setMenuBar(f);
        setTitle();
        addToFrame(f);
        f.pack();
        f.setVisible(true);
    }

    protected EcosSystemConnectionMemo adaptermemo;
    protected EcosLocoAddressManager locoManager;

    @Override
    public void setManager(Manager man) {
        locoManager = (EcosLocoAddressManager) man;
    }
    protected String rosterAttribute;

    public void setAdapterMemo(EcosSystemConnectionMemo memo) {
        adaptermemo = memo;
        locoManager = adaptermemo.getLocoAddressManager();
        rosterAttribute = adaptermemo.getPreferenceManager().getRosterAttribute();
    }

    protected EcosLocoAddress getByEcosObject(String object) {
        return locoManager.getByEcosObject(object);
    }

    List<String> ecosObjectIdList = null;
    JTable table;

    static public final int PROTOCOL = 5;
    static public final int ADDTOROSTERCOL = 6;
    static public final int SPEEDDIR = 7;
    static public final int STOP = 8;

    @Override
    protected void createModel() {
        m = new BeanTableDataModel() {

            //We have to set a manager first off, but this gets replaced.
            @Override
            protected EcosLocoAddressManager getManager() {
                return locoManager;
            }

            protected String getRosterAttribute() {
                return rosterAttribute;
            }

            @Override
            public String getValue(String s) {
                return Bundle.getMessage("ButtonSet");
            }

            @Override
            protected String getMasterClassName() {
                return getClassName();
            }

            @Override
            public void clickOn(jmri.NamedBean t) {
            }

            @Override
            protected synchronized void updateNameList() {
                // first, remove listeners from the individual objects
                if (ecosObjectIdList != null) {
                    for (int i = 0; i < ecosObjectIdList.size(); i++) {
                        // if object has been deleted, it's not here; ignore it
                        jmri.jmrix.ecos.EcosLocoAddress b = getByEcosObject(ecosObjectIdList.get(i));
                        if (b != null) {
                            b.removePropertyChangeListener(this);
                        }
                    }
                }
                ecosObjectIdList = getManager().getEcosObjectList();
                // and add them back in
                for (int i = 0; i < ecosObjectIdList.size(); i++) {
                    getByEcosObject(ecosObjectIdList.get(i)).addPropertyChangeListener(this);
                }
            }

            @Override
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                updateNameList();
                if (e.getPropertyName().equals("length")) {
                    // a new jmri.jmrix.ecos.EcosLocoAddressManager is available in the manager
                    updateNameList();
                    fireTableDataChanged();
                } else if (matchPropertyName(e)) {
                    // a value changed.  Find it, to avoid complete redraw
                    String object = ((jmri.jmrix.ecos.EcosLocoAddress) e.getSource()).getEcosObject();
                    // since we can add columns, the entire row is marked as updated
                    int row = ecosObjectIdList.indexOf(object);
                    fireTableRowsUpdated(row, row);
                }
            }

            @Override
            public int getColumnCount() {
                return STOP + 1;
            }

            @Override
            public void setValueAt(Object value, int row, int col) {
                if (col == COMMENTCOL) {
                    RosterEntry re;
                    String ecosObjectNo = ecosObjectIdList.get(row);
                    if (value == null) {
                        return;
                    } else if (value instanceof RosterEntry) {
                        re = (RosterEntry) value;
                        if ((re.getAttribute(getRosterAttribute()) != null && !re.getAttribute(getRosterAttribute()).equals(""))) {
                            JOptionPane.showMessageDialog(f,
                                    Bundle.getMessage("EcosEditAssignedDialog", ecosObjectNo));
                            log.error(ecosObjectNo + " This roster entry already has an ECoS loco assigned to it");
                            return;
                        }
                        String oldRoster = getByEcosObject(ecosObjectNo).getRosterId();
                        RosterEntry oldre;
                        if (oldRoster != null) {
                            oldre = Roster.getDefault().getEntryForId(oldRoster);
                            if (oldre != null) {
                                oldre.deleteAttribute(getRosterAttribute());
                            }
                        }
                        re.putAttribute(getRosterAttribute(), ecosObjectNo);
                        getByEcosObject(ecosObjectNo).setRosterId(re.getId());
                        re.updateFile();
                    } else if (value instanceof String) {
                        List<RosterEntry> r = Roster.getDefault().getEntriesWithAttributeKeyValue(getRosterAttribute(), ecosObjectNo);
                        if (r.isEmpty()) {
                            r.get(0).deleteAttribute(getRosterAttribute());
                            getByEcosObject(ecosObjectNo).setRosterId(null);
                            r.get(0).updateFile();
                        }

                    }
                    Roster.getDefault().writeRoster();
                } else if (col == ADDTOROSTERCOL) {
                    addToRoster(row, col);
                } else if (col == STOP) {
                    stopLoco(row, col);
                } else if (col == DELETECOL) {
                    // button fired, delete Bean
                    deleteLoco(row, col);
                } else if (col == USERNAMECOL) {
                    jmri.jmrix.ecos.EcosLocoAddress b = getByEcosObject(ecosObjectIdList.get(row));
                    EcosMessage m = new EcosMessage("request(" + b.getEcosObject() + ", control, force)");
                    adaptermemo.getTrafficController().sendEcosMessage(m, null);
                    m = new EcosMessage("set(" + b.getEcosObject() + ", name[\"" + (String) value + "\"])");
                    adaptermemo.getTrafficController().sendEcosMessage(m, null);
                    m = new EcosMessage("release(" + b.getEcosObject() + ", control)");
                    adaptermemo.getTrafficController().sendEcosMessage(m, null);
                }
            }

            @Override
            public JTable makeJTable(@Nonnull String name, @Nonnull TableModel model, @Nullable RowSorter<? extends TableModel> sorter) {
                return this.configureJTable(name, this.makeJTable(model), sorter);
            }

            private JTable makeJTable(@Nonnull TableModel model) {
                JTable table = new JTable(model) {

                    @Override
                    public TableCellRenderer getCellRenderer(int row, int column) {
                        if (column == COMMENTCOL) {
                            return getRenderer(row);
                        } else {
                            return super.getCellRenderer(row, column);
                        }
                    }

                    @Override
                    public TableCellEditor getCellEditor(int row, int column) {
                        if (column == COMMENTCOL) {
                            return getEditor(row);
                        } else {
                            return super.getCellEditor(row, column);
                        }
                    }

                    TableCellRenderer getRenderer(int row) {
                        TableCellRenderer retval = rendererMap.get(ecosObjectIdList.get(row));
                        if (retval == null) {
                            jmri.jmrix.ecos.EcosLocoAddress b = getByEcosObject(ecosObjectIdList.get(row));
                            RosterEntry re = null;
                            if (b != null) {
                                re = Roster.getDefault().getEntryForId(b.getRosterId());
                            }
                            retval = new RosterBoxRenderer(re);
                            rendererMap.put(ecosObjectIdList.get(row), retval);
                        }
                        return retval;
                    }
                    Hashtable<Object, TableCellRenderer> rendererMap = new Hashtable<>();

                    TableCellEditor getEditor(int row) {
                        TableCellEditor retval = editorMap.get(ecosObjectIdList.get(row));
                        if (retval == null) {
                            jmri.jmrix.ecos.EcosLocoAddress b = getByEcosObject(ecosObjectIdList.get(row));
                            RosterEntry re = null;
                            if (b != null) {
                                re = Roster.getDefault().getEntryForId(b.getRosterId());
                            }
                            GlobalRosterEntryComboBox cb = new GlobalRosterEntryComboBox();
                            cb.setNonSelectedItem(" ");
                            if (re == null) {
                                cb.setSelectedIndex(0);
                            } else {
                                cb.setSelectedItem(re);
                            }
                            // create a new one with right aspects
                            retval = new RosterComboBoxEditor(cb);
                            editorMap.put(ecosObjectIdList.get(row), retval);
                        }
                        return retval;
                    }
                    Hashtable<Object, TableCellEditor> editorMap = new Hashtable<>();
                };
                table.getTableHeader().setReorderingAllowed(true);
                table.setColumnModel(new XTableColumnModel());
                table.createDefaultColumnsFromModel();

                addMouseListenerToHeader(table);
                return table;
            }

            /**
             * Is this property event announcing a change this table should
             * display?
             * <P>
             * Note that events will come both from the
             * jmri.jmrix.ecos.EcosLocoAddressManagers and also from the manager
             */
            @Override
            protected boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
                if (!showLocoMonitor && (e.getPropertyName().equals("Speed") || e.getPropertyName().equals("Direction"))) {
                    return false;
                }
                return true;
            }

            @Override
            public String getColumnName(int col) {
                switch (col) {
                    case SYSNAMECOL:
                        return Bundle.getMessage("ObjectIdCol");
                    case USERNAMECOL:
                        return Bundle.getMessage("DescriptionCol");
                    case VALUECOL:
                        return Bundle.getMessage("AddressCol");
                    case COMMENTCOL:
                        return Bundle.getMessage("JmriIdCol");
                    case DELETECOL:
                        return ""; // no heading on Delete column
                    case PROTOCOL:
                        return Bundle.getMessage("ProtocolCol");
                    case ADDTOROSTERCOL:
                        return ""; // no heading on Add to Roster column
                    case SPEEDDIR:
                        return Bundle.getMessage("SpeedCol") + " " + Bundle.getMessage("DirectionCol");
                    case STOP:
                        return ""; // no heading on Stop column
                    default:
                        return "unknown"; // NOI18N
                }
            }

            @Override
            public Class<?> getColumnClass(int col) {
                switch (col) {
                    case SYSNAMECOL:
                    case USERNAMECOL:
                    case PROTOCOL:
                    case SPEEDDIR:
                        return String.class;
                    case VALUECOL:
                        return Integer.class;
                    case ADDTOROSTERCOL:
                    case DELETECOL:
                    case STOP:
                        return JButton.class;
                    case COMMENTCOL:
                        return JComboBox.class;
                    default:
                        return null;
                }
            }

            @Override
            public boolean isCellEditable(int row, int col) {
                switch (col) {
                    case COMMENTCOL:
                        return true;
                    case ADDTOROSTERCOL:
                        jmri.jmrix.ecos.EcosLocoAddress b = getByEcosObject(ecosObjectIdList.get(row));
                        if (b.getRosterId() == null || b.getRosterId().equals("")) {
                            return true;
                        } else {
                            return false;
                        }
                    case USERNAMECOL:
                    case DELETECOL:
                    case STOP:
                        return true;
                    default:
                        return false;
                }
            }

            @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "DB_DUPLICATE_SWITCH_CLAUSES",
                                justification="better to keep cases in column order rather than to combine")
            @Override
            public int getPreferredWidth(int col) {
                switch (col) {
                    case SYSNAMECOL:
                        return new JTextField(5).getPreferredSize().width;
                    case COMMENTCOL:
                        return new JTextField(20).getPreferredSize().width;
                    case USERNAMECOL:
                        return new JTextField(20).getPreferredSize().width;
                    case ADDTOROSTERCOL: // not actually used due to the configureTable, setColumnToHoldButton, configureButton
                        return new JTextField(12).getPreferredSize().width;
                    case STOP: // not actually used due to the configureTable, setColumnToHoldButton, configureButton
                        return new JTextField(6).getPreferredSize().width;
                    case VALUECOL:
                        return new JTextField(5).getPreferredSize().width;
                    case SPEEDDIR:
                        return new JTextField(10).getPreferredSize().width;
                    case PROTOCOL:
                        return new JTextField(5).getPreferredSize().width;
                    default:
                        //log.warn("Unexpected column in getPreferredWidth: "+col);
                        return super.getPreferredWidth(col);
                    //return new JTextField(8).getPreferredSize().width;
                }
            }

            @Override
            public void configureTable(JTable tbl) {
                table = tbl;
                setColumnToHoldButton(table, ADDTOROSTERCOL,
                        new JButton(Bundle.getMessage("ButtonAddRoster")));
                setColumnToHoldButton(table, STOP, stopButton());
                super.configureTable(table);
                XTableColumnModel columnModel = (XTableColumnModel) table.getColumnModel();
                TableColumn column = columnModel.getColumnByModelIndex(SPEEDDIR);
                columnModel.setColumnVisible(column, false);
                column = columnModel.getColumnByModelIndex(STOP);
                columnModel.setColumnVisible(column, false);
            }

            @Override
            public NamedBean getBySystemName(String name) {
                return null;
            }

            @Override
            public NamedBean getByUserName(String name) {
                return null;
            }

            @Override
            synchronized public void dispose() {
                showLocoMonitor = false;
                getManager().removePropertyChangeListener(this);
                if (ecosObjectIdList != null) {
                    for (int i = 0; i < ecosObjectIdList.size(); i++) {
                        jmri.jmrix.ecos.EcosLocoAddress b = getByEcosObject(ecosObjectIdList.get(i));
                        if (b != null) {
                            b.removePropertyChangeListener(this);
                        }
                    }
                }
            }

            protected void deleteLoco(final int row, int col) {
                if (row >= ecosObjectIdList.size()) {
                    log.debug("row is greater than list size");
                    return;
                }
                jmri.jmrix.ecos.EcosLocoAddress b = getByEcosObject(ecosObjectIdList.get(row));
                final JDialog dialog = new JDialog();
                dialog.setTitle(Bundle.getMessage("RemoveLocoTitle"));
                dialog.setLocation(300, 200);
                dialog.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
                JPanel container = new JPanel();
                container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
                container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                JLabel question = new JLabel(Bundle.getMessage("RemoveLocoXDialog", b.getEcosDescription()));
                question.setAlignmentX(Component.CENTER_ALIGNMENT);
                container.add(question);
                JButton yesButton = new JButton(Bundle.getMessage("ButtonYes"));
                JButton noButton = new JButton(Bundle.getMessage("ButtonNo"));
                JPanel button = new JPanel();
                button.setAlignmentX(Component.CENTER_ALIGNMENT);
                button.add(yesButton);
                button.add(noButton);
                container.add(button);

                noButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        dialog.dispose();
                    }
                });

                yesButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        RemoveObjectFromEcos removeObjectFromEcos = new RemoveObjectFromEcos();
                        removeObjectFromEcos.removeObjectFromEcos(ecosObjectIdList.get(row), adaptermemo.getTrafficController());
                        dialog.dispose();
                    }
                });
                container.setAlignmentX(Component.CENTER_ALIGNMENT);
                container.setAlignmentY(Component.CENTER_ALIGNMENT);
                dialog.getContentPane().add(container);
                dialog.pack();
                dialog.setModal(true);
                dialog.setVisible(true);
            }

            @Override
            public int getRowCount() {
                return ecosObjectIdList.size();
            }

            @Override
            public Object getValueAt(int row, int col) {
                if (row >= ecosObjectIdList.size()) {
                    log.debug("row is greater than list size");
                    return null;

                }
                jmri.jmrix.ecos.EcosLocoAddress b;
                switch (col) {
                    case SYSNAMECOL:
                        return ecosObjectIdList.get(row);
                    case USERNAMECOL:  // return user name
                        // sometimes, the TableSorter invokes this on rows that no longer exist, so we check
                        b = getByEcosObject(ecosObjectIdList.get(row));
                        return (b != null) ? b.getEcosDescription() : null;
                    case VALUECOL:  //
                        b = getByEcosObject(ecosObjectIdList.get(row));
                        return (b != null) ? b.getNumber() : null;
                    case COMMENTCOL:
                        b = getByEcosObject(ecosObjectIdList.get(row));
                        RosterEntry re = null;
                        if (b != null) {
                            re = Roster.getDefault().getEntryForId(b.getRosterId());
                        }
                        GlobalRosterEntryComboBox cb = (GlobalRosterEntryComboBox) table.getCellRenderer(row, col);
                        if (re == null) {
                            cb.setSelectedIndex(0);
                        } else {
                            cb.setSelectedItem(re);
                        }
                        return re;
                    case PROTOCOL:
                        b = getByEcosObject(ecosObjectIdList.get(row));
                        return (b != null) ? b.getECOSProtocol() : null;
                    case ADDTOROSTERCOL:  //
                        b = getByEcosObject(ecosObjectIdList.get(row));
                        if (b.getRosterId() == null || b.getRosterId().equals("")) {
                            return Bundle.getMessage("ButtonAddRoster");
                        } else {
                            return " ";
                        }
                    case STOP:
                        return Bundle.getMessage("ButtonStop");
                    case SPEEDDIR:
                        b = getByEcosObject(ecosObjectIdList.get(row));
                        return (b != null) ? (b.getDirectionAsString() + " : " + b.getSpeed()) : null;
                    case DELETECOL:  //
                        return Bundle.getMessage("ButtonDelete");
                    default:
                        //log.error("internal state inconsistent with table requst for "+row+" "+col);
                        return null;
                }
            }

            @Override
            protected String getBeanType() {
                return Bundle.getMessage("EcosLocoTitle");
            }

            @Override
            protected void showPopup(MouseEvent e) {

            }
        };
    }

    boolean showLocoMonitor = false;

    void showMonitorChanged() {
        showLocoMonitor = showMonitorLoco.isSelected();
        XTableColumnModel columnModel = (XTableColumnModel) table.getColumnModel();
        TableColumn column = columnModel.getColumnByModelIndex(SPEEDDIR);
        columnModel.setColumnVisible(column, showLocoMonitor);
        column = columnModel.getColumnByModelIndex(STOP);
        columnModel.setColumnVisible(column, showLocoMonitor);
    }

    JCheckBox showMonitorLoco = new JCheckBox("Monitor Loco Speed");

    /**
     * Create a JButton to edit a turnout operation.
     *
     * @return the JButton
     */
    protected JButton stopButton() {
        JButton stopButton = new JButton("STOP");
        return (stopButton);
    }

    void stopLoco(int row, int col) {

        String objectNumber = ecosObjectIdList.get(row);
        EcosMessage msg;
        //We will repeat this three times to make sure it gets through.
        for (int x = 0; x < 3; x++) {
            msg = new EcosMessage("request(" + objectNumber + ", control, force)");
            adaptermemo.getTrafficController().sendEcosMessage(msg, null);
            msg = new EcosMessage("set(" + objectNumber + ", stop)");
            adaptermemo.getTrafficController().sendEcosMessage(msg, null);
            msg = new EcosMessage("release(" + objectNumber + ", control)");
            adaptermemo.getTrafficController().sendEcosMessage(msg, null);
        }
    }

    public void addToPanel(EcosLocoTableTabAction f) {
        f.addToBottomBox(showMonitorLoco, adaptermemo.getUserName());
        showMonitorLoco.setToolTipText("Show extra columns for configuring turnout feedback?");
        showMonitorLoco.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showMonitorChanged();
            }
        });
    }

    void addToRoster(int row, int col) {
        if (getByEcosObject(ecosObjectIdList.get(row)).getRosterId() == null) {
            EcosLocoToRoster addLoco = new EcosLocoToRoster(adaptermemo);
            getByEcosObject(ecosObjectIdList.get(row)).allowAddToRoster();
            addLoco.addToQueue(getByEcosObject(ecosObjectIdList.get(row)));
            addLoco.processQueue();
            m.fireTableRowsUpdated(row, row);
        }
    }

    @Override
    protected void setTitle() {
        if (adaptermemo != null) {
            f.setTitle(Bundle.getMessage("XLocoTableTitle", adaptermemo.getUserName()));
        }
        f.setTitle(Bundle.getMessage("EcosLocoTableTitle"));
    }

    @Override
    protected String helpTarget() {
        return "package.jmri.jmrix.ecos.swing.locodatabase.EcosLocoTable"; // very simple help page
    }

    static class RosterBoxRenderer extends GlobalRosterEntryComboBox implements TableCellRenderer {

        public RosterBoxRenderer(RosterEntry re) {
            super();
            setNonSelectedItem(" ");
            if (re == null) {
                setSelectedIndex(0);
            } else {
                setSelectedItem(re);
            }
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                super.setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(table.getBackground());
            }
            if (value == null) {
                setSelectedIndex(0);
            } else {
                setSelectedItem(value);
            }
            return this;
        }
    }

    static class RosterComboBoxEditor extends DefaultCellEditor {

        public RosterComboBoxEditor(GlobalRosterEntryComboBox cb) {
            super(cb);
        }
    }

    @Override
    protected void addPressed(ActionEvent e) {
    }

    @Override
    protected String getClassName() {
        return EcosLocoTableAction.class.getName();
    }

    private final static Logger log = LoggerFactory.getLogger(EcosLocoTableAction.class);

}
