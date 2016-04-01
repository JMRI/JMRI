// SignalGroupTableAction.java
package jmri.jmrit.beantable;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.SignalGroup;
import jmri.SignalHead;
import jmri.SignalMast;
import jmri.util.JmriJFrame;
import jmri.util.swing.JmriBeanComboBox;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a SignalGroup Table
 *
 * Based in part on RouteTableAction.java by Bob Jacobsen
 *
 * @author	Kevin Dickerson Copyright (C) 2010
 *
 */
public class SignalGroupTableAction extends AbstractTableAction implements PropertyChangeListener {

    /**
     *
     */
    private static final long serialVersionUID = -3907892888059515176L;
    static final ResourceBundle rbx = ResourceBundle.getBundle("jmri.jmrit.beantable.LogixTableBundle");

    /**
     * Create an action with a specific title.
     * <P>
     * Note that the argument is the Action title, not the title of the
     * resulting frame. Perhaps this should be changed?
     *
     * @param s
     */
    public SignalGroupTableAction(String s) {
        super(s);
        // disable ourself if there is no primary SignalGroup manager available
        if (jmri.InstanceManager.signalGroupManagerInstance() == null) {
            setEnabled(false);
        }

    }

    public SignalGroupTableAction() {
        this(Bundle.getMessage("TitleSignalGroupTable"));
    }

    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("UpdateCondition")) {
            for (int i = _signalList.size() - 1; i >= 0; i--) {
                SignalGroupSignal signal = _signalList.get(i);
                //String tSysName = signal.getSysName();
                SignalHead sigBean = signal.getBean();
                if (curSignalGroup.isSignalIncluded(sigBean)) {
                    signal.setIncluded(true);
                    signal.setOnState(curSignalGroup.getSignalHeadOnState(sigBean));
                    signal.setOffState(curSignalGroup.getSignalHeadOffState(sigBean));
                } else {
                    signal.setIncluded(false);
                }
            }
        }
    }

    /**
     * Create the JTable DataModel, along with the changes for the specific case
     * of SignalGroups
     */
    protected void createModel() {
        m = new BeanTableDataModel() {
            /**
             *
             */
            private static final long serialVersionUID = 5080048505014587453L;
            static public final int COMMENTCOL = 2;
            static public final int DELETECOL = 3;
            static public final int ENABLECOL = 4;
            static public final int SETCOL = 5;

            public int getColumnCount() {
                return 6;
            }

            public String getColumnName(int col) {
                if (col == SETCOL) {
                    return "";    // no heading on "Edit"
                }
                if (col == ENABLECOL) {
                    return Bundle.getMessage("ColumnHeadEnabled");
                }
                if (col == COMMENTCOL) {
                    return Bundle.getMessage("ColumnComment");
                }
                if (col == DELETECOL) {
                    return "";
                } else {
                    return super.getColumnName(col);
                }
            }

            public Class<?> getColumnClass(int col) {
                if (col == SETCOL) {
                    return JButton.class;
                }
                if (col == ENABLECOL) {
                    return Boolean.class;
                }
                if (col == DELETECOL) {
                    return JButton.class;
                }
                if (col == COMMENTCOL) {
                    return String.class;
                } else {
                    return super.getColumnClass(col);
                }
            }

            public int getPreferredWidth(int col) {
                if (col == SETCOL) {
                    return new JTextField(6).getPreferredSize().width;
                }
                if (col == ENABLECOL) {
                    return new JTextField(6).getPreferredSize().width;
                }
                if (col == COMMENTCOL) {
                    return new JTextField(15).getPreferredSize().width;
                }
                if (col == DELETECOL) {
                    return new JTextField(22).getPreferredSize().width;
                } else {
                    return super.getPreferredWidth(col);
                }
            }

            public boolean isCellEditable(int row, int col) {
                if (col == COMMENTCOL) {
                    return true;
                }
                if (col == SETCOL) {
                    return true;
                }
                if (col == ENABLECOL) {
                    return true;
                }
                if (col == DELETECOL) {
                    return true;
                } else {
                    return super.isCellEditable(row, col);
                }
            }

            public Object getValueAt(int row, int col) {
                NamedBean b;
                if (col == SETCOL) {
                    return Bundle.getMessage("ButtonEdit");
                } else if (col == ENABLECOL) {
                    return Boolean.valueOf(((SignalGroup) getBySystemName((String) getValueAt(row, SYSNAMECOL))).getEnabled());
                    //return true;
                } else if (col == COMMENTCOL) {
                    b = getBySystemName(sysNameList.get(row));
                    return (b != null) ? b.getComment() : null;
                } else if (col == DELETECOL) //
                {
                    return Bundle.getMessage("Delete");
                } else {
                    return super.getValueAt(row, col);
                }
            }

            public void setValueAt(Object value, int row, int col) {
                if (col == SETCOL) {
                    // set up to edit. Use separate Runnable so window is created on top
                    class WindowMaker implements Runnable {

                        int row;

                        WindowMaker(int r) {
                            row = r;
                        }

                        public void run() {
                            addPressed(null);
                            _systemName.setText((String) getValueAt(row, SYSNAMECOL));
                            editPressed(null); // don't really want to stop SignalGroup w/o user action
                        }
                    }
                    WindowMaker t = new WindowMaker(row);
                    javax.swing.SwingUtilities.invokeLater(t);
                } else if (col == ENABLECOL) {
                    // alternate
                    SignalGroup r = (SignalGroup) getBySystemName((String) getValueAt(row, SYSNAMECOL));
                    boolean v = r.getEnabled();
                    r.setEnabled(!v);
                } else if (col == COMMENTCOL) {
                    getBySystemName(sysNameList.get(row)).setComment(
                            (String) value);
                    fireTableRowsUpdated(row, row);
                } else if (col == DELETECOL) {
                    // button fired, delete Bean
                    deleteBean(row, col);
                } else {
                    super.setValueAt(value, row, col);
                }
            }

            public void configureTable(JTable table) {
                table.setDefaultRenderer(Boolean.class, new EnablingCheckboxRenderer());
                table.setDefaultRenderer(JComboBox.class, new jmri.jmrit.symbolicprog.ValueRenderer());
                table.setDefaultEditor(JComboBox.class, new jmri.jmrit.symbolicprog.ValueEditor());
                super.configureTable(table);
            }

            protected void configDeleteColumn(JTable table) {
                // have the delete column hold a button
                SignalGroupTableAction.this.setColumnToHoldButton(table, DELETECOL,
                        new JButton(Bundle.getMessage("Delete")));
            }

            /**
             * Delete the bean after all the checking has been done.
             * <P>
             * Deactivate the light, then use the superclass to delete it.
             */
            void doDelete(NamedBean bean) {
                //((SignalGroup)bean).deActivateSignalGroup();
                super.doDelete(bean);
            }

            // want to update when enabled parameter changes
            protected boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
                if (e.getPropertyName().equals("Enabled")) {
                    return true;
                } //if (e.getPropertyName().equals("Locked")) return true;
                else {
                    return super.matchPropertyName(e);
                }
            }

            public Manager getManager() {
                return jmri.InstanceManager.signalGroupManagerInstance();
            }

            public NamedBean getBySystemName(String name) {
                return jmri.InstanceManager.signalGroupManagerInstance().getBySystemName(name);
            }

            public NamedBean getByUserName(String name) {
                return jmri.InstanceManager.signalGroupManagerInstance().getByUserName(name);
            }

            public int getDisplayDeleteMsg() {
                return 0x00;/*return InstanceManager.getDefault(jmri.UserPreferencesManager.class).getWarnDeleteSignalGroup();*/ }

            public void setDisplayDeleteMsg(int boo) { /*InstanceManager.getDefault(jmri.UserPreferencesManager.class).setWarnDeleteSignalGroup(boo); */

            }

            protected String getMasterClassName() {
                return getClassName();
            }

            public void clickOn(NamedBean t) {
                //((SignalGroup)t).setSignalGroup();
            }

            public String getValue(String s) {
                return "Set";
            }

            public JButton configureButton() {
                return new JButton(" Set ");
            }

            protected String getBeanType() {
                return "Signal Group";
            }
        };
    }

    protected void setTitle() {
        f.setTitle(Bundle.getMessage("TitleSignalGroupTable"));
    }

    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.SignalGroupTable";
    }

    int signalStateFromBox(JComboBox<String> box) {
        String mode = (String) box.getSelectedItem();
        int result = jmri.util.StringUtil.getStateFromName(mode, signalStatesValues, signalStates);

        if (result < 0) {
            log.warn("unexpected mode string in sensorMode: " + mode);
            throw new IllegalArgumentException();
        }
        return result;
    }

    void setSignalStateBox(int mode, JComboBox<String> box) {
        String result = jmri.util.StringUtil.getNameFromState(mode, signalStatesValues, signalStates);
        box.setSelectedItem(result);
    }

    JTextField _systemName = new JTextField(10);
    JTextField _userName = new JTextField(22);

    JmriJFrame addFrame = null;

    SignalGroupSignalModel _SignalGroupSignalModel;
    JScrollPane _SignalGroupSignalScrollPane;

    SignalMastAppearanceModel _AppearanceModel;
    JScrollPane _SignalAppearanceScrollPane;

    //JTextField mainSignal = new JTextField(30);
    JmriBeanComboBox mainSignal;

    ButtonGroup selGroup = null;
    JRadioButton allButton = null;
    JRadioButton includedButton = null;

    JLabel nameLabel = new JLabel(Bundle.getMessage("LabelSystemName"));
    JLabel userLabel = new JLabel(Bundle.getMessage("LabelUserName"));
    JLabel fixedSystemName = new JLabel("xxxxxxxxxxx");

    JButton cancelButton = new JButton(Bundle.getMessage("CancelButton"));
    JButton deleteButton = new JButton(Bundle.getMessage("Delete") + " " + Bundle.getMessage("BeanNameSignalGroup"));
    JButton updateButton = new JButton(Bundle.getMessage("ButtonUpdate"));

    JPanel p2xs = null;   // SignalHead list table
    JPanel p2xsi = null;   // SignalHead list table
    JPanel p3xsi = null;

    SignalGroup curSignalGroup = null;
    boolean SignalGroupDirty = false;  // true to fire reminder to save work
    boolean editMode = false;

    protected void addPressed(ActionEvent e) {
        /*if (editMode) {
         cancelEdit();
         }*/

        jmri.TurnoutManager tm = InstanceManager.turnoutManagerInstance();
        List<String> systemNameList = tm.getSystemNameList();
        _mastAppearancesList = null;

        jmri.SignalHeadManager shm = InstanceManager.signalHeadManagerInstance();
        systemNameList = shm.getSystemNameList();
        _signalList = new ArrayList<SignalGroupSignal>(systemNameList.size());

        Iterator<String> iter = systemNameList.iterator();
        while (iter.hasNext()) {
            String systemName = iter.next();
            if (shm.getBySystemName(systemName).getClass().getName().contains("SingleTurnoutSignalHead")) {
                String userName = shm.getBySystemName(systemName).getUserName();
                _signalList.add(new SignalGroupSignal(systemName, userName));
            } else {
                log.debug("Signal Head " + systemName + " is not a single Turnout Controlled Signal Head");
            }
        }

        // Set up window
        if (addFrame == null) {
            mainSignal = new JmriBeanComboBox(jmri.InstanceManager.signalMastManagerInstance(), null, JmriBeanComboBox.DISPLAYNAME);
            mainSignal.setFirstItemBlank(true);
            addFrame = new JmriJFrame(Bundle.getMessage("AddEditSignalGroup"), false, true);
            addFrame.addHelpMenu("package.jmri.jmrit.beantable.SignalGroupAddEdit", true);
            addFrame.setLocation(100, 30);
            addFrame.getContentPane().setLayout(new BoxLayout(addFrame.getContentPane(), BoxLayout.Y_AXIS));
            Container contentPane = addFrame.getContentPane();
            // add system name
            JPanel ps = new JPanel();
            ps.setLayout(new FlowLayout());
            ps.add(nameLabel);
            ps.add(_systemName);
            _systemName.setToolTipText("Enter system name for new SignalGroup, e.g. R12.");
            ps.add(fixedSystemName);
            fixedSystemName.setVisible(false);
            contentPane.add(ps);
            // add user name
            JPanel p = new JPanel();
            p.setLayout(new FlowLayout());
            p.add(userLabel);
            p.add(_userName);
            _userName.setToolTipText("Enter user name for new SignalGroup, e.g. Junction Indicator on Signal 1.");

            contentPane.add(p);
            // add Turnout Display Choice
            JPanel py = new JPanel();
            py.add(new JLabel("Show "));
            selGroup = new ButtonGroup();
            allButton = new JRadioButton("All", true);
            selGroup.add(allButton);
            py.add(allButton);
            allButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    // Setup for display of all Turnouts, if needed
                    if (!showAll) {
                        showAll = true;
                        _SignalGroupSignalModel.fireTableDataChanged();
                        _AppearanceModel.fireTableDataChanged();
                    }
                }
            });
            includedButton = new JRadioButton("Included", false);
            selGroup.add(includedButton);
            py.add(includedButton);
            includedButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    // Setup for display of included Turnouts only, if needed
                    if (showAll) {
                        showAll = false;
                        initializeIncludedList();
                        _SignalGroupSignalModel.fireTableDataChanged();
                        _AppearanceModel.fireTableDataChanged();

                    }
                }
            });
            py.add(new JLabel("  Mast Appearances and Signals"));
            contentPane.add(py);

            // add control sensor table
            JPanel p3 = new JPanel();
            p3.setLayout(new BoxLayout(p3, BoxLayout.Y_AXIS));
            JPanel p31 = new JPanel();
            p31.add(new JLabel("Enter the Signal Mast that this group is attached to"));
            p3.add(p31);
            JPanel p32 = new JPanel();
            p32.add(new JLabel("Signal Mast: "));
            p32.add(mainSignal);
            //p32.add(new JLabel("Appearance Logic"));
            //p32.add(mainSignalOperation);
//            mainSignal.setText("");
            p3.add(p32);

            p3xsi = new JPanel();
            JPanel p3xsiSpace = new JPanel();
            p3xsiSpace.setLayout(new BoxLayout(p3xsiSpace, BoxLayout.Y_AXIS));
            p3xsiSpace.add(new JLabel(" "));
            p3xsi.add(p3xsiSpace);

            JPanel p31si = new JPanel();
            p31si.setLayout(new BoxLayout(p31si, BoxLayout.Y_AXIS));
            p31si.add(new JLabel(" Select the Mast "));
            p31si.add(new JLabel(" Appearances that"));
            p31si.add(new JLabel(" can trigger this"));
            p31si.add(new JLabel(" Group."));
            p3xsi.add(p31si);
            _AppearanceModel = new SignalMastAppearanceModel();
            JTable SignalAppearanceTable = jmri.util.JTableUtil.sortableDataModel(_AppearanceModel);
            try {
                jmri.util.com.sun.TableSorter tmodel = ((jmri.util.com.sun.TableSorter) SignalAppearanceTable.getModel());
                tmodel.setColumnComparator(String.class, new jmri.util.SystemNameComparator());
                tmodel.setSortingStatus(SignalGroupTableAction.SignalMastAppearanceModel.APPEAR_COLUMN, jmri.util.com.sun.TableSorter.ASCENDING);
            } catch (ClassCastException e3) {
            }  // if not a sortable table model
            SignalAppearanceTable.setRowSelectionAllowed(false);
            SignalAppearanceTable.setPreferredScrollableViewportSize(new java.awt.Dimension(200, 80));
            TableColumnModel SignalAppearanceColumnModel = SignalAppearanceTable.getColumnModel();
            TableColumn includeColumnA = SignalAppearanceColumnModel.
                    getColumn(SignalGroupTableAction.SignalMastAppearanceModel.INCLUDE_COLUMN);
            includeColumnA.setResizable(false);
            includeColumnA.setMinWidth(30);
            includeColumnA.setMaxWidth(60);
            @SuppressWarnings("static-access")
            TableColumn sNameColumnA = SignalAppearanceColumnModel.
                    getColumn(_AppearanceModel.APPEAR_COLUMN);
            sNameColumnA.setResizable(true);
            sNameColumnA.setMinWidth(75);
            sNameColumnA.setMaxWidth(140);

            _SignalAppearanceScrollPane = new JScrollPane(SignalAppearanceTable);
            p3xsi.add(_SignalAppearanceScrollPane, BorderLayout.CENTER);
            p3.add(p3xsi);
            p3xsi.setVisible(true);

            mainSignal.addFocusListener(
                    new FocusListener() {
                        public void focusGained(FocusEvent e) {
                        }

                        public void focusLost(FocusEvent e) {
                            if (curSignalGroup == null) {
                                setValidSignalAppearances();
                            } else if (mainSignal.getSelectedBean() != curSignalGroup.getSignalMast()) {
                                setValidSignalAppearances();
                            }
                        }
                    }
            );

            // complete this panel
            Border p3Border = BorderFactory.createEtchedBorder();
            p3.setBorder(p3Border);
            contentPane.add(p3);

            p2xsi = new JPanel();
            JPanel p2xsiSpace = new JPanel();
            p2xsiSpace.setLayout(new BoxLayout(p2xsiSpace, BoxLayout.Y_AXIS));
            p2xsiSpace.add(new JLabel("XXX"));
            p2xsi.add(p2xsiSpace);

            JPanel p21si = new JPanel();
            p21si.setLayout(new BoxLayout(p21si, BoxLayout.Y_AXIS));
            p21si.add(new JLabel("Please select "));
            p21si.add(new JLabel(" Signals to "));
            p21si.add(new JLabel(" be included "));
            p21si.add(new JLabel(" in this Group."));
            p2xsi.add(p21si);
            _SignalGroupSignalModel = new SignalGroupSignalModel();
            JTable SignalGroupSignalTable = jmri.util.JTableUtil.sortableDataModel(_SignalGroupSignalModel);
            try {
                jmri.util.com.sun.TableSorter tmodel = ((jmri.util.com.sun.TableSorter) SignalGroupSignalTable.getModel());
                tmodel.setColumnComparator(String.class, new jmri.util.SystemNameComparator());
                tmodel.setSortingStatus(SignalGroupSignalModel.SNAME_COLUMN, jmri.util.com.sun.TableSorter.ASCENDING);
            } catch (ClassCastException e3) {
            }  // if not a sortable table model
            SignalGroupSignalTable.setRowSelectionAllowed(false);
            SignalGroupSignalTable.setPreferredScrollableViewportSize(new java.awt.Dimension(480, 160));
            TableColumnModel SignalGroupSignalColumnModel = SignalGroupSignalTable.getColumnModel();
            TableColumn includeColumnSi = SignalGroupSignalColumnModel.
                    getColumn(SignalGroupSignalModel.INCLUDE_COLUMN);
            includeColumnSi.setResizable(false);
            includeColumnSi.setMinWidth(30);
            includeColumnSi.setMaxWidth(60);
            TableColumn sNameColumnSi = SignalGroupSignalColumnModel.
                    getColumn(SignalGroupSignalModel.SNAME_COLUMN);
            sNameColumnSi.setResizable(true);
            sNameColumnSi.setMinWidth(75);
            sNameColumnSi.setMaxWidth(95);
            TableColumn uNameColumnSi = SignalGroupSignalColumnModel.
                    getColumn(SignalGroupSignalModel.UNAME_COLUMN);
            uNameColumnSi.setResizable(true);
            uNameColumnSi.setMinWidth(100);
            uNameColumnSi.setMaxWidth(260);
            TableColumn stateColumnSi = SignalGroupSignalColumnModel.
                    getColumn(SignalGroupSignalModel.STATE_ON_COLUMN);
            stateColumnSi.setResizable(false);
            stateColumnSi.setMinWidth(50);
            stateColumnSi.setMaxWidth(100);

            TableColumn stateOffColumnSi = SignalGroupSignalColumnModel.
                    getColumn(SignalGroupSignalModel.STATE_OFF_COLUMN);
            stateOffColumnSi.setResizable(false);
            stateOffColumnSi.setMinWidth(50);
            stateOffColumnSi.setMaxWidth(100);
            JButton editButton = new JButton(Bundle.getMessage("ButtonEdit"));
            TableColumn editColumnSi = SignalGroupSignalColumnModel.
                    getColumn(SignalGroupSignalModel.EDIT_COLUMN);
            editColumnSi.setResizable(false);
            editColumnSi.setMinWidth(50);
            editColumnSi.setMaxWidth(100);
            setColumnToHoldButton(SignalGroupSignalTable, SignalGroupSignalModel.EDIT_COLUMN, editButton);

            _SignalGroupSignalScrollPane = new JScrollPane(SignalGroupSignalTable);
            p2xsi.add(_SignalGroupSignalScrollPane, BorderLayout.CENTER);
            contentPane.add(p2xsi);
            p2xsi.setVisible(true);

            // add notes panel
            JPanel pa = new JPanel();
            pa.setLayout(new BoxLayout(pa, BoxLayout.Y_AXIS));

            Border pBorder = BorderFactory.createEtchedBorder();
            pa.setBorder(pBorder);
            contentPane.add(pa);
            // buttons at bottom of window
            JPanel pb = new JPanel();
            pb.setLayout(new FlowLayout(FlowLayout.TRAILING));

            pb.add(cancelButton);
            cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) { cancelPressed(e); }
            });
            pb.add(deleteButton);
            deleteButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    deletePressed(e);
                }
            });
            deleteButton.setToolTipText(Bundle.getMessage("DeleteSignalGroupInSystem"));
            // Update SignalGroup button
            pb.add(updateButton);
            updateButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    updatePressed(e, false, true);
                }
            });
            updateButton.setToolTipText("Change this SignalGroup and leave Edit mode");  // I18N TODO
            updateButton.setVisible(true);
            contentPane.add(pb);
            // pack and release space
            addFrame.pack();
            p2xsiSpace.setVisible(false);
        } // set listener for window closing
        else {
            mainSignal.setSelectedBean(null);
        }
        addFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                // remind to save, if SignalGroup was created or edited
                if (SignalGroupDirty) {
                    InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                            showInfoMessage("Reminder", "Remember to save your Signal Group information.", "beantable.SignalGroupTableAction", "remindSignalGroup");
                    SignalGroupDirty = false;
                }
                // hide addFrame
                addFrame.setVisible(false);
                finishUpdate();
                _SignalGroupSignalModel.dispose();
                _AppearanceModel.dispose();

            }
        });
        // display the window
        addFrame.setVisible(true);

    }

    void setColumnToHoldButton(JTable table, int column, JButton sample) {
        //TableColumnModel tcm = table.getColumnModel();
        // install a button renderer & editor
        ButtonRenderer buttonRenderer = new ButtonRenderer();
        table.setDefaultRenderer(JButton.class, buttonRenderer);
        TableCellEditor buttonEditor = new ButtonEditor(new JButton());
        table.setDefaultEditor(JButton.class, buttonEditor);
        // ensure the table rows, columns have enough room for buttons
        table.setRowHeight(sample.getPreferredSize().height);
        table.getColumnModel().getColumn(column)
                .setPreferredWidth((sample.getPreferredSize().width) + 4);
    }

    /**
     * Initialize list of included turnout positions
     */
    void initializeIncludedList() {
        _includedMastAppearancesList = new ArrayList<SignalMastAppearances>();
        for (int i = 0; i < _mastAppearancesList.size(); i++) {
            if (_mastAppearancesList.get(i).isIncluded()) {
                _includedMastAppearancesList.add(_mastAppearancesList.get(i));
            }

        }
        _includedSignalList = new ArrayList<SignalGroupSignal>();
        for (int i = 0; i < _signalList.size(); i++) {
            if (_signalList.get(i).isIncluded()) {
                _includedSignalList.add(_signalList.get(i));
            }
        }
    }

    boolean checkNewNamesOK() {
        // Get system name and user name
        String sName = _systemName.getText().toUpperCase();
        String uName = _userName.getText();
        if (sName.length() == 0) {
            javax.swing.JOptionPane.showMessageDialog(null, "System Name field can not be left blank", "System Name Error", javax.swing.JOptionPane.WARNING_MESSAGE);
            return false;
        }
        SignalGroup g = null;
        // check if a SignalGroup with the same user name exists
        if (!uName.equals("")) {
            g = jmri.InstanceManager.signalGroupManagerInstance().getByUserName(uName);
            if (g != null) {
                // SignalGroup with this user name already exists
                javax.swing.JOptionPane.showMessageDialog(null, "Signal Group with this username already exists", "User Name Error", javax.swing.JOptionPane.WARNING_MESSAGE);
                return false;
            } else {
                return true;
            }
        }
        // check if a SignalGroup with this system name already exists
        g = jmri.InstanceManager.signalGroupManagerInstance().getBySystemName(sName);
        if (g != null) {
            // SignalGroup already exists
            javax.swing.JOptionPane.showMessageDialog(null, "A SignalGroup with this system name already exists", "System Name Error", javax.swing.JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    boolean checkValidSignalMast() {
        SignalMast mMast = (SignalMast) mainSignal.getSelectedBean();
        if (mMast == null) {
            log.warn("Signal Mast not selected");
            javax.swing.JOptionPane.showMessageDialog(null, "SignalMast not selected.", "Error", javax.swing.JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    SignalGroup checkNamesOK() {
        // Get system name and user name
        String sName = _systemName.getText().toUpperCase();
        String uName = _userName.getText();
        if (sName.length() == 0) {
            javax.swing.JOptionPane.showMessageDialog(null, "Please enter a System Name and User Name.", "Error", javax.swing.JOptionPane.WARNING_MESSAGE);
            return null;
        }
        SignalGroup g = jmri.InstanceManager.signalGroupManagerInstance().provideSignalGroup(sName, uName);
        if (g == null) {
            // should never get here
            log.error("Unknown failure to create SignalGroup with System Name: " + sName);
        }
        return g;
    }

    int setSignalInformation(SignalGroup g) {
        for (int i = 0; i < g.getNumSignalHeadItems(); i++) {
            SignalHead sig = g.getSignalHeadItemBeanByIndex(i);
            boolean valid = false;
            for (int x = 0; x < _includedSignalList.size(); x++) {
                SignalGroupSignal s = _includedSignalList.get(x);
                //String si = s.getBean();
                if (sig == s.getBean()) {
                    valid = true;
                    break;
                }
            }
            if (!valid) {
                g.deleteSignalHead(sig);
            }
        }

        for (int i = 0; i < _includedSignalList.size(); i++) {
            SignalGroupSignal s = _includedSignalList.get(i);
            SignalHead sig = s.getBean();
            if (!g.isSignalIncluded(sig)) {
                g.addSignalHead(sig);
                g.setSignalHeadOnState(sig, s.getOnStateInt());
                g.setSignalHeadOffState(sig, s.getOffStateInt());
            }
        }

        return _includedSignalList.size();
    }

    void setMastAppearanceInformation(SignalGroup g) {
        g.clearSignalMastAppearance();
        for (int x = 0; x < _includedMastAppearancesList.size(); x++) {
            g.addSignalMastAppearance(_includedMastAppearancesList.get(x).getAppearance());
        }
    }

    /**
     * Sets the Sensor, Turnout, and delay control information for adding or
     * editting if any
     */
    /*void setControlInformation(SignalGroup g) {
     // Get sensor control information if any
     //Not sure about this...
     String signalSystemName = mainSignal.getSelectedBean();
     if(
     if (signalSystemName.length() > 0) {
     SignalHead s1 = InstanceManager.signalHeadManagerInstance().getSignalHead(signalSystemName);
     if (s1!=null)
     g.setSignalMast(signalSystemName);
     }
     }*/
    void setValidSignalAppearances() {
        jmri.SignalMast sh = (SignalMast) mainSignal.getSelectedBean();
        if (sh == null) {
            return;
        }
        java.util.Vector<String> appear = sh.getValidAspects();

        _mastAppearancesList = new ArrayList<SignalMastAppearances>(appear.size());
        for (int i = 0; i < appear.size(); i++) {
            _mastAppearancesList.add(new SignalMastAppearances(appear.get(i)));
        }
        _AppearanceModel.fireTableDataChanged();
    }

    void cancelPressed(ActionEvent e) {
        addFrame.setVisible(false);
        addFrame.dispose();
        addFrame = null;
    }

    /**
     * Responds to the Edit button
     */
    void editPressed(ActionEvent e) {
        // identify the SignalGroup with this name if it already exists
        String sName = _systemName.getText().toUpperCase();
        SignalGroup g = jmri.InstanceManager.signalGroupManagerInstance().getBySystemName(sName);
        if (g == null) {
            // SignalGroup does not exist, so cannot be edited
            return;
        }
        g.addPropertyChangeListener(this);

        // SignalGroup was found, make its system name not changeable
        curSignalGroup = g;

        jmri.SignalMast sh = jmri.InstanceManager.signalMastManagerInstance().getSignalMast(g.getSignalMastName());
        java.util.Vector<String> appear = sh.getValidAspects();

        _mastAppearancesList = new ArrayList<SignalMastAppearances>(appear.size());

        for (int i = 0; i < appear.size(); i++) {
            _mastAppearancesList.add(new SignalMastAppearances(appear.get(i)));
        }

        fixedSystemName.setText(sName);
        fixedSystemName.setVisible(true);
        _systemName.setVisible(false);
        //mainSignal.setText(g.getSignalMastName());
        mainSignal.setSelectedBean(g.getSignalMast());

        _userName.setText(g.getUserName());
        int setRow = 0;

        for (int i = _signalList.size() - 1; i >= 0; i--) {
            SignalGroupSignal signal = _signalList.get(i);
            //String tSysName = signal.getSysName();
            SignalHead sigBean = signal.getBean();
            if (g.isSignalIncluded(sigBean)) {
                signal.setIncluded(true);
                signal.setOnState(g.getSignalHeadOnState(sigBean));
                signal.setOffState(g.getSignalHeadOffState(sigBean));
                setRow = i;
            } else {
                signal.setIncluded(false);
                //signal.setOnState(SignalHead.DARK);
                //signal.setOffState(SignalHead.DARK);
            }
        }
        _SignalGroupSignalScrollPane.getVerticalScrollBar().setValue(setRow * ROW_HEIGHT);
        _SignalGroupSignalModel.fireTableDataChanged();

        for (int i = 0; i < _mastAppearancesList.size(); i++) {
            SignalMastAppearances appearance = _mastAppearancesList.get(i);
            String app = appearance.getAppearance();
            if (g.isSignalMastAppearanceIncluded(app)) {
                appearance.setIncluded(true);
                setRow = i;
            } else {
                appearance.setIncluded(false);
            }

        }
        _SignalAppearanceScrollPane.getVerticalScrollBar().setValue(setRow * ROW_HEIGHT);

        _AppearanceModel.fireTableDataChanged();
        initializeIncludedList();

        updateButton.setVisible(true);
        fixedSystemName.setVisible(true);
        _systemName.setVisible(false);
        editMode = true;
    }

    /**
     * Responds to the Delete button
     */
    void deletePressed(ActionEvent e) {
        InstanceManager.signalGroupManagerInstance().deleteSignalGroup(curSignalGroup);
        curSignalGroup = null;
        finishUpdate();
    }

    /**
     * Responds to the Update button - update to SignalGroup Table
     */
    void updatePressed(ActionEvent e, boolean newSignalGroup, boolean close) {
        // Check if the User Name has been changed
        if (curSignalGroup == null) {
            if (!checkNewNamesOK()) {
                return;
            }
        }
        if (!checkValidSignalMast()) {
            return;
        }
        String uName = _userName.getText();
        SignalGroup g = checkNamesOK();
        if (g == null) {
            return;
        }
        curSignalGroup = g;
        // user name is unique, change it
        g.setUserName(uName);
        initializeIncludedList();
        setSignalInformation(g);
        setMastAppearanceInformation(g);

        g.setSignalMast((SignalMast) mainSignal.getSelectedBean(), mainSignal.getSelectedDisplayName());
        if (close) {
            finishUpdate();
        }
    }

    void finishUpdate() {
        if (curSignalGroup != null) {
            curSignalGroup.removePropertyChangeListener(this);
        }
        _systemName.setVisible(true);
        fixedSystemName.setVisible(false);
        _systemName.setText("");
        _userName.setText("");
        mainSignal.setSelectedBean(null);
        for (int i = _signalList.size() - 1; i >= 0; i--) {
            _signalList.get(i).setIncluded(false);
        }
        for (int i = _mastAppearancesList.size() - 1; i >= 0; i--) {
            _mastAppearancesList.get(i).setIncluded(false);
        }
        // mainSignal.setText("");
        curSignalGroup = null;
        addFrame.setVisible(false);

    }

    public class SignalMastAppearanceModel extends AbstractTableModel implements PropertyChangeListener {

        /**
         *
         */
        private static final long serialVersionUID = -8196267502015148289L;

        public Class<?> getColumnClass(int c) {
            if (c == INCLUDE_COLUMN) {
                return Boolean.class;
            } else {
                return String.class;
            }
        }

        public String getColumnName(int col) {
            if (col == INCLUDE_COLUMN) {
                return rbx.getString("ColumnLabelInclude");
            }
            if (col == APPEAR_COLUMN) {
                return rbx.getString("LabelActionSignal");
            }
            return "";
        }

        public void dispose() {
            InstanceManager.signalMastManagerInstance().removePropertyChangeListener(this);
        }

        public void propertyChange(java.beans.PropertyChangeEvent e) {
            if (e.getPropertyName().equals("length")) {
                // a new NamedBean is available in the manager
                fireTableDataChanged();
            }
        }

        public int getColumnCount() {
            return 2;
        }

        public boolean isCellEditable(int r, int c) {
            return ((c == INCLUDE_COLUMN));
        }

        public static final int APPEAR_COLUMN = 0;
        public static final int INCLUDE_COLUMN = 1;

        public void setSetToState(String x) {
        }

        public int getRowCount() {
            if (_mastAppearancesList == null) {
                return 0;
            }
            if (showAll) {
                return _mastAppearancesList.size();
            } else {
                return _includedMastAppearancesList.size();
            }
        }

        public Object getValueAt(int r, int c) {
            ArrayList<SignalMastAppearances> appearList = null;
            if (showAll) {
                appearList = _mastAppearancesList;
            } else {
                appearList = _includedMastAppearancesList;
            }
            // some error checking
            if (r >= appearList.size()) {
                log.debug("row is greater than turnout list size");
                return null;
            }
            switch (c) {
                case INCLUDE_COLUMN:
                    return Boolean.valueOf(appearList.get(r).isIncluded());
                case APPEAR_COLUMN:  // slot number
                    return appearList.get(r).getAppearance();
                default:
                    return null;
            }
        }

        public void setValueAt(Object type, int r, int c) {
            ArrayList<SignalMastAppearances> appearList = null;
            if (showAll) {
                appearList = _mastAppearancesList;
            } else {
                appearList = _includedMastAppearancesList;
            }
            switch (c) {
                case INCLUDE_COLUMN:
                    appearList.get(r).setIncluded(((Boolean) type).booleanValue());
                    break;
                case APPEAR_COLUMN:
                    appearList.get(r).setAppearance((String) type);
                    break;
                default:
                    break;
            }
        }

    }

    /**
     * Base table model for selecting outputs
     */
    public abstract class SignalGroupOutputModel extends AbstractTableModel implements PropertyChangeListener {

        /**
         *
         */
        private static final long serialVersionUID = -7170451044063687952L;

        public Class<?> getColumnClass(int c) {
            if (c == INCLUDE_COLUMN) {
                return Boolean.class;
            } else {
                return String.class;
            }
        }

        public void propertyChange(java.beans.PropertyChangeEvent e) {
            if (e.getPropertyName().equals("length")) {
                // a new NamedBean is available in the manager
                fireTableDataChanged();
            } else if (e.getPropertyName().equals("UpdateCondition")) {
                fireTableDataChanged();
            }
        }

        public String getColumnName(int c) {
            return COLUMN_NAMES[c];
        }

        public int getColumnCount() {
            return 4;
        }

        public boolean isCellEditable(int r, int c) {
            return ((c == INCLUDE_COLUMN) || (c == STATE_COLUMN));
        }

        public static final int SNAME_COLUMN = 0;
        public static final int UNAME_COLUMN = 1;
        public static final int INCLUDE_COLUMN = 2;
        public static final int STATE_COLUMN = 3;

    }

    class SignalGroupSignalModel extends SignalGroupOutputModel {

        /**
         *
         */
        private static final long serialVersionUID = 3600677786771301391L;

        SignalGroupSignalModel() {
            InstanceManager.signalHeadManagerInstance().addPropertyChangeListener(this);
        }

        public boolean isCellEditable(int r, int c) {
            return ((c == INCLUDE_COLUMN) || (c == STATE_ON_COLUMN) || (c == STATE_OFF_COLUMN) || (c == EDIT_COLUMN));
        }

        public int getColumnCount() {
            return 6;
        }

        public static final int STATE_ON_COLUMN = 3;
        public static final int STATE_OFF_COLUMN = 4;
        public static final int EDIT_COLUMN = 5;

        public Class<?> getColumnClass(int c) {
            if (c == INCLUDE_COLUMN) {
                return Boolean.class;
            } else if (c == EDIT_COLUMN) {
                return JButton.class;
            } else {
                return String.class;
            }
        }

        public String getColumnName(int c) {
            return COLUMN_SIG_NAMES[c];
        }

        public void setSetToState(String x) {
        }

        public int getRowCount() {
            if (showAll) {
                return _signalList.size();
            } else {
                return _includedSignalList.size();
            }
        }

        public Object getValueAt(int r, int c) {
            ArrayList<SignalGroupSignal> signalList = null;
            if (showAll) {
                signalList = _signalList;
            } else {
                signalList = _includedSignalList;
            }
            // some error checking
            if (r >= signalList.size()) {
                log.debug("row is greater than turnout list size");
                return null;
            }
            switch (c) {
                case INCLUDE_COLUMN:
                    return Boolean.valueOf(signalList.get(r).isIncluded());
                case SNAME_COLUMN:  // slot number
                    return signalList.get(r).getSysName();
                case UNAME_COLUMN:  //
                    return signalList.get(r).getUserName();
                case STATE_ON_COLUMN:  //
                    return signalList.get(r).getOnState();
                case STATE_OFF_COLUMN:  //
                    return signalList.get(r).getOffState();
                case EDIT_COLUMN:
                    return (Bundle.getMessage("ButtonEdit"));
                default:
                    return null;
            }
        }

        public String getDisplayName(int r) {
            if (((String) getValueAt(r, UNAME_COLUMN) != null) && (!((String) getValueAt(r, UNAME_COLUMN)).equals(""))) {
                return (String) getValueAt(r, UNAME_COLUMN);
            } else {
                return (String) getValueAt(r, SNAME_COLUMN);
            }
        }

        public SignalHead getBean(int r) {
            return jmri.InstanceManager.signalHeadManagerInstance().getSignalHead((String) getValueAt(r, SNAME_COLUMN));
        }

        public void setValueAt(Object type, int r, int c) {
            ArrayList<SignalGroupSignal> signalList = null;
            if (showAll) {
                signalList = _signalList;
            } else {
                signalList = _includedSignalList;
            }
            switch (c) {
                case INCLUDE_COLUMN:
                    signalList.get(r).setIncluded(((Boolean) type).booleanValue());
                    break;
                case STATE_ON_COLUMN:
                    signalList.get(r).setSetOnState((String) type);
                    break;
                case STATE_OFF_COLUMN:
                    signalList.get(r).setSetOffState((String) type);
                    break;
                case EDIT_COLUMN:
                    signalList.get(r).setIncluded(((Boolean) true).booleanValue());
                    class WindowMaker implements Runnable {

                        final int row;

                        WindowMaker(int r) {
                            row = r;
                        }

                        public void run() {
                            signalEditPressed(row);
                            //_systemName.setText((String)getValueAt(row, SYSNAMECOL));
                            //editPressed(null); 
                        }
                    }
                    WindowMaker t = new WindowMaker(r);
                    javax.swing.SwingUtilities.invokeLater(t);
                    break;
                default:
                    break;
            }
        }

        public void dispose() {
            InstanceManager.signalHeadManagerInstance().removePropertyChangeListener(this);
        }
    }

    JmriJFrame signalEditFrame = null;

    void signalEditPressed(int row) {
        if (curSignalGroup == null) {
            if (!checkNewNamesOK()) {
                return;
            }
            if (!checkValidSignalMast()) {
                return;
            }
            updatePressed(null, true, false);
        }
        if (!curSignalGroup.isSignalIncluded(_SignalGroupSignalModel.getBean(row))) {
            curSignalGroup.addSignalHead(_SignalGroupSignalModel.getBean(row));
        }
        _SignalGroupSignalModel.fireTableDataChanged();
        SignalGroupSubTableAction editSignalHead = new SignalGroupSubTableAction();
        editSignalHead.editSignal(curSignalGroup, _SignalGroupSignalModel.getDisplayName(row));
    }

    private boolean showAll = true;   // false indicates show only included Turnouts

    private static int ROW_HEIGHT;

    private static String[] COLUMN_NAMES = {rbx.getString("ColumnLabelSystemName"),
        rbx.getString("ColumnLabelUserName"),
        rbx.getString("ColumnLabelInclude"),
        rbx.getString("ColumnLabelSetState")};
    private static String[] COLUMN_SIG_NAMES = {rbx.getString("ColumnLabelSystemName"),
        rbx.getString("ColumnLabelUserName"),
        rbx.getString("ColumnLabelInclude"),
        "On State", "Off State", "Edit"};

    private static String[] signalStates = new String[]{rbx.getString("StateSignalHeadDark"), rbx.getString("StateSignalHeadRed"), rbx.getString("StateSignalHeadYellow"), rbx.getString("StateSignalHeadGreen"), rbx.getString("StateSignalHeadLunar")};
    private static int[] signalStatesValues = new int[]{SignalHead.DARK, SignalHead.RED, SignalHead.YELLOW, SignalHead.GREEN, SignalHead.LUNAR};

    private ArrayList<SignalGroupSignal> _signalList;        // array of all Sensorsy
    private ArrayList<SignalGroupSignal> _includedSignalList;

    private ArrayList<SignalMastAppearances> _mastAppearancesList;        // array of all Sensorsy
    private ArrayList<SignalMastAppearances> _includedMastAppearancesList;

    private static class SignalGroupSignal {

        //String _sysName=null;
        //String _userName=null;
        SignalHead _signal = null;
        boolean _included;

        SignalGroupSignal(String sysName, String userName) {
            /*_sysName = sysName;
             _userName = userName;*/
            _included = false;
            if (InstanceManager.signalHeadManagerInstance().getBySystemName(sysName).getClass().getName().contains("SingleTurnoutSignalHead")) {
                jmri.implementation.SingleTurnoutSignalHead signal = (jmri.implementation.SingleTurnoutSignalHead) InstanceManager.signalHeadManagerInstance().getBySystemName(sysName);
                _onState = signal.getOnAppearance();
                _offState = signal.getOffAppearance();
                _signal = signal;
            }

        }

        SignalHead getBean() {
            return _signal;
        }

        String getSysName() {
            return _signal.getSystemName();
        }

        String getUserName() {
            return _signal.getUserName();
        }

        /*String getDisplayName(){
         return _signal.getDisplayName();
         }*/
        boolean isIncluded() {
            return _included;
        }

        void setIncluded(boolean include) {
            _included = include;
        }

        String getOnState() {
            switch (_onState) {
                case SignalHead.DARK:
                    return rbx.getString("StateSignalHeadDark");
                case SignalHead.RED:
                    return rbx.getString("StateSignalHeadRed");
                case SignalHead.YELLOW:
                    return rbx.getString("StateSignalHeadYellow");
                case SignalHead.GREEN:
                    return rbx.getString("StateSignalHeadGreen");
                case SignalHead.LUNAR:
                    return rbx.getString("StateSignalHeadLunar");
                case SignalHead.FLASHRED:
                    return rbx.getString("StateSignalHeadFlashRed");
                case SignalHead.FLASHYELLOW:
                    return rbx.getString("StateSignalHeadFlashYellow");
                case SignalHead.FLASHGREEN:
                    return rbx.getString("StateSignalHeadFlashGreen");
                case SignalHead.FLASHLUNAR:
                    return rbx.getString("StateSignalHeadFlashLunar");
            }
            return "";
        }

        String getOffState() {
            switch (_offState) {
                case SignalHead.DARK:
                    return rbx.getString("StateSignalHeadDark");
                case SignalHead.RED:
                    return rbx.getString("StateSignalHeadRed");
                case SignalHead.YELLOW:
                    return rbx.getString("StateSignalHeadYellow");
                case SignalHead.GREEN:
                    return rbx.getString("StateSignalHeadGreen");
                case SignalHead.LUNAR:
                    return rbx.getString("StateSignalHeadLunar");
                case SignalHead.FLASHRED:
                    return rbx.getString("StateSignalHeadFlashRed");
                case SignalHead.FLASHYELLOW:
                    return rbx.getString("StateSignalHeadFlashYellow");
                case SignalHead.FLASHGREEN:
                    return rbx.getString("StateSignalHeadFlashGreen");
                case SignalHead.FLASHLUNAR:
                    return rbx.getString("StateSignalHeadFlashLunar");
            }
            return "";
        }

        int getOnStateInt() {
            return _onState;
        }

        int getOffStateInt() {
            return _offState;
        }

        void setSetOnState(String state) {
            if (state.equals(rbx.getString("StateSignalHeadDark"))) {
                _onState = SignalHead.DARK;
            } else if (state.equals(rbx.getString("StateSignalHeadRed"))) {
                _onState = SignalHead.RED;
            } else if (state.equals(rbx.getString("StateSignalHeadYellow"))) {
                _onState = SignalHead.YELLOW;
            } else if (state.equals(rbx.getString("StateSignalHeadGreen"))) {
                _onState = SignalHead.GREEN;
            } else if (state.equals(rbx.getString("StateSignalHeadLunar"))) {
                _onState = SignalHead.LUNAR;
            } else if (state.equals(rbx.getString("StateSignalHeadFlashRed"))) {
                _onState = SignalHead.FLASHRED;
            } else if (state.equals(rbx.getString("StateSignalHeadFlashYellow"))) {
                _onState = SignalHead.FLASHYELLOW;
            } else if (state.equals(rbx.getString("StateSignalHeadFlashGreen"))) {
                _onState = SignalHead.FLASHGREEN;
            } else if (state.equals(rbx.getString("StateSignalHeadFlashLunar"))) {
                _onState = SignalHead.FLASHLUNAR;
            }
        }

        void setSetOffState(String state) {
            if (state.equals(rbx.getString("StateSignalHeadDark"))) {
                _offState = SignalHead.DARK;
            } else if (state.equals(rbx.getString("StateSignalHeadRed"))) {
                _offState = SignalHead.RED;
            } else if (state.equals(rbx.getString("StateSignalHeadYellow"))) {
                _offState = SignalHead.YELLOW;
            } else if (state.equals(rbx.getString("StateSignalHeadGreen"))) {
                _offState = SignalHead.GREEN;
            } else if (state.equals(rbx.getString("StateSignalHeadLunar"))) {
                _offState = SignalHead.LUNAR;
            } else if (state.equals(rbx.getString("StateSignalHeadFlashRed"))) {
                _offState = SignalHead.FLASHRED;
            } else if (state.equals(rbx.getString("StateSignalHeadFlashYellow"))) {
                _offState = SignalHead.FLASHYELLOW;
            } else if (state.equals(rbx.getString("StateSignalHeadFlashGreen"))) {
                _offState = SignalHead.FLASHGREEN;
            } else if (state.equals(rbx.getString("StateSignalHeadFlashLunar"))) {
                _offState = SignalHead.FLASHLUNAR;
            }
        }

        int _onState = 0x00;
        int _offState = 0x00;

        public void setOnState(int state) {
            _onState = state;
        }

        public void setOffState(int state) {
            _offState = state;
        }
    }

    private static class SignalMastAppearances {

        SignalMastAppearances(String appearance) {
            _appearance = appearance;
        }
        boolean _include;
        String _appearance;

        void setIncluded(boolean include) {
            _include = include;
        }

        boolean isIncluded() {
            return _include;
        }

        void setAppearance(String app) {
            _appearance = app;
        }

        String getAppearance() {
            return _appearance;
        }

    }

    protected String getClassName() {
        return SignalGroupTableAction.class.getName();
    }

    public String getClassDescription() {
        return Bundle.getMessage("TitleSignalGroupTable");
    }

    private final static Logger log = LoggerFactory.getLogger(SignalGroupTableAction.class.getName());
}
