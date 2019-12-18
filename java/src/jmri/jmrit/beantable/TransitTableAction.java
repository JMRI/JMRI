package jmri.jmrit.beantable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import jmri.Block;
import jmri.EntryPoint;
import jmri.InstanceManager;
import jmri.Section;
import jmri.SectionManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.SignalHead;
import jmri.SignalHeadManager;
import jmri.SignalMast;
import jmri.SignalMastManager;
import jmri.Transit;
import jmri.TransitManager;
import jmri.TransitSection;
import jmri.TransitSectionAction;
import jmri.NamedBean.DisplayOptions;
import jmri.util.JmriJFrame;
import jmri.swing.NamedBeanComboBox;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a TransitTable GUI.
 *
 * @author Dave Duchamp Copyright (C) 2008, 2010, 2011
 */
public class TransitTableAction extends AbstractTableAction<Transit> {

    /**
     * Create an action with a specific title.
     * <p>
     * Note that the argument is the Action title, not the title of the
     * resulting frame. Perhaps this should be changed?
     *
     * @param actionName action title
     */
    public TransitTableAction(String actionName) {
        super(actionName);

        transitManager = jmri.InstanceManager.getNullableDefault(jmri.TransitManager.class);
        // disable ourself if there is no Transit manager available
        if (sectionManager == null || transitManager == null) {
            setEnabled(false);
        }
        updateSensorList();
    }

    public TransitTableAction() {
        this(Bundle.getMessage("TitleTransitTable"));
    }

    static final ResourceBundle rbx = ResourceBundle.getBundle("jmri.jmrit.beantable.SectionTransitTableBundle");

    /**
     * Create the JTable DataModel, along with the changes for the specific case
     * of Transit objects.
     */
    @Override
    protected void createModel() {
        m = new BeanTableDataModel<Transit>() {

            static public final int EDITCOL = NUMCOLUMN;
            static public final int DUPLICATECOL = EDITCOL + 1;

            @Override
            public String getValue(String name) {
                if (name == null) {
                    log.warn("requested getValue(null)");
                    return "(no name)";
                }
                Transit z = InstanceManager.getDefault(jmri.TransitManager.class).getBySystemName(name);
                if (z == null) {
                    log.debug("requested getValue(\"{}\"), Transit doesn't exist", name);
                    return "(no Transit)";
                }
                return "Transit";
            }

            @Override
            public TransitManager getManager() {
                return InstanceManager.getDefault(jmri.TransitManager.class);
            }

            @Override
            public Transit getBySystemName(String name) {
                return InstanceManager.getDefault(jmri.TransitManager.class).getBySystemName(name);
            }

            @Override
            public Transit getByUserName(String name) {
                return InstanceManager.getDefault(jmri.TransitManager.class).getByUserName(name);
            }

            @Override
            protected String getMasterClassName() {
                return getClassName();
            }

            @Override
            public void clickOn(Transit t) {
            }

            @Override
            public int getColumnCount() {
                return DUPLICATECOL + 1;
            }

            @Override
            public Object getValueAt(int row, int col) {
                if (col == VALUECOL) {
                    // some error checking
                    if (row >= sysNameList.size()) {
                        log.debug("row is greater than name list");
                        return "";
                    }
                    Transit z = getBySystemName(sysNameList.get(row));
                    if (z == null) {
                        return "";
                    } else {
                        int state = z.getState();
                        if (state == Transit.IDLE) {
                            return (rbx.getString("TransitIdle"));
                        } else if (state == Transit.ASSIGNED) {
                            return (rbx.getString("TransitAssigned"));
                        }
                    }
                } else if (col == EDITCOL) {
                    return Bundle.getMessage("ButtonEdit");
                } else if (col == DUPLICATECOL) {
                    return rbx.getString("ButtonDuplicate");
                } else {
                    return super.getValueAt(row, col);
                }
                return null;
            }

            @Override
            public void setValueAt(Object value, int row, int col) {
                if (col == EDITCOL) {
                    class WindowMaker implements Runnable {

                        int row;

                        WindowMaker(int r) {
                            row = r;
                        }

                        @Override
                        public void run() {
                            String sName = ((Transit) getValueAt(row, SYSNAMECOL)).getSystemName();
                            editPressed(sName);
                        }
                    }
                    WindowMaker t = new WindowMaker(row);
                    javax.swing.SwingUtilities.invokeLater(t);
                } else if (col == DUPLICATECOL) {
                    // set up to duplicate
                    class WindowMaker implements Runnable {

                        int row;

                        WindowMaker(int r) {
                            row = r;
                        }

                        @Override
                        public void run() {
                            String sName = ((Transit) getValueAt(row, SYSNAMECOL)).getSystemName();
                            duplicatePressed(sName);
                        }
                    }
                    WindowMaker t = new WindowMaker(row);
                    javax.swing.SwingUtilities.invokeLater(t);
                } else {
                    super.setValueAt(value, row, col);
                }
            }

            @Override
            public String getColumnName(int col) {
                if (col == EDITCOL) {
                    return ""; // no name on Edit column
                }
                if (col == DUPLICATECOL) {
                    return ""; // no name on Duplicate column
                }
                return super.getColumnName(col);
            }

            @Override
            public Class<?> getColumnClass(int col) {
                if (col == VALUECOL) {
                    return String.class; // not a button
                }
                if (col == EDITCOL) {
                    return JButton.class;
                }
                if (col == DUPLICATECOL) {
                    return JButton.class;
                } else {
                    return super.getColumnClass(col);
                }
            }

            @Override
            public boolean isCellEditable(int row, int col) {
                if (col == VALUECOL) {
                    return false;
                }
                if (col == EDITCOL) {
                    return true;
                }
                if (col == DUPLICATECOL) {
                    return true;
                } else {
                    return super.isCellEditable(row, col);
                }
            }

            @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "DB_DUPLICATE_SWITCH_CLAUSES",
                                justification="better to keep cases in column order rather than to combine")
            @Override
            public int getPreferredWidth(int col) {
                // override default value for SystemName and UserName columns
                if (col == SYSNAMECOL) {
                    return new JTextField(9).getPreferredSize().width;
                }
                if (col == USERNAMECOL) {
                    return new JTextField(17).getPreferredSize().width;
                }
                if (col == VALUECOL) {
                    return new JTextField(6).getPreferredSize().width;
                }
                // new columns
                if (col == EDITCOL) {
                    return new JTextField(6).getPreferredSize().width;
                }
                if (col == DUPLICATECOL) {
                    return new JTextField(10).getPreferredSize().width;
                } else {
                    return super.getPreferredWidth(col);
                }
            }

            @Override
            public void configValueColumn(JTable table) {
                // value column isn't a button, so config is null
            }

            @Override
            protected boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
                return true;
                // return (e.getPropertyName().indexOf("alue")=0);
            }

            @Override
            public JButton configureButton() {
                log.error("configureButton should not have been called");
                return null;
            }

            @Override
            protected String getBeanType() {
                return "Transit";
            }

            @Override
            public void configureTable(JTable table) {
                jmri.InstanceManager.sensorManagerInstance().addPropertyChangeListener(this);
                super.configureTable(table);
            }

            @Override
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                if (e.getSource() instanceof jmri.SensorManager) {
                    if (e.getPropertyName().equals("DisplayListName") || e.getPropertyName().equals("length")) {
                        updateSensorList();
                    }
                }
                super.propertyChange(e);
            }

        };
    }

    @Override
    protected void setTitle() {
        f.setTitle(Bundle.getMessage("TitleTransitTable"));
    }

    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.TransitTable";
    }

    // instance variables
    private boolean editMode = false;
    private boolean duplicateMode = false;
    private TransitManager transitManager = null;
    private final SectionManager sectionManager = InstanceManager.getNullableDefault(jmri.SectionManager.class);
    private Transit curTransit = null;
    private SectionTableModel sectionTableModel = null;
    private final List<Section> sectionList = new ArrayList<>();
    private final int[] direction = new int[150];
    private final int[] sequence = new int[150];
    @SuppressWarnings("unchecked")
    private final List<TransitSectionAction>[] action = new ArrayList[150];
    private final boolean[] alternate = new boolean[150];
    private final boolean[] safe = new boolean[150];
    private String sensorList[];
    private final String[] sensorStopAllocation = new String[150];
    private final int maxSections = 150;  // must be equal to the dimension of the above arrays
    private final List<Section> primarySectionBoxList = new ArrayList<>();
    private final int[] priSectionDirection = new int[150];
    private final List<Section> alternateSectionBoxList = new ArrayList<>();
    private final int[] altSectionDirection = new int[150];
    private final List<Section> insertAtBeginningBoxList = new ArrayList<>();
    private final int[] insertAtBeginningDirection = new int[150];
    private Section curSection = null;
    private int curSectionDirection = 0;
    private Section prevSection = null;
    private int prevSectionDirection = 0;
    private int curSequenceNum = 0;

    // add/create variables
    JmriJFrame addFrame = null;
    JTextField sysName = new JTextField(5);
    JLabel sysNameFixed = new JLabel("");
    JTextField userName = new JTextField(17);
    JLabel sysNameLabel = new JLabel(Bundle.getMessage("LabelSystemName"));
    JLabel userNameLabel = new JLabel(Bundle.getMessage("LabelUserName"));
    JButton create = null;
    JButton update = null;
    JButton deleteSections = null;
    JComboBox<String> primarySectionBox = new JComboBox<>();
    JButton addNextSection = null;
    JCheckBox addAsSafe = null;
    JComboBox<String> stopAllocatingSensorBox = new JComboBox<>();
    JButton removeLastSection = null;
    JButton removeFirstSection = null;
    JButton insertAtBeginning = null;
    JComboBox<String> insertAtBeginningBox = new JComboBox<>();
    JLabel seqNumLabel = new JLabel(rbx.getString("LabelSeqNum"));
    JSpinner seqNum = new JSpinner(new SpinnerNumberModel(1, 1, 1, 1));
    JButton replacePrimaryForSequence = null;
    JButton deleteAlternateForSequence = null;
    JButton addAlternateForSequence = null;
    JComboBox<String> alternateSectionBox = new JComboBox<>();
    JButton addAlternateSection = null;
    JCheckBox _autoSystemName = new JCheckBox(Bundle.getMessage("LabelAutoSysName"));
    jmri.UserPreferencesManager pref;
    String systemNameAuto = this.getClass().getName() + ".AutoSystemName";


    /**
     * Responds to the Add... button and the Edit buttons in Transit Table.
     */
    @Override
    protected void addPressed(ActionEvent e) {
        editMode = false;
        duplicateMode = false;
        if ((sectionManager.getNamedBeanSet().size()) > 0) {
            addEditPressed();
        } else {
            JOptionPane.showMessageDialog(null, rbx
                    .getString("Message21"), Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    void editPressed(String sName) {
        curTransit = transitManager.getBySystemName(sName);
        if (curTransit == null) {
            // no transit - should never happen, but protects against a $%^#@ exception
            return;
        }
        sysNameFixed.setText(sName);
        editMode = true;
        duplicateMode = false;
        addEditPressed();
    }

    void duplicatePressed(String sName) {
        curTransit = transitManager.getBySystemName(sName);
        if (curTransit == null) {
            // no transit - should never happen, but protects against a $%^#@ exception
            return;
        }
        duplicateMode = true;
        editMode = false;
        addEditPressed();
    }

    void addEditPressed() {
        pref = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        if (addFrame == null) {
            addFrame = new JmriJFrame(Bundle.getMessage("TitleAddTransit"));
            addFrame.addHelpMenu("package.jmri.jmrit.beantable.TransitAddEdit", true);
            addFrame.getContentPane().setLayout(new BoxLayout(addFrame.getContentPane(), BoxLayout.Y_AXIS));
            JPanel p;
            // system name
            p = new JPanel();
            p.setLayout(new FlowLayout());
            p.add(sysNameLabel);
            sysNameLabel.setLabelFor(sysName);
            p.add(sysNameFixed);
            p.add(sysName);
            p.add(_autoSystemName);
            _autoSystemName.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    autoSystemName();
                }
            });
            if (pref.getSimplePreferenceState(systemNameAuto)) {
                _autoSystemName.setSelected(true);
            }
            sysName.setToolTipText(rbx.getString("TransitSystemNameHint"));
            addFrame.getContentPane().add(p);
            // user name
            p = new JPanel();
            p.add(userNameLabel);
            userNameLabel.setLabelFor(userName);
            p.add(userName);
            userName.setToolTipText(rbx.getString("TransitUserNameHint"));
            addFrame.getContentPane().add(p);
            addFrame.getContentPane().add(new JSeparator());
            // instruction text fields
            JPanel p1 = new JPanel();
            p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
            JPanel p11 = new JPanel();
            p11.setLayout(new FlowLayout());
            p11.add(new JLabel(rbx.getString("SectionTableMessage")));
            p1.add(p11);
            JPanel p12 = new JPanel();
            p12.setLayout(new BorderLayout());
            // initialize table of sections
            sectionTableModel = new SectionTableModel();
            JTable sectionTable = new JTable(sectionTableModel);
            sectionTable.setDefaultRenderer(JComboBox.class, new jmri.jmrit.symbolicprog.ValueRenderer());
            sectionTable.setDefaultEditor(JComboBox.class, new jmri.jmrit.symbolicprog.ValueEditor());
            sectionTable.setDefaultRenderer(Boolean.class, new EnablingCheckboxRenderer());
            sectionTable.setRowSelectionAllowed(false);
            TableColumnModel sectionColumnModel = sectionTable.getColumnModel();
            TableColumn sequenceColumn = sectionColumnModel.getColumn(SectionTableModel.SEQUENCE_COLUMN);
            sequenceColumn.setResizable(true);
            sequenceColumn.setMinWidth(50);
            sequenceColumn.setMaxWidth(70);
            TableColumn sectionColumn = sectionColumnModel.getColumn(SectionTableModel.SECTIONNAME_COLUMN);
            sectionColumn.setResizable(true);
            sectionColumn.setMinWidth(150);
            //sectionColumn.setMaxWidth(210);
            TableColumn actionColumn = sectionColumnModel.getColumn(SectionTableModel.ACTION_COLUMN);
            // install button renderer and editor
            ButtonRenderer buttonRenderer = new ButtonRenderer();
            sectionTable.setDefaultRenderer(JButton.class, buttonRenderer);
            TableCellEditor buttonEditor = new ButtonEditor(new JButton());
            sectionTable.setDefaultEditor(JButton.class, buttonEditor);
            JButton testButton = new JButton(rbx.getString("AddEditActions"));
            sectionTable.setRowHeight(testButton.getPreferredSize().height);
            actionColumn.setResizable(false);
            actionColumn.setMinWidth(testButton.getPreferredSize().width);
            TableColumn directionColumn = sectionColumnModel.getColumn(SectionTableModel.SEC_DIRECTION_COLUMN);
            directionColumn.setResizable(true);
            directionColumn.setMinWidth((int)new JLabel(rbx.getString("DirectionColName").substring(1,7)).getPreferredSize().getWidth());
            directionColumn.setMaxWidth((int)new JLabel(rbx.getString("DirectionColName").concat("WW")).getPreferredSize().getWidth());
            TableColumn alternateColumn = sectionColumnModel.getColumn(SectionTableModel.ALTERNATE_COLUMN);
            alternateColumn.setResizable(true);
            alternateColumn.setMinWidth((int)new JLabel(rbx.getString("AlternateColName").substring(1,7)).getPreferredSize().getWidth());
            alternateColumn.setMaxWidth((int)new JLabel(rbx.getString("AlternateColName").concat("WW")).getPreferredSize().getWidth());
            JScrollPane sectionTableScrollPane = new JScrollPane(sectionTable);
            p12.add(sectionTableScrollPane, BorderLayout.CENTER);
            p1.add(p12);
            JPanel p13 = new JPanel();
            p13.add(primarySectionBox);
            primarySectionBox.setToolTipText(rbx.getString("PrimarySectionBoxHint"));
            p13.add(addNextSection = new JButton(rbx.getString("AddPrimaryButton")));
            p13.add(addAsSafe = new JCheckBox(Bundle.getMessage("TransitSectionIsSafe")));
            addAsSafe.setToolTipText(Bundle.getMessage("TransitSectionIsSafeHint"));
            JPanel p13A = new JPanel();
            p13A.add(new JLabel(Bundle.getMessage("PauseAllocationOnSensorActive")));
            p13A.add(stopAllocatingSensorBox = new JComboBox<>(sensorList));
            p13.add(p13A);
            stopAllocatingSensorBox.setToolTipText(Bundle.getMessage("PauseAllocationOnSensorActiveHint"));
            addNextSection.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    addNextSectionPressed(e);
                }
            });
            addNextSection.setToolTipText(rbx.getString("AddPrimaryButtonHint"));
            p13.setLayout(new FlowLayout());
            p1.add(p13);
            JPanel p14 = new JPanel();
            p14.setLayout(new FlowLayout());
            p14.add(alternateSectionBox);
            alternateSectionBox.setToolTipText(rbx.getString("AlternateSectionBoxHint"));
            p14.add(addAlternateSection = new JButton(rbx.getString("AddAlternateButton")));
            addAlternateSection.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    addAlternateSectionPressed(e);
                }
            });
            addAlternateSection.setToolTipText(rbx.getString("AddAlternateButtonHint"));
            p14.add(new JLabel("        ")); // spacer between 2 groups of label + combobox
            p14.add(insertAtBeginningBox);
            insertAtBeginningBox.setToolTipText(rbx.getString("InsertAtBeginningBoxHint"));
            p14.add(insertAtBeginning = new JButton(rbx.getString("InsertAtBeginningButton")));
            insertAtBeginning.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    insertAtBeginningPressed(e);
                }
            });
            insertAtBeginning.setToolTipText(rbx.getString("InsertAtBeginningButtonHint"));
            p1.add(p14);
            p1.add(new JSeparator());
            JPanel p15 = new JPanel();
            p15.setLayout(new FlowLayout());
            p15.add(deleteSections = new JButton(rbx.getString("DeleteSectionsButton")));
            deleteSections.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    deleteAllSections(e);
                }
            });
            deleteSections.setToolTipText(rbx.getString("DeleteSectionsButtonHint"));
            p15.add(new JLabel("  "));
            p15.add(removeLastSection = new JButton(rbx.getString("RemoveLastButton")));
            removeLastSection.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    removeLastSectionPressed(e);
                }
            });
            removeLastSection.setToolTipText(rbx.getString("RemoveLastButtonHint"));
            p15.add(new JLabel("  "));
            p15.add(removeFirstSection = new JButton(rbx.getString("RemoveFirstButton")));
            removeFirstSection.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    removeFirstSectionPressed(e);
                }
            });
            removeFirstSection.setToolTipText(rbx.getString("RemoveFirstButtonHint"));
            p1.add(p15);
            JPanel p16 = new JPanel();
            p16.setLayout(new FlowLayout());
            p16.add(seqNumLabel);
            p16.add(seqNum);
            seqNum.setToolTipText(rbx.getString("SeqNumHint"));
            p1.add(p16);
            JPanel p17 = new JPanel();
            p17.setLayout(new FlowLayout());
            p17.add(replacePrimaryForSequence = new JButton(rbx.getString("ReplacePrimaryForSeqButton")));
            replacePrimaryForSequence.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    replacePrimaryForSeqPressed(e);
                }
            });
            replacePrimaryForSequence.setToolTipText(rbx.getString("ReplacePrimaryForSeqButtonHint"));
            p17.add(new JLabel("  "));
            p17.add(deleteAlternateForSequence = new JButton(rbx.getString("DeleteAlternateForSeqButton")));
            deleteAlternateForSequence.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    deleteAlternateForSeqPressed(e);
                }
            });
            deleteAlternateForSequence.setToolTipText(rbx.getString("DeleteAlternateForSeqButtonHint"));
            p17.add(new JLabel("  "));
            p17.add(addAlternateForSequence = new JButton(rbx.getString("AddAlternateForSeqButton")));
            addAlternateForSequence.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    addAlternateForSeqPressed(e);
                }
            });
            addAlternateForSequence.setToolTipText(rbx.getString("AddAlternateForSeqButtonHint"));
            p1.add(p17);
            addFrame.getContentPane().add(p1);
            // set up bottom buttons
            addFrame.getContentPane().add(new JSeparator());
            JButton cancel = null;
            JPanel pb = new JPanel();
            pb.setLayout(new FlowLayout());
            pb.add(cancel = new JButton(Bundle.getMessage("ButtonCancel")));
            cancel.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    cancelPressed(e);
                }
            });
            cancel.setToolTipText(rbx.getString("CancelButtonHint"));
            pb.add(create = new JButton(Bundle.getMessage("ButtonCreate")));
            create.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    createPressed(e);
                }
            });
            create.setToolTipText(rbx.getString("SectionCreateButtonHint"));
            pb.add(update = new JButton(Bundle.getMessage("ButtonUpdate")));
            update.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    updatePressed(e);
                }
            });
            update.setToolTipText(rbx.getString("SectionUpdateButtonHint"));
            addFrame.getContentPane().add(pb);
        }
        if (editMode) {
            // setup for edit window
            addFrame.setTitle(Bundle.getMessage("TitleEditTransit"));
            _autoSystemName.setVisible(false);
            sysNameLabel.setEnabled(true);
            create.setVisible(false);
            update.setVisible(true);
            sysName.setVisible(false);
            sysNameFixed.setVisible(true);
            initializeEditInformation();
        } else {
            // setup for create window
            addFrame.setTitle(Bundle.getMessage("TitleAddTransit"));
            _autoSystemName.setVisible(true);
            _autoSystemName.setEnabled(true);
            autoSystemName();
            create.setVisible(true);
            create.setEnabled(true);
            update.setVisible(false);
            sysName.setVisible(true);
            sysNameFixed.setVisible(false);
            if (duplicateMode) {
                // setup with information from previous Transit
                initializeEditInformation();
                sysName.setText(curTransit.getSystemName());
                curTransit = null;
            } else {
                deleteAllSections(null);
            }
        }
        initializeSectionCombos();
        updateSeqNum();
        addFrame.pack();
        addFrame.setVisible(true);
    }

    private void initializeEditInformation() {
        sectionList.clear();
        curSection = null;
        curSectionDirection = 0;
        curSequenceNum = 0;
        prevSection = null;
        prevSectionDirection = 0;
        if (curTransit != null) {
            userName.setText(curTransit.getUserName());
            List<TransitSection> tsList = curTransit.getTransitSectionList();
            for (int i = 0; i < tsList.size(); i++) {
                TransitSection ts = tsList.get(i);
                if (ts != null) {
                    sectionList.add(ts.getSection());
                    sequence[i] = ts.getSequenceNumber();
                    direction[i] = ts.getDirection();
                    action[i] = ts.getTransitSectionActionList();
                    alternate[i] = ts.isAlternate();
                    safe[i] = ts.isSafe();
                    sensorStopAllocation[i] = ts.getStopAllocatingSensor();
                }
            }
            int index = sectionList.size() - 1;
            while (alternate[index] && (index > 0)) {
                index--;
            }
            if (index >= 0) {
                curSection = sectionList.get(index);
                curSequenceNum = sequence[index];
                if (index > 0) {
                    curSectionDirection = direction[index];
                }
                index--;
                while ((index >= 0) && alternate[index]) {
                    index--;
                }
                if (index >= 0) {
                    prevSection = sectionList.get(index);
                    prevSectionDirection = direction[index];
                }
            }
        }
        sectionTableModel.fireTableDataChanged();
    }

    private void deleteAllSections(ActionEvent e) {
        sectionList.clear();
        for (int i = 0; i < maxSections; i++) {
            direction[i] = Section.FORWARD;
            sequence[i] = 0;
            action[i] = null;
            alternate[i] = false;
            safe[i] = false;
            sensorStopAllocation[i] = "";
        }
        curSection = null;
        curSectionDirection = 0;
        prevSection = null;
        prevSectionDirection = 0;
        curSequenceNum = 0;
        initializeSectionCombos();
        updateSeqNum();
        sectionTableModel.fireTableDataChanged();
    }

    void addNextSectionPressed(ActionEvent e) {
        if (sectionList.size() > maxSections) {
            JOptionPane.showMessageDialog(addFrame, rbx
                    .getString("Message23"), Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (primarySectionBoxList.size() == 0) {
            JOptionPane.showMessageDialog(addFrame, rbx
                    .getString("Message25"), Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        int index = primarySectionBox.getSelectedIndex();
        Section s = primarySectionBoxList.get(index);
        if (s != null) {
            int j = sectionList.size();
            sectionList.add(s);
            direction[j] = priSectionDirection[index];
            curSequenceNum++;
            sequence[j] = curSequenceNum;
            safe[j] = addAsSafe.isSelected();
            if (stopAllocatingSensorBox.getSelectedIndex() >= 0) {
                sensorStopAllocation[j] = (String)stopAllocatingSensorBox.getSelectedItem();
            } else {
                sensorStopAllocation[j] = "";
            }
            action[j] = new ArrayList<>();
            alternate[j] = false;
            if ((sectionList.size() == 2) && (curSection != null)) {
                if (forwardConnected(curSection, s, 0)) {
                    direction[0] = Section.REVERSE;
                }
                curSectionDirection = direction[0];
            }
            prevSection = curSection;
            prevSectionDirection = curSectionDirection;
            curSection = s;
            if (prevSection != null) {
                curSectionDirection = direction[j];
            }
            initializeSectionCombos();
        }
        updateSeqNum();
        sectionTableModel.fireTableDataChanged();
    }

    void removeLastSectionPressed(ActionEvent e) {
        if (sectionList.size() <= 1) {
            deleteAllSections(e);
        } else {
            int j = sectionList.size() - 1;
            if (!alternate[j]) {
                curSequenceNum--;
                curSection = sectionList.get(j - 1);
                curSectionDirection = direction[j - 1];
                // delete alternate if present
                int k = j - 2;
                while ((k >= 0) && alternate[k]) {
                    k--;
                }
                // After this delete we need the new previous section, if there is one.
                if (k < 0) {
                    // There is no previous section
                    prevSection = null;
                } else {
                    prevSection = sectionList.get(k);
                    prevSectionDirection = direction[k];
                }
            }
            sectionList.remove(j);
            initializeSectionCombos();
        }
        updateSeqNum();
        sectionTableModel.fireTableDataChanged();
    }

    void insertAtBeginningPressed(ActionEvent e) {
        if (sectionList.size() > maxSections) {
            JOptionPane.showMessageDialog(addFrame, rbx
                    .getString("Message23"), Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (insertAtBeginningBoxList.size() == 0) {
            JOptionPane.showMessageDialog(addFrame, rbx
                    .getString("Message35"), Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        int index = insertAtBeginningBox.getSelectedIndex();
        Section s = insertAtBeginningBoxList.get(index);
        if (s != null) {
            sectionList.add(0, s);
            for (int i = sectionList.size() - 2; i > 0; i--) {
                direction[i + 1] = direction[i];
                alternate[i + 1] = alternate[i];
                action[i + 1] = action[i];
                sequence[i + 1] = sequence[i] + 1;
                safe[i + 1] = safe[i];
                sensorStopAllocation[i + 1] = sensorStopAllocation[i];
            }
            direction[0] = insertAtBeginningDirection[index];
            curSequenceNum++;
            sequence[0] = 1;
            alternate[0] = false;
            safe[0] = addAsSafe.isSelected();
            sensorStopAllocation[0] = "";
            action[0] = new ArrayList<>();
            if (curSequenceNum == 2) {
                prevSectionDirection = direction[0];
                prevSection = s;
            }
            initializeSectionCombos();
        }
        updateSeqNum();
        sectionTableModel.fireTableDataChanged();
    }

    void removeFirstSectionPressed(ActionEvent e) {
        if (curSequenceNum <= 1) {
            deleteAllSections(e);
        } else {
            int keep = 1;
            while (alternate[keep]) {
                keep++;
            }
            for (int i = keep, j = 0; i < sectionList.size(); i++, j++) {
                sequence[j] = sequence[i] - 1;
                direction[j] = direction[i];
                action[j] = action[i];
                alternate[j] = alternate[i];
                safe[j] = safe[i];
                sensorStopAllocation[j] = sensorStopAllocation[i];
            }
            for (int k = 0; k < keep; k++) {
                sectionList.remove(0);
            }
            curSequenceNum--;
            initializeSectionCombos();
        }
        updateSeqNum();
        sectionTableModel.fireTableDataChanged();
    }

    void replacePrimaryForSeqPressed(ActionEvent e) {
        int seq = getSeqNum();
        if (seq == 0) {
            return;
        }
        Section sOld = null;
        List<Section> altOldList = new ArrayList<>();
        Section beforeSection = null;
        int beforeSectionDirection = 0;
        Section afterSection = null;
        int afterSectionDirection = 0;
        int index = -1;
        for (int i = 0; i < sectionList.size(); i++) {
            if ((sequence[i] == seq) && (!alternate[i])) {
                sOld = sectionList.get(i);
                index = i;
            }
            if ((sequence[i] == seq) && alternate[i]) {
                altOldList.add(sectionList.get(i));
            }
            if ((sequence[i] == (seq - 1)) && (!alternate[i])) {
                beforeSection = sectionList.get(i);
                beforeSectionDirection = direction[i];
            }
            if ((sequence[i] == (seq + 1)) && (!alternate[i])) {
                afterSection = sectionList.get(i);
                afterSectionDirection = Section.FORWARD;
                if (afterSectionDirection == direction[i]) {
                    afterSectionDirection = Section.REVERSE;
                }
            }
        }
        if (sOld == null) {
            log.error("Missing primary Section for seq = {}", seq);
            return;
        }
        List<Section> possibles = new ArrayList<>();
        int[] possiblesDirection = new int[150];
        List<String> possibleNames = new ArrayList<>();
        
        for (Section s : sectionManager.getNamedBeanSet()) {
            Section mayBeSection = null;
            String mayBeName = s.getSystemName();
            int mayBeDirection = 0;
            if ((s != sOld) && (s != beforeSection)
                    && (s != afterSection) && (!inSectionList(s, altOldList))) {
                if (beforeSection != null) {
                    if (forwardConnected(s, beforeSection, beforeSectionDirection)) {
                        String uname = s.getUserName();
                        if ((uname != null) && (!uname.equals(""))) {
                            mayBeName = mayBeName + "( " + uname + " )";
                        }
                        mayBeSection = s;
                        mayBeDirection = Section.FORWARD;
                    } else if (reverseConnected(s, beforeSection, beforeSectionDirection)) {
                        String uname = s.getUserName();
                        if ((uname != null) && (!uname.equals(""))) {
                            mayBeName = mayBeName + "( " + uname + " )";
                        }
                        mayBeSection = s;
                        mayBeDirection = Section.REVERSE;
                    }
                    if ((mayBeSection != null) && (afterSection != null)) {
                        if (mayBeDirection == Section.REVERSE) {
                            if (!forwardConnected(s, afterSection, afterSectionDirection)) {
                                mayBeSection = null;
                            }
                        } else {
                            if (!reverseConnected(s, afterSection, afterSectionDirection)) {
                                mayBeSection = null;
                            }
                        }
                    }
                } else if (afterSection != null) {
                    if (forwardConnected(s, afterSection, afterSectionDirection)) {
                        String uname = s.getUserName();
                        if ((uname != null) && (!uname.equals(""))) {
                            mayBeName = mayBeName + "( " + uname + " )";
                        }
                        mayBeSection = s;
                        mayBeDirection = Section.REVERSE;
                    } else if (reverseConnected(s, afterSection, afterSectionDirection)) {
                        String uname = s.getUserName();
                        if ((uname != null) && (!uname.equals(""))) {
                            mayBeName = mayBeName + "( " + uname + " )";
                        }
                        mayBeSection = s;
                        mayBeDirection = Section.FORWARD;
                    }
                } else {
                    mayBeSection = s;
                    mayBeDirection = Section.FORWARD;
                }
                if (mayBeSection != null) {
                    possibles.add(mayBeSection);
                    possiblesDirection[possibles.size() - 1] = mayBeDirection;
                    possibleNames.add(mayBeName);
                }
            }
        }
        if (possibles.size() == 0) {
            JOptionPane.showMessageDialog(addFrame,
                    java.text.MessageFormat.format(rbx.getString("Message36"),
                            new Object[]{"" + seq}), Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        int k = 0;
        if (possibles.size() > 1) {
            Object choices[] = new Object[possibles.size()];
            for (int j = 0; j < possibles.size(); j++) {
                choices[j] = possibleNames.get(j);
            }
            Object selName = JOptionPane.showInputDialog(addFrame,
                    rbx.getString("ReplacePrimaryChoice"),
                    rbx.getString("ReplacePrimaryTitle"),
                    JOptionPane.QUESTION_MESSAGE, null, choices, choices[0]);
            if (selName == null) {
                return;
            }
            for (int j = 0; j < possibles.size(); j++) {
                if (selName.equals(choices[j])) {
                    k = j;
                }
            }
        }
        sectionList.remove(index);
        sectionList.add(index, possibles.get(k));
        direction[index] = possiblesDirection[k];
        if (index == (sectionList.size() - 1)) {
            curSection = sectionList.get(index);
            curSectionDirection = direction[index];
        } else if (index == (sectionList.size() - 2)) {
            prevSection = sectionList.get(index);
            prevSectionDirection = direction[index];
        }
        initializeSectionCombos();
        sectionTableModel.fireTableDataChanged();
    }

    boolean inSectionList(Section s, List<Section> sList) {
        for (int i = 0; i < sList.size(); i++) {
            if (sList.get(i) == s) {
                return true;
            }
        }
        return false;
    }

    int getSeqNum() {
        int n = (Integer) seqNum.getValue(); // JSpinner int from 1 - sectionList.size()
        if (n > curSequenceNum) {
            JOptionPane.showMessageDialog(null, rbx
                    .getString("Message34"), Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return 0;
        }
        return n;
    }

    /**
     * Update Order Num spinner on pane. Limit spinner to highest order index in
     * section table (column 0).
     */
    void updateSeqNum() {
        int seqMax = 0;
        for (int i = 0; i < sectionList.size(); i++) {
            if (sequence[i] > seqMax) {
                seqMax = sequence[i];
            }
        }
        seqNum.setModel(new SpinnerNumberModel(
                seqMax, // initial value set
                Math.min(seqMax, 1), // minimum value, either 0 (empty list) or 1
                seqMax, // maximum order number
                1));
        seqNum.setValue(Math.min(seqMax, 1));
        return;
    }

    void deleteAlternateForSeqPressed(ActionEvent e) {
        if (sectionList.size() <= 1) {
            deleteAllSections(e);
        } else {
            int seq = getSeqNum();
            if (seq == 0) {
                return;
            }
            for (int i = sectionList.size(); i >= seq; i--) {
                if ((sequence[i] == seq) && alternate[i]) {
                    for (int j = i; j < sectionList.size() - 1; j++) {
                        sequence[j] = sequence[j + 1];
                        direction[j] = direction[j + 1];
                        action[j] = action[j + 1];
                        alternate[j] = alternate[j + 1];
                        safe[j] = safe[j + 1];
                        sensorStopAllocation[j] = sensorStopAllocation[j + 1];
                    }
                    sectionList.remove(i);
                }
            }
            initializeSectionCombos();
        }
        updateSeqNum();
        sectionTableModel.fireTableDataChanged();
    }

    void addAlternateForSeqPressed(ActionEvent e) {
        if (sectionList.size() > maxSections) {
            JOptionPane.showMessageDialog(addFrame, rbx
                    .getString("Message23"), Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        int seq = getSeqNum();
        if (seq == 0) {
            return;
        }
        Section primarySection = null;
        List<Section> altOldList = new ArrayList<>();
        Section beforeSection = null;
        int beforeSectionDirection = 0;
        Section afterSection = null;
        int afterSectionDirection = 0;
        int index = -1;
        for (int i = 0; i < sectionList.size(); i++) {
            if ((sequence[i] == seq) && (!alternate[i])) {
                primarySection = sectionList.get(i);
                index = i;
            }
            if ((sequence[i] == seq) && alternate[i]) {
                altOldList.add(sectionList.get(i));
            }
            if ((sequence[i] == (seq - 1)) && (!alternate[i])) {
                beforeSection = sectionList.get(i);
                beforeSectionDirection = direction[i];
            }
            if ((sequence[i] == (seq + 1)) && (!alternate[i])) {
                afterSection = sectionList.get(i);
                afterSectionDirection = Section.FORWARD;
                if (afterSectionDirection == direction[i]) {
                    afterSectionDirection = Section.REVERSE;
                }
            }
        }
        if (primarySection == null) {
            log.error("Missing primary Section for seq = {}", seq);
            return;
        }
        List<Section> possibles = new ArrayList<>();
        int[] possiblesDirection = new int[150];
        List<String> possibleNames = new ArrayList<>();
        for (Section s : sectionManager.getNamedBeanSet()) {
            Section mayBeSection = null;
            String mayBeName = s.getSystemName();
            int mayBeDirection = 0;
            if ((s != primarySection) && (s != beforeSection)
                    && (s != afterSection) && (!inSectionList(s, altOldList))) {
                if (beforeSection != null) {
                    if (forwardConnected(s, beforeSection, beforeSectionDirection)) {
                        String uname = s.getUserName();
                        if ((uname != null) && (!uname.equals(""))) {
                            mayBeName = mayBeName + "( " + uname + " )";
                        }
                        mayBeSection = s;
                        mayBeDirection = Section.FORWARD;
                    } else if (reverseConnected(s, beforeSection, beforeSectionDirection)) {
                        String uname = s.getUserName();
                        if ((uname != null) && (!uname.equals(""))) {
                            mayBeName = mayBeName + "( " + uname + " )";
                        }
                        mayBeSection = s;
                        mayBeDirection = Section.REVERSE;
                    }
                    if ((mayBeSection != null) && (afterSection != null)) {
                        if (mayBeDirection == Section.REVERSE) {
                            if (!forwardConnected(s, afterSection, afterSectionDirection)) {
                                mayBeSection = null;
                            }
                        } else {
                            if (!reverseConnected(s, afterSection, afterSectionDirection)) {
                                mayBeSection = null;
                            }
                        }
                    }
                } else if (afterSection != null) {
                    if (forwardConnected(s, afterSection, afterSectionDirection)) {
                        String uname = s.getUserName();
                        if ((uname != null) && (!uname.equals(""))) {
                            mayBeName = mayBeName + "( " + uname + " )";
                        }
                        mayBeSection = s;
                        mayBeDirection = Section.REVERSE;
                    } else if (reverseConnected(s, afterSection, afterSectionDirection)) {
                        String uname = s.getUserName();
                        if ((uname != null) && (!uname.equals(""))) {
                            mayBeName = mayBeName + "( " + uname + " )";
                        }
                        mayBeSection = s;
                        mayBeDirection = Section.FORWARD;
                    }
                } else {
                    mayBeSection = s;
                    mayBeDirection = Section.FORWARD;
                }
                if (mayBeSection != null) {
                    possibles.add(mayBeSection);
                    possiblesDirection[possibles.size() - 1] = mayBeDirection;
                    possibleNames.add(mayBeName);
                }
            }
        }
        if (possibles.size() == 0) {
            JOptionPane.showMessageDialog(addFrame,
                    java.text.MessageFormat.format(rbx.getString("Message37"),
                            new Object[]{"" + seq}), Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        int k = 0;
        if (possibles.size() > 1) {
            Object choices[] = new Object[possibles.size()];
            for (int j = 0; j < possibles.size(); j++) {
                choices[j] = possibleNames.get(j);
            }
            Object selName = JOptionPane.showInputDialog(addFrame,
                    rbx.getString("AddAlternateChoice"),
                    rbx.getString("AddAlternateTitle"),
                    JOptionPane.QUESTION_MESSAGE, null, choices, choices[0]);
            if (selName == null) {
                return;
            }
            for (int j = 0; j < possibles.size(); j++) {
                if (selName.equals(choices[j])) {
                    k = j;
                }
            }
        }
        index = index + 1 + altOldList.size();
        sectionList.add(index, possibles.get(k));
        for (int i = sectionList.size() - 2; i >= index; i--) {
            direction[i + 1] = direction[i];
            alternate[i + 1] = alternate[i];
            action[i + 1] = action[i];
            sequence[i + 1] = sequence[i];
            safe[i + 1] = safe[i];
            sensorStopAllocation[i + 1] = sensorStopAllocation[i];
        }
        direction[index] = possiblesDirection[k];
        sequence[index] = sequence[index - 1];
        alternate[index] = true;
        safe[index] = addAsSafe.isSelected();
        if (stopAllocatingSensorBox.getSelectedIndex() < 0) {
            sensorStopAllocation[index] = "";
        } else {
            sensorStopAllocation[index] = (String) stopAllocatingSensorBox.getSelectedItem();
        }
        action[index] = new ArrayList<>();
        initializeSectionCombos();
        updateSeqNum();
        sectionTableModel.fireTableDataChanged();
    }

    void addAlternateSectionPressed(ActionEvent e) {
        if (sectionList.size() > maxSections) {
            JOptionPane.showMessageDialog(addFrame, rbx
                    .getString("Message23"), Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (alternateSectionBoxList.size() == 0) {
            JOptionPane.showMessageDialog(addFrame, rbx
                    .getString("Message24"), Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        int index = alternateSectionBox.getSelectedIndex();
        Section s = alternateSectionBoxList.get(index);
        if (s != null) {
            int j = sectionList.size();
            sectionList.add(s);
            direction[j] = altSectionDirection[index];
            sequence[j] = curSequenceNum;
            action[j] = new ArrayList<>();
            alternate[j] = true;
            safe[j] = addAsSafe.isSelected();
            initializeSectionCombos();
        }
        updateSeqNum();
        sectionTableModel.fireTableDataChanged();
    }

    void createPressed(ActionEvent e) {
        if (!checkTransitInformation()) {
            return;
        }
        String uName = userName.getText();
        if (uName.equals("")) {
            uName = null;
        }

        // attempt to create the new Transit
        if (_autoSystemName.isSelected()) {
            curTransit = transitManager.createNewTransit(uName);
        } else {
            String sName = sysName.getText();
            curTransit = transitManager.createNewTransit(sName, uName);
        }
        if (curTransit == null) {
            JOptionPane.showMessageDialog(addFrame, rbx
                    .getString("Message22"), Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        sysName.setText(curTransit.getSystemName());
        setTransitInformation();
        addFrame.setVisible(false);
        pref.setSimplePreferenceState(systemNameAuto, _autoSystemName.isSelected());
    }

    void cancelPressed(ActionEvent e) {
        addFrame.setVisible(false);
        addFrame.dispose();  // remove addFrame from Windows menu
        addFrame = null;
    }

    void updatePressed(ActionEvent e) {
        if (!checkTransitInformation()) {
            return;
        }
        // check if user name has been changed
        String uName = userName.getText();
        if (uName.equals("")) {
            uName = null;
        }
        if ((uName != null) && (!uName.equals(curTransit.getUserName()))) {
            // check that new user name is unique
            Transit tTransit = transitManager.getByUserName(uName);
            if (tTransit != null) {
                JOptionPane.showMessageDialog(addFrame, rbx
                        .getString("Message22"), Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        curTransit.setUserName(uName);
        if (setTransitInformation()) {
            // successful update
            addFrame.setVisible(false);
            addFrame.dispose();  // remove addFrame from Windows menu
            addFrame = null;
        }
    }

    private boolean checkTransitInformation() {
        if ((sectionList.size() <= 1) || (curSequenceNum <= 1)) {
            JOptionPane.showMessageDialog(addFrame, rbx
                    .getString("Message26"), Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private boolean setTransitInformation() {
        if (curTransit == null) {
            return false;
        }
        curTransit.removeAllSections();
        for (int i = 0; i < sectionList.size(); i++) {
            TransitSection ts = new TransitSection(sectionList.get(i),
                    sequence[i], direction[i], alternate[i], safe[i], sensorStopAllocation[i]);
            List<TransitSectionAction> list = action[i];
            if (list != null) {
                for (int j = 0; j < list.size(); j++) {
                    ts.addAction(list.get(j));
                }
            }
            curTransit.addTransitSection(ts);
        }
        return true;
    }

    private void initializeSectionCombos() {
        primarySectionBox.removeAllItems();
        alternateSectionBox.removeAllItems();
        insertAtBeginningBox.removeAllItems();
        primarySectionBoxList.clear();
        alternateSectionBoxList.clear();
        insertAtBeginningBoxList.clear();
        if (sectionList.size() == 0) {
            // no Sections currently in Transit - all Sections and all Directions OK
            for (Section s : sectionManager.getNamedBeanSet()) {
                String sName = s.getSystemName();
                String uname = s.getUserName();
                if ((uname != null) && (!uname.equals(""))) {
                    sName = sName + "( " + uname + " )";
                }
                primarySectionBox.addItem(sName);
                primarySectionBoxList.add(s);
                priSectionDirection[primarySectionBoxList.size() - 1] = Section.FORWARD;
            }
        } else {
            // limit to Sections that connect to the current Section and are not the previous Section
            for (Section s : sectionManager.getNamedBeanSet()) {
                String sName = s.getSystemName();
                if ((s != prevSection) && (forwardConnected(s, curSection, curSectionDirection))) {
                    String uname = s.getUserName();
                    if ((uname != null) && (!uname.equals(""))) {
                        sName = sName + "( " + uname + " )";
                    }
                    primarySectionBox.addItem(sName);
                    primarySectionBoxList.add(s);
                    priSectionDirection[primarySectionBoxList.size() - 1] = Section.FORWARD;
                } else if ((s != prevSection) && (reverseConnected(s, curSection, curSectionDirection))) {
                    String uname = s.getUserName();
                    if ((uname != null) && (!uname.equals(""))) {
                        sName = sName + "( " + uname + " )";
                    }
                    primarySectionBox.addItem(sName);
                    primarySectionBoxList.add(s);
                    priSectionDirection[primarySectionBoxList.size() - 1] = Section.REVERSE;
                }
            }
            // check if there are any alternate Section choices
            if (prevSection != null) {
                for (Section s : sectionManager.getNamedBeanSet()) {
                    String sName = s.getSystemName();
                    if ((notIncludedWithSeq(s, curSequenceNum))
                            && forwardConnected(s, prevSection, prevSectionDirection)) {
                        String uname = s.getUserName();
                        if ((uname != null) && (!uname.equals(""))) {
                            sName = sName + "( " + uname + " )";
                        }
                        alternateSectionBox.addItem(sName);
                        alternateSectionBoxList.add(s);
                        altSectionDirection[alternateSectionBoxList.size() - 1] = Section.FORWARD;
                    } else if (notIncludedWithSeq(s, curSequenceNum)
                            && reverseConnected(s, prevSection, prevSectionDirection)) {
                        String uname = s.getUserName();
                        if ((uname != null) && (!uname.equals(""))) {
                            sName = sName + "( " + uname + " )";
                        }
                        alternateSectionBox.addItem(sName);
                        alternateSectionBoxList.add(s);
                        altSectionDirection[alternateSectionBoxList.size() - 1] = Section.REVERSE;
                    }
                }
            }
            // check if there are any Sections available to be inserted at beginning
            Section firstSection = sectionList.get(0);
            int testDirection = Section.FORWARD;
            if (direction[0] == Section.FORWARD) {
                testDirection = Section.REVERSE;
            }
            for (Section s : sectionManager.getNamedBeanSet()) {
                String sName = s.getSystemName();
                if ((s != firstSection) && (forwardConnected(s, firstSection, testDirection))) {
                    String uname = s.getUserName();
                    if ((uname != null) && (!uname.equals(""))) {
                        sName = sName + "( " + uname + " )";
                    }
                    insertAtBeginningBox.addItem(sName);
                    insertAtBeginningBoxList.add(s);
                    insertAtBeginningDirection[insertAtBeginningBoxList.size() - 1] = Section.REVERSE;
                } else if ((s != firstSection) && (reverseConnected(s, firstSection, testDirection))) {
                    String uname = s.getUserName();
                    if ((uname != null) && (!uname.equals(""))) {
                        sName = sName + "( " + uname + " )";
                    }
                    insertAtBeginningBox.addItem(sName);
                    insertAtBeginningBoxList.add(s);
                    insertAtBeginningDirection[insertAtBeginningBoxList.size() - 1] = Section.FORWARD;
                }
            }
        }
    }

    @SuppressWarnings("unused")
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification="was previously marked with @SuppressWarnings, reason unknown")
    private boolean connected(Section s1, Section s2) {
        if ((s1 != null) && (s2 != null)) {
            List<EntryPoint> s1Entries = s1.getEntryPointList();
            List<EntryPoint> s2Entries = s2.getEntryPointList();
            for (int i = 0; i < s1Entries.size(); i++) {
                Block b = s1Entries.get(i).getFromBlock();
                for (int j = 0; j < s2Entries.size(); j++) {
                    if (b == s2Entries.get(j).getBlock()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean forwardConnected(Section s1, Section s2, int restrictedDirection) {
        if ((s1 != null) && (s2 != null)) {
            List<EntryPoint> s1ForwardEntries = s1.getForwardEntryPointList();
            List<EntryPoint> s2Entries;
            if (restrictedDirection == Section.FORWARD) {
                s2Entries = s2.getReverseEntryPointList();
            } else if (restrictedDirection == Section.REVERSE) {
                s2Entries = s2.getForwardEntryPointList();
            } else {
                s2Entries = s2.getEntryPointList();
            }
            for (int i = 0; i < s1ForwardEntries.size(); i++) {
                Block b1 = s1ForwardEntries.get(i).getFromBlock();
                for (int j = 0; j < s2Entries.size(); j++) {
                    Block b2 = s2Entries.get(j).getFromBlock();
                    if ((b1 == s2Entries.get(j).getBlock())
                            && (b2 == s1ForwardEntries.get(i).getBlock())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean reverseConnected(Section s1, Section s2, int restrictedDirection) {
        if ((s1 != null) && (s2 != null)) {
            List<EntryPoint> s1ReverseEntries = s1.getReverseEntryPointList();
            List<EntryPoint> s2Entries;
            if (restrictedDirection == Section.FORWARD) {
                s2Entries = s2.getReverseEntryPointList();
            } else if (restrictedDirection == Section.REVERSE) {
                s2Entries = s2.getForwardEntryPointList();
            } else {
                s2Entries = s2.getEntryPointList();
            }
            for (int i = 0; i < s1ReverseEntries.size(); i++) {
                Block b1 = s1ReverseEntries.get(i).getFromBlock();
                for (int j = 0; j < s2Entries.size(); j++) {
                    Block b2 = s2Entries.get(j).getFromBlock();
                    if ((b1 == s2Entries.get(j).getBlock())
                            && (b2 == s1ReverseEntries.get(i).getBlock())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean notIncludedWithSeq(Section s, int seq) {
        for (int i = 0; i < sectionList.size(); i++) {
            if ((sectionList.get(i) == s) && (seq == sequence[i])) {
                return false;
            }
        }
        return true;
    }

    private void autoSystemName() {
        if (_autoSystemName.isSelected()) {
//            create.setEnabled(true);
            sysName.setEnabled(false);
            sysNameLabel.setEnabled(false);
        } else {
//            if (sysName.getText().length() > 0)
//                create.setEnabled(true);
//            else
//                create.setEnabled(false);
            sysName.setEnabled(true);
            sysNameLabel.setEnabled(true);
        }
    }

    // variables for View Actions window
    private int activeRow = 0;
    private SpecialActionTableModel actionTableModel = null;
    private JmriJFrame actionTableFrame = null;
    private final JLabel fixedSectionLabel = new JLabel("X");

    private void addEditActionsPressed(int r) {
        activeRow = r;
        if (actionTableModel != null) {
            actionTableModel.fireTableStructureChanged();
        }
        if (actionTableFrame == null) {
            actionTableFrame = new JmriJFrame(rbx.getString("TitleViewActions"));
            actionTableFrame.addHelpMenu(
                    "package.jmri.jmrit.beantable.ViewSpecialActions", true);
            actionTableFrame.setLocation(50, 60);
            Container contentPane = actionTableFrame.getContentPane();
            contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
            JPanel panel1 = new JPanel();
            panel1.setLayout(new FlowLayout());
            JLabel sectionNameLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameSection")));
            panel1.add(sectionNameLabel);
            panel1.add(fixedSectionLabel);
            contentPane.add(panel1);
            // add table of Actions
            JPanel pctSpace = new JPanel();
            pctSpace.setLayout(new FlowLayout());
            pctSpace.add(new JLabel("   "));
            contentPane.add(pctSpace);
            JPanel pct = new JPanel();
            // initialize table of actions
            actionTableModel = new SpecialActionTableModel();
            JTable actionTable = new JTable(actionTableModel);
            actionTable.setRowSelectionAllowed(false);
            actionTable.setPreferredScrollableViewportSize(
                    new java.awt.Dimension(750, 200));
            TableColumnModel actionColumnModel = actionTable
                    .getColumnModel();
            TableColumn whenColumn = actionColumnModel
                    .getColumn(SpecialActionTableModel.WHEN_COLUMN);
            whenColumn.setResizable(true);
            whenColumn.setMinWidth(270);
            whenColumn.setMaxWidth(300);
            TableColumn whatColumn = actionColumnModel
                    .getColumn(SpecialActionTableModel.WHAT_COLUMN);
            whatColumn.setResizable(true);
            whatColumn.setMinWidth(290);
            whatColumn.setMaxWidth(350);
            TableColumn editColumn = actionColumnModel
                    .getColumn(SpecialActionTableModel.EDIT_COLUMN);
            // install button renderer and editor
            ButtonRenderer buttonRenderer = new ButtonRenderer();
            actionTable.setDefaultRenderer(JButton.class, buttonRenderer);
            TableCellEditor buttonEditor = new ButtonEditor(new JButton());
            actionTable.setDefaultEditor(JButton.class, buttonEditor);
            JButton testButton = new JButton(Bundle.getMessage("ButtonDelete"));
            actionTable.setRowHeight(testButton.getPreferredSize().height);
            editColumn.setResizable(false);
            editColumn.setMinWidth(testButton.getPreferredSize().width);
            TableColumn removeColumn = actionColumnModel
                    .getColumn(SpecialActionTableModel.REMOVE_COLUMN);
            removeColumn.setMinWidth(testButton.getPreferredSize().width);
            removeColumn.setResizable(false);
            JScrollPane actionTableScrollPane = new JScrollPane(
                    actionTable);
            pct.add(actionTableScrollPane, BorderLayout.CENTER);
            contentPane.add(pct);
            pct.setVisible(true);
            // add View Action panel buttons
            JPanel but = new JPanel();
            but.setLayout(new BoxLayout(but, BoxLayout.Y_AXIS));
            JPanel panel4 = new JPanel();
            panel4.setLayout(new FlowLayout());
            JButton newActionButton = new JButton(rbx.getString("ButtonAddNewAction"));
            panel4.add(newActionButton);
            newActionButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    newActionPressed(e);
                }
            });
            newActionButton.setToolTipText(rbx.getString("NewActionButtonHint"));
            JButton doneButton = new JButton(Bundle.getMessage("ButtonDone"));
            panel4.add(doneButton);
            doneButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    doneWithActionsPressed(e);
                }
            });
            doneButton.setToolTipText(rbx.getString("DoneButtonHint"));
            but.add(panel4);
            contentPane.add(but);
        }
        fixedSectionLabel.setText(getSectionNameByRow(r) + "    "
                + rbx.getString("SequenceAbbrev") + ": " + sequence[r]);
        actionTableFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (actionTableFrame != null) {
                    actionTableFrame.setVisible(false);
                    actionTableFrame.dispose();
                    actionTableFrame = null;
                }
                if (addEditActionFrame != null) {
                    addEditActionFrame.setVisible(false);
                    addEditActionFrame.dispose();
                    addEditActionFrame = null;
                }
            }
        });
        actionTableFrame.pack();
        actionTableFrame.setVisible(true);
    }

    private void doneWithActionsPressed(ActionEvent e) {
        actionTableFrame.setVisible(false);
        actionTableFrame.dispose();
        actionTableFrame = null;
        if (addEditActionFrame != null) {
            addEditActionFrame.setVisible(false);
            addEditActionFrame.dispose();
            addEditActionFrame = null;
        }
    }

    private void newActionPressed(ActionEvent e) {
        editActionMode = false;
        curTSA = null;
        addEditActionWindow();
    }

    // variables for Add/Edit Action window
    private boolean editActionMode = false;
    private JmriJFrame addEditActionFrame = null;
    private TransitSectionAction curTSA = null;
    private final JComboBox<String> whenBox = new JComboBox<>();
    private final NamedBeanComboBox<Sensor> whenSensorComboBox = new NamedBeanComboBox<>(InstanceManager.getDefault(SensorManager.class), null, DisplayOptions.DISPLAYNAME);
    private final JSpinner whenDataSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 65500, 1)); // delay
    private final JComboBox<String> whatBox = new JComboBox<>();
    private final JSpinner whatPercentSpinner = new JSpinner(); // speed
    private final JSpinner whatMinuteSpinner1 = new JSpinner(new SpinnerNumberModel(1, 1, 65500, 1));     // time in ms
    private final JSpinner whatMinuteSpinner2 = new JSpinner(new SpinnerNumberModel(100, 100, 65500, 1)); // time in ms
    private final JSpinner locoFunctionSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 28, 1));       // function ID
    private final JTextField whatStringField = new JTextField(12);
    private JButton updateActionButton = null;
    private JButton createActionButton = null;
    private JButton cancelAddEditActionButton = null;
    private final JComboBox<String> blockBox = new JComboBox<>();
    private List<Block> blockList = new ArrayList<>();
    private final JRadioButton onButton = new JRadioButton(Bundle.getMessage("StateOn"));
    private final JRadioButton offButton = new JRadioButton(Bundle.getMessage("StateOff"));
    private final JLabel doneSensorLabel = new JLabel(rbx.getString("DoneSensorLabel"));
    private JPanel signalPanel;
    private final NamedBeanComboBox<Sensor> doneSensorComboBox = new NamedBeanComboBox<>(InstanceManager.getDefault(SensorManager.class), null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<SignalMast> signalMastComboBox = new NamedBeanComboBox<>(InstanceManager.getDefault(SignalMastManager.class), null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<SignalHead> signalHeadComboBox = new NamedBeanComboBox<>(InstanceManager.getDefault(SignalHeadManager.class), null, DisplayOptions.DISPLAYNAME);

    private void addEditActionWindow() {
        if (addEditActionFrame == null) {
            // set up add/edit action window
            addEditActionFrame = new JmriJFrame(rbx.getString("TitleAddAction"));
            addEditActionFrame.addHelpMenu(
                    "package.jmri.jmrit.beantable.TransitSectionAddEditAction", true);
            addEditActionFrame.setLocation(120, 80);
            Container contentPane = addEditActionFrame.getContentPane();
            contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
            // to set When to start the action
            JPanel panelx = new JPanel();
            panelx.setLayout(new BoxLayout(panelx, BoxLayout.Y_AXIS));
            JPanel panel1 = new JPanel();
            panel1.setLayout(new FlowLayout());
            panel1.add(new JLabel(rbx.getString("WhenText")));
            initializeWhenBox();
            panel1.add(whenBox);
            whenBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    log.debug("whenBox was set");
                    setWhen(whenBox.getSelectedIndex() + 1);
                }
            });
            whenBox.setToolTipText(rbx.getString("WhenBoxTip"));
            panel1.add(whenSensorComboBox);
            whenSensorComboBox.setAllowNull(true);
            initializeBlockBox();
            panel1.add(blockBox);
            panelx.add(panel1);
            // to set optional delay setting
            JPanel panel11 = new JPanel();
            panel11.setLayout(new FlowLayout());
            panel11.add(new JLabel("    " + rbx.getString("OptionalDelay") + ": "));
            panel11.add(whenDataSpinner);
            whenDataSpinner.setToolTipText(rbx.getString("HintDelayData"));
            panel11.add(new JLabel(Bundle.getMessage("LabelMilliseconds")));
            panelx.add(panel11);
            JPanel spacer = new JPanel();
            spacer.setLayout(new FlowLayout());
            spacer.add(new JLabel("     "));
            panelx.add(spacer);
            // to set What action to take
            JPanel panel2 = new JPanel();
            panel2.setLayout(new FlowLayout());
            panel2.add(new JLabel(rbx.getString("WhatText")));
            initializeWhatBox();
            panel2.add(whatBox);
            whatBox.setToolTipText(rbx.getString("WhatBoxTip"));
            whatBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setWhat(whatBox.getSelectedIndex() + 1);
                }
            });
            panel2.add(whatStringField);
            whatStringField.setToolTipText(rbx.getString("HintSoundHornPatternString"));
            panelx.add(panel2);
            JPanel panel21 = new JPanel();
            panel21.setLayout(new FlowLayout());
            whatPercentSpinner.setModel(new SpinnerNumberModel(Float.valueOf(1.0f), Float.valueOf(0.1f), Float.valueOf(1.5f), Float.valueOf(0.01f)));
            whatPercentSpinner.setEditor(new JSpinner.NumberEditor(whatPercentSpinner, "# %")); // show as a percentage % sign
            panel21.add(whatPercentSpinner);
            panel21.add(whatMinuteSpinner1);
            panel21.add(whatMinuteSpinner2);
            panel21.add(locoFunctionSpinner);
            // signal comboboxes
            TitledBorder border = BorderFactory.createTitledBorder(rbx.getString("SelectASignal"));
            signalPanel = new JPanel();
            signalPanel.setBorder(border);
            signalPanel.add(new JLabel(rbx.getString("MastLabel")));
            signalPanel.add(signalMastComboBox);
            signalMastComboBox.setAllowNull(true);
            signalMastComboBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (signalMastComboBox.getSelectedIndex() > 0) {
                        signalHeadComboBox.setSelectedIndex(0); // choose either a head or a mast
                    }
                }
            });
            signalPanel.add(new JLabel(rbx.getString("HeadLabel")));
            signalPanel.add(signalHeadComboBox);
            signalHeadComboBox.setAllowNull(true);
            signalHeadComboBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (signalHeadComboBox.getSelectedIndex() > 0) {
                        signalMastComboBox.setSelectedIndex(0); // choose either a head or a mast
                    }
                }
            });
            signalMastComboBox.setToolTipText(rbx.getString("HintSignalEntry"));
            signalHeadComboBox.setToolTipText(rbx.getString("HintSignalEntry"));
            panel21.add(signalPanel);
            // On/Off buttons
            ButtonGroup onOffGroup = new ButtonGroup();
            onOffGroup.add(onButton);
            onOffGroup.add(offButton);
            panel21.add(onButton);
            panel21.add(offButton);
            panel21.add(doneSensorLabel);
            panel21.add(doneSensorComboBox);
            doneSensorComboBox.setAllowNull(true);
            panelx.add(panel21);
            contentPane.add(panelx);
            contentPane.add(new JSeparator());
            // add buttons
            JPanel but = new JPanel();
            but.setLayout(new FlowLayout());
            but.add(cancelAddEditActionButton = new JButton(Bundle.getMessage("ButtonCancel")));
            cancelAddEditActionButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    cancelAddEditActionPressed(e);
                }
            });
            cancelAddEditActionButton.setToolTipText(rbx.getString("CancelButtonHint"));
            createActionButton = new JButton(rbx.getString("CreateActionButton"));
            but.add(createActionButton);
            createActionButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    createActionPressed(e);
                }
            });
            createActionButton.setToolTipText(rbx.getString("CreateActionButtonHint"));
            updateActionButton = new JButton(rbx.getString("UpdateActionButton"));
            but.add(updateActionButton);
            updateActionButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    updateActionPressed(e);
                }
            });
            updateActionButton.setToolTipText(rbx.getString("UpdateActionButtonHint"));
            contentPane.add(but);
        }
        if (editActionMode) {
            // initialize window for the action being edited
            addEditActionFrame.setTitle(rbx.getString("TitleEditAction"));
            updateActionButton.setVisible(true);
            createActionButton.setVisible(false);
            whenDataSpinner.setValue(curTSA.getDataWhen());
            whenSensorComboBox.setSelectedItemByName(curTSA.getStringWhen());
            // spinners are set in setWhat()
            whatStringField.setText(curTSA.getStringWhat());
            onButton.setSelected(true);
            if (curTSA.getStringWhat().equals("Off")) {
                offButton.setSelected(true);
            }
            log.debug("setWhen called for edit of action, editmode = {}", editActionMode);
            whenBox.setSelectedIndex(curTSA.getWhenCode() - 1);
            // setWhen(curTSA.getWhenCode()) and setWhat(idem) are set via whenBox and whatBox
            whatBox.setSelectedIndex(curTSA.getWhatCode() - 1);
            setBlockBox();
        } else {
            // initialize for add new action
            addEditActionFrame.setTitle(rbx.getString("TitleAddAction"));
            whenBox.setSelectedIndex(0);
            // setWhen(1) and setWhat(1) are set from the whenBox and whatBox listeners
            whatBox.setSelectedIndex(0);
            // set initial values after setting model
            whenDataSpinner.setValue(0);
            whenSensorComboBox.setSelectedItem(0);
            whatPercentSpinner.setValue(Float.valueOf(1.0f));
            whatMinuteSpinner1.setValue(100);
            whatMinuteSpinner2.setValue(100);
            locoFunctionSpinner.setValue(0);
            signalMastComboBox.setSelectedItem(0);
            signalHeadComboBox.setSelectedItem(0);
            doneSensorComboBox.setSelectedItem(0);
            whatStringField.setText("");
            onButton.setSelected(true);
            updateActionButton.setVisible(false);
            createActionButton.setVisible(true);
            setBlockBox();
        }
        addEditActionFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (addEditActionFrame != null) {
                    addEditActionFrame.setVisible(false);
                    addEditActionFrame.dispose();
                    addEditActionFrame = null;
                }
            }
        });
        addEditActionFrame.pack();
        addEditActionFrame.setVisible(true);
    }

    /**
     * Set special stuff depending on When selected.
     *
     * @param code selected item in getWhenBox
     */
    private void setWhen(int code) {
        // setting the whenBox here causes recursion
        whenSensorComboBox.setVisible(false);
        blockBox.setVisible(false);
        log.debug("setWhen code = {}", code);
        switch (code) {
            case TransitSectionAction.ENTRY:
            case TransitSectionAction.EXIT:
            case TransitSectionAction.TRAINSTOP:
            case TransitSectionAction.TRAINSTART:
                break;
            case TransitSectionAction.BLOCKENTRY:
            case TransitSectionAction.BLOCKEXIT:
                blockBox.setVisible(true);
                blockBox.setToolTipText(rbx.getString("HintBlockEntry"));
                break;
            case TransitSectionAction.SENSORACTIVE:
            case TransitSectionAction.SENSORINACTIVE:
                whenSensorComboBox.setVisible(true);
                whenSensorComboBox.setToolTipText(rbx.getString("HintSensorEntry"));
                break;
            default:
                log.debug("Unhandled transit action code: {}", code); // causes too much noise, no harm done hiding it
        }
        addEditActionFrame.pack();
        addEditActionFrame.setVisible(true);
    }

    /**
     * Set special stuff depending on What selected, including spinner value.
     *
     * @param code selected item in getWhatBox
     */
    private void setWhat(int code) {
        // setting the whatBox here causes recursion
        // hide all input boxes, set those needed visible via a switch case
        whatStringField.setVisible(false);
        whatPercentSpinner.setVisible(false);
        whatMinuteSpinner1.setVisible(false);
        whatMinuteSpinner2.setVisible(false);
        locoFunctionSpinner.setVisible(false);
        signalPanel.setVisible(false);
        onButton.setVisible(false);
        offButton.setVisible(false);
        doneSensorLabel.setVisible(false);
        doneSensorComboBox.setVisible(false);
        log.debug("setWhat code = {}", code);
        switch (code) {
            case TransitSectionAction.PAUSE:
                if (editActionMode) {
                    whatMinuteSpinner1.setValue(Math.max(curTSA.getDataWhat1(), 1));
                }
                whatMinuteSpinner1.setModel(new SpinnerNumberModel(1, 1, 65500, 1));
                whatMinuteSpinner1.setVisible(true);
                whatMinuteSpinner1.setToolTipText(rbx.getString("HintPauseData"));
                break;
            case TransitSectionAction.SETMAXSPEED:
            case TransitSectionAction.SETCURRENTSPEED:
            case TransitSectionAction.RAMPTRAINSPEED:
                if (editActionMode) {
                    float maxPerc = Math.max(Float.valueOf(0.01f * curTSA.getDataWhat1()), 0.1f);
                    whatPercentSpinner.setValue(maxPerc);
                }
                whatPercentSpinner.setVisible(true);
                whatPercentSpinner.setToolTipText(rbx.getString("HintSetSpeedData1"));
                break;
            case TransitSectionAction.TOMANUALMODE:
                if (editActionMode) {
                    doneSensorComboBox.setSelectedItemByName(curTSA.getStringWhat());
                }
                doneSensorLabel.setVisible(true);
                doneSensorComboBox.setVisible(true);
                doneSensorComboBox.setToolTipText(rbx.getString("HintDoneSensor"));
                break;
            case TransitSectionAction.SETLIGHT:
                onButton.setVisible(true);
                offButton.setVisible(true);
                onButton.setToolTipText(rbx.getString("HintSetLight"));
                offButton.setToolTipText(rbx.getString("HintSetLight"));
                break;
            case TransitSectionAction.STARTBELL:
                break;
            case TransitSectionAction.STOPBELL:
                break;
            case TransitSectionAction.SOUNDHORN:
                whatMinuteSpinner1.setValue(100);
                whatMinuteSpinner1.setModel(new SpinnerNumberModel(100, 100, 65500, 1));
                if (editActionMode) {
                    whatMinuteSpinner1.setValue(curTSA.getDataWhat1());
                }
                if ((Integer) whatMinuteSpinner1.getValue() < 100) {
                    whatMinuteSpinner1.setValue(100); // might result from user changing from PAUSE to SOUNDHORN
                }
                whatMinuteSpinner1.setVisible(true);
                whatMinuteSpinner1.setToolTipText(rbx.getString("HintSoundHornData1"));
                break;
            case TransitSectionAction.SOUNDHORNPATTERN:
                whatMinuteSpinner1.setValue(100);
                whatMinuteSpinner1.setModel(new SpinnerNumberModel(100, 100, 65500, 1));
                // whatMinuteSpinner2 model never changes
                if (editActionMode) {
                    whatMinuteSpinner1.setValue(curTSA.getDataWhat1());
                    whatMinuteSpinner2.setValue(Math.max(curTSA.getDataWhat2(), 100));
                    // might result from user changing from sth.else to SOUNDHORNPATTERN
                }
                if ((Integer) whatMinuteSpinner1.getValue() < 100) {
                    whatMinuteSpinner1.setValue(100); // might result from user changing from PAUSE to SOUNDHORNPATTERN
                }
                whatMinuteSpinner1.setVisible(true);
                whatMinuteSpinner1.setToolTipText(rbx.getString("HintSoundHornPatternData1"));
                whatMinuteSpinner2.setVisible(true);
                whatMinuteSpinner2.setToolTipText(rbx.getString("HintSoundHornPatternData2"));
                whatStringField.setVisible(true);
                break;
            case TransitSectionAction.LOCOFUNCTION:
                if (editActionMode) {
                    locoFunctionSpinner.setValue(curTSA.getDataWhat1());
                }
                locoFunctionSpinner.setVisible(true);
                locoFunctionSpinner.setToolTipText(rbx.getString("HintLocoFunctionData1"));
                onButton.setVisible(true);
                offButton.setVisible(true);
                onButton.setToolTipText(rbx.getString("HintLocoFunctionOnOff"));
                offButton.setToolTipText(rbx.getString("HintLocoFunctionOnOff"));
                break;
            case TransitSectionAction.SETSENSORACTIVE:
            case TransitSectionAction.SETSENSORINACTIVE:
                if (editActionMode) {
                    doneSensorComboBox.setSelectedItemByName(curTSA.getStringWhat());
                }
                doneSensorComboBox.setVisible(true);
                doneSensorComboBox.setToolTipText(rbx.getString("HintSensorEntry"));
                break;
            case TransitSectionAction.HOLDSIGNAL:
            case TransitSectionAction.RELEASESIGNAL:
                if (editActionMode) {
                    SignalMast sm = null;
                    sm = InstanceManager.getDefault(SignalMastManager.class).getSignalMast(curTSA.getStringWhat());
                    if (sm != null) { // name is an existing mast
                        signalMastComboBox.setSelectedItemByName(curTSA.getStringWhat());
                    } else {
                        SignalHead sh = null;
                        sh = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(curTSA.getStringWhat());
                        if (sh != null) { // name is an existing head
                            signalHeadComboBox.setSelectedItemByName(curTSA.getStringWhat());
                        }
                    }
                }
                signalPanel.setVisible(true);
                break;
            default:
                log.debug("Unhandled transit section action: {}", code); // causes too much noise, no harm done hiding it
                break;
        }
        addEditActionFrame.pack();
        addEditActionFrame.setVisible(true);
    }

    // temporary action variables
    private int tWhen = 0;
    private int tWhenData = 0;
    private String tWhenString = "";
    private int tWhat = 0;
    private int tWhatData1 = 0;
    private int tWhatData2 = 0;
    private String tWhatString = "";

    /**
     * Handle button presses in Add/Edit Transit Action window.
     *
     * @param e the event seen
     */
    private void createActionPressed(ActionEvent e) {
        if ((!validateWhenData()) || (!validateWhatData())) {
            return;
        }
        // entered data is OK, create a special action
        curTSA = new TransitSectionAction(tWhen, tWhat, tWhenData, tWhatData1, tWhatData2, tWhenString, tWhatString);
        List<TransitSectionAction> list = action[activeRow];
        list.add(curTSA);
        actionTableModel.fireTableDataChanged();
        addEditActionFrame.setVisible(false);
        addEditActionFrame.dispose();
        addEditActionFrame = null;
    }

    private void updateActionPressed(ActionEvent e) {
        if ((!validateWhenData()) || (!validateWhatData())) {
            return;
        }
        // entered data is OK, update the current special action
        curTSA.setWhenCode(tWhen);
        curTSA.setWhatCode(tWhat);
        curTSA.setDataWhen(tWhenData);
        curTSA.setDataWhat1(tWhatData1);
        curTSA.setDataWhat2(tWhatData2);
        curTSA.setStringWhen(tWhenString);
        curTSA.setStringWhat(tWhatString);
        actionTableModel.fireTableDataChanged();
        addEditActionFrame.setVisible(false);
        addEditActionFrame.dispose();
        addEditActionFrame = null;
    }

    private void cancelAddEditActionPressed(ActionEvent e) {
        addEditActionFrame.setVisible(false);
        addEditActionFrame.dispose();
        addEditActionFrame = null;
    }

    private boolean validateWhenData() {
        tWhen = whenBox.getSelectedIndex() + 1;
        tWhenData = (Integer) whenDataSpinner.getValue(); // always int within range from JSpinner
        tWhenString = "";
        if ((tWhen == TransitSectionAction.SENSORACTIVE) || (tWhen == TransitSectionAction.SENSORINACTIVE)) {
            if (whenSensorComboBox.getSelectedIndex() != 0) { // it's optional, so might be 0
                tWhenString = whenSensorComboBox.getSelectedItemSystemName();
            }
            if (!validateSensor(tWhenString, true)) {
                return false;
            }
        }
        if ((tWhen == TransitSectionAction.BLOCKENTRY) || (tWhen == TransitSectionAction.BLOCKEXIT)) {
            tWhenString = blockList.get(blockBox.getSelectedIndex()).getSystemName();
        }
        return true;
    }

    private boolean validateSensor(String sName, boolean when) {
        // check if anything entered
        if (sName.length() < 1) {
            // no sensor selected
            JOptionPane.showMessageDialog(addEditActionFrame, (rbx.getString("NoSensorError")),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        // get the sensor corresponding to this name
        Sensor s = InstanceManager.sensorManagerInstance().getSensor(sName);
        if (s == null) {
            // There is no sensor corresponding to this name
            JOptionPane.showMessageDialog(addEditActionFrame, (rbx.getString("SensorEntryError")),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (!sName.equals(s.getUserName())) {
            if (when) {
                tWhenString = sName;
            } else {
                tWhatString = sName;
            }
        }
        return true;
    }

    private boolean validateSignal(String sName, boolean when) {
        // check if anything is selected
        if (sName.length() < 1) {
            // no signal selected
            JOptionPane.showMessageDialog(addEditActionFrame, (rbx.getString("NoSignalError")),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        // get the signalMast or signalHead corresponding to this name
        SignalMast sm = null;
        SignalHead sh = null;
        sm = InstanceManager.getDefault(SignalMastManager.class).getSignalMast(sName);
        if (sm == null) {
            sh = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(sName);
        }
        if (sm == null && sh == null) {
            // There is no signal corresponding to this name
            JOptionPane.showMessageDialog(addEditActionFrame, (rbx.getString("SignalEntryError")),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    /**
     * Validate entered data for selected Action. Converted to use JSpinners
     * where applicable, 2017.
     *
     * @return true if data entered into field whatStringField is valid for selected Action type tWhat
     */
    private boolean validateWhatData() {
        tWhat = whatBox.getSelectedIndex() + 1;
        tWhatData1 = 0;
        tWhatData2 = 0;
        tWhatString = "";
        switch (tWhat) {
            case TransitSectionAction.SETMAXSPEED:
            case TransitSectionAction.SETCURRENTSPEED:
            case TransitSectionAction.RAMPTRAINSPEED:
                tWhatData1 = Math.round(100 * (float) whatPercentSpinner.getValue());
                break;
            case TransitSectionAction.TOMANUALMODE:
                if (doneSensorComboBox.getSelectedIndex() != 0) { // it's optional, so might be 0
                    tWhatString = doneSensorComboBox.getSelectedItemSystemName(); // sensor system name
                }
                if (tWhatString.length() >= 1) {
                    if (!validateSensor(tWhatString, false)) {
                        tWhatString = "";
                    }
                }
                break;
            case TransitSectionAction.SETLIGHT:
                tWhatString = "On"; // NOI18N
                if (offButton.isSelected()) {
                    tWhatString = "Off"; // NOI18N
                }
                break;
            case TransitSectionAction.STARTBELL:
            case TransitSectionAction.STOPBELL:
                break;
            case TransitSectionAction.PAUSE:
            case TransitSectionAction.SOUNDHORN:
                tWhatData1 = (Integer) whatMinuteSpinner1.getValue();
                break;
            case TransitSectionAction.SOUNDHORNPATTERN:
                tWhatData1 = (Integer) whatMinuteSpinner1.getValue();
                tWhatData2 = (Integer) whatMinuteSpinner2.getValue();
                tWhatString = whatStringField.getText();
                if ((tWhatString == null) || tWhatString.equals("") || (tWhatString.length() < 1)) {
                    JOptionPane.showMessageDialog(addEditActionFrame, (rbx.getString("MissingPattern")),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                tWhatString = tWhatString.trim().toLowerCase();
                for (int i = 0; i < tWhatString.length(); i++) {
                    char c = tWhatString.charAt(i);
                    if ((c != 's') && (c != 'l')) {
                        JOptionPane.showMessageDialog(addEditActionFrame, (rbx.getString("ErrorPattern")),
                                Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                }
                whatStringField.setText(tWhatString); // re-enter normalized value in display field
                break;
            case TransitSectionAction.LOCOFUNCTION:
                tWhatData1 = (Integer) locoFunctionSpinner.getValue();
                tWhatString = "On"; // NOI18N
                if (offButton.isSelected()) {
                    tWhatString = "Off"; // NOI18N
                }
                break;
            case TransitSectionAction.SETSENSORACTIVE:
            case TransitSectionAction.SETSENSORINACTIVE:
                if (doneSensorComboBox.getSelectedIndex() != 0) {
                    tWhatString = doneSensorComboBox.getSelectedItemSystemName();
                }
                if (!validateSensor(tWhatString, false)) {
                    return false;
                }
                break;
            case TransitSectionAction.HOLDSIGNAL:
            case TransitSectionAction.RELEASESIGNAL:
                if (signalMastComboBox.getSelectedIndex() != 0) {
                    tWhatString = signalMastComboBox.getSelectedItemSystemName();
                } else if (signalHeadComboBox.getSelectedIndex() != 0) {
                    tWhatString = signalHeadComboBox.getSelectedItemSystemName();
                }
                if (!validateSignal(tWhatString, false)) {
                    return false;
                }
                break;
            default:
                log.warn("Unhandled transit section action code: {}", tWhat);
                break;
        }
        return true;
    }

    // initialize combos for add/edit action window
    private void initializeWhenBox() {
        whenBox.removeAllItems();
        for (int i = 1; i <= TransitSectionAction.NUM_WHENS; i++) {
            whenBox.addItem(getWhenMenuText(i));
        }
    }

    private String getWhenMenuText(int i) {
        switch (i) {
            case TransitSectionAction.ENTRY:
                return rbx.getString("OnEntry");
            case TransitSectionAction.EXIT:
                return rbx.getString("OnExit");
            case TransitSectionAction.BLOCKENTRY:
                return rbx.getString("OnBlockEntry");
            case TransitSectionAction.BLOCKEXIT:
                return rbx.getString("OnBlockExit");
            case TransitSectionAction.TRAINSTOP:
                return rbx.getString("TrainStop");
            case TransitSectionAction.TRAINSTART:
                return rbx.getString("TrainStart");
            case TransitSectionAction.SENSORACTIVE:
                return rbx.getString("OnSensorActive");
            case TransitSectionAction.SENSORINACTIVE:
                return rbx.getString("OnSensorInactive");
            default:
                log.warn("Unhandled transit section action code: {}", i);
                break;
        }
        return "WHEN";
    }

    private void initializeWhatBox() {
        whatBox.removeAllItems();
        for (int i = 1; i <= TransitSectionAction.NUM_WHATS; i++) {
            whatBox.addItem(getWhatMenuText(i));
        }
    }

    private String getWhatMenuText(int i) {
        switch (i) {
            case TransitSectionAction.PAUSE:
                return rbx.getString("Pause");
            case TransitSectionAction.SETMAXSPEED:
                return rbx.getString("SetMaxSpeed");
            case TransitSectionAction.SETCURRENTSPEED:
                return rbx.getString("SetTrainSpeed");
            case TransitSectionAction.RAMPTRAINSPEED:
                return rbx.getString("RampTrainSpeed");
            case TransitSectionAction.TOMANUALMODE:
                return rbx.getString("ToManualMode");
            case TransitSectionAction.SETLIGHT:
                return rbx.getString("SetLight");
            case TransitSectionAction.STARTBELL:
                return rbx.getString("StartBell");
            case TransitSectionAction.STOPBELL:
                return rbx.getString("StopBell");
            case TransitSectionAction.SOUNDHORN:
                return rbx.getString("SoundHorn");
            case TransitSectionAction.SOUNDHORNPATTERN:
                return rbx.getString("SoundHornPattern");
            case TransitSectionAction.LOCOFUNCTION:
                return rbx.getString("LocoFunction");
            case TransitSectionAction.SETSENSORACTIVE:
                return rbx.getString("SetSensorActive");
            case TransitSectionAction.SETSENSORINACTIVE:
                return rbx.getString("SetSensorInactive");
            case TransitSectionAction.HOLDSIGNAL:
                return rbx.getString("HoldSignal");
            case TransitSectionAction.RELEASESIGNAL:
                return rbx.getString("ReleaseSignal");
            default:
                log.warn("Unhandled transit section action code: {}", i);
                break;
        }
        return "WHAT";
    }

    private void initializeBlockBox() {
        blockList = sectionList.get(activeRow).getBlockList();
        blockBox.removeAllItems();
        for (int i = 0; i < blockList.size(); i++) {
            String s = blockList.get(i).getSystemName();
            String uname = blockList.get(i).getUserName();
            if ((uname != null) && (!uname.equals(""))) {
                s = s + "(" + uname + ")";
            }
            blockBox.addItem(s);
        }
    }

    private void setBlockBox() {
        if (editActionMode) {
            if ((curTSA.getWhenCode() == TransitSectionAction.BLOCKENTRY)
                    || (curTSA.getWhenCode() == TransitSectionAction.BLOCKEXIT)) {
                // assumes that initializeBlockBox has been called prior to this call
                for (int i = 0; i < blockList.size(); i++) {
                    if (curTSA.getStringWhen().equals(blockList.get(i).getSystemName())) {
                        blockBox.setSelectedIndex(i);
                        return;
                    }
                }
            }
        }
        blockBox.setSelectedIndex(0);
    }

    private void editAction(int r) {
        curTSA = action[activeRow].get(r);
        editActionMode = true;
        addEditActionWindow();
    }

    private void deleteAction(int r) {
        TransitSectionAction tsa = action[activeRow].get(r);
        action[activeRow].remove(r);
        tsa.dispose();
        actionTableModel.fireTableDataChanged();
    }

    /**
     * Build display When string for Actions table.
     *
     * @param r row in the Special Actions table. A TransitSectionAction must be
     *          available for this row.
     * @return display string including entered values
     */
    private String getWhenText(int r) {
        TransitSectionAction tsa = action[activeRow].get(r);
        switch (tsa.getWhenCode()) {
            case TransitSectionAction.ENTRY:
                if (tsa.getDataWhen() > 0) {
                    return java.text.MessageFormat.format(rbx.getString("OnEntryDelayedFull"),
                            new Object[]{"" + tsa.getDataWhen()});
                }
                return rbx.getString("OnEntryFull");
            case TransitSectionAction.EXIT:
                if (tsa.getDataWhen() > 0) {
                    return java.text.MessageFormat.format(rbx.getString("OnExitDelayedFull"),
                            new Object[]{"" + tsa.getDataWhen()});
                }
                return rbx.getString("OnExitFull");
            case TransitSectionAction.BLOCKENTRY:
                if (tsa.getDataWhen() > 0) {
                    return java.text.MessageFormat.format(rbx.getString("OnBlockEntryDelayedFull"),
                            new Object[]{"" + tsa.getDataWhen(), tsa.getStringWhen()});
                }
                return java.text.MessageFormat.format(rbx.getString("OnBlockEntryFull"),
                        new Object[]{tsa.getStringWhen()});
            case TransitSectionAction.BLOCKEXIT:
                if (tsa.getDataWhen() > 0) {
                    return java.text.MessageFormat.format(rbx.getString("OnBlockExitDelayedFull"),
                            new Object[]{"" + tsa.getDataWhen(), tsa.getStringWhen()});
                }
                return java.text.MessageFormat.format(rbx.getString("OnBlockExitFull"),
                        new Object[]{tsa.getStringWhen()});
            case TransitSectionAction.TRAINSTOP:
                if (tsa.getDataWhen() > 0) {
                    return java.text.MessageFormat.format(rbx.getString("TrainStopDelayedFull"),
                            new Object[]{"" + tsa.getDataWhen()});
                }
                return rbx.getString("TrainStopFull");
            case TransitSectionAction.TRAINSTART:
                if (tsa.getDataWhen() > 0) {
                    return java.text.MessageFormat.format(rbx.getString("TrainStartDelayedFull"),
                            new Object[]{"" + tsa.getDataWhen()});
                }
                return rbx.getString("TrainStartFull");
            case TransitSectionAction.SENSORACTIVE:
                if (tsa.getDataWhen() > 0) {
                    return java.text.MessageFormat.format(rbx.getString("OnSensorActiveDelayedFull"),
                            new Object[]{"" + tsa.getDataWhen(), tsa.getStringWhen()});
                }
                return java.text.MessageFormat.format(rbx.getString("OnSensorActiveFull"),
                        new Object[]{tsa.getStringWhen()});
            case TransitSectionAction.SENSORINACTIVE:
                if (tsa.getDataWhen() > 0) {
                    return java.text.MessageFormat.format(rbx.getString("OnSensorInactiveDelayedFull"),
                            new Object[]{"" + tsa.getDataWhen(), tsa.getStringWhen()});
                }
                return java.text.MessageFormat.format(rbx.getString("OnSensorInactiveFull"),
                        new Object[]{tsa.getStringWhen()});
            default:
                log.warn("Unhandled transit section action When code: {}", tsa.getWhenCode());
                break;
        }
        return "WHEN";
    }

    /**
     * Build display What string for Actions table.
     *
     * @param r row in the Special Actions table. A TransitSectionAction must be
     *          available for this row.
     * @return display string including entered values
     */
    private String getWhatText(int r) {
        TransitSectionAction tsa = action[activeRow].get(r);
        switch (tsa.getWhatCode()) {
            case TransitSectionAction.PAUSE:
                return java.text.MessageFormat.format(rbx.getString("PauseFull"),
                        new Object[]{tsa.getDataWhat1()});
            case TransitSectionAction.SETMAXSPEED:
                return java.text.MessageFormat.format(rbx.getString("SetMaxSpeedFull"),
                        new Object[]{tsa.getDataWhat1()});
            case TransitSectionAction.SETCURRENTSPEED:
                return java.text.MessageFormat.format(rbx.getString("SetTrainSpeedFull"),
                        new Object[]{tsa.getDataWhat1()});
            case TransitSectionAction.RAMPTRAINSPEED:
                return java.text.MessageFormat.format(rbx.getString("RampTrainSpeedFull"),
                        new Object[]{"" + tsa.getDataWhat1()});
            case TransitSectionAction.TOMANUALMODE:
                if (tsa.getStringWhat().length() > 0) {
                    return java.text.MessageFormat.format(rbx.getString("ToManualModeAltFull"),
                            new Object[]{tsa.getStringWhat()});
                }
                return rbx.getString("ToManualModeFull");
            case TransitSectionAction.SETLIGHT:
                if (tsa.getStringWhat().equals("Off")) {
                    return java.text.MessageFormat.format(rbx.getString("SetLightFull"),
                        new Object[]{Bundle.getMessage("StateOff")});
                }
                return java.text.MessageFormat.format(rbx.getString("SetLightFull"),
                        new Object[]{Bundle.getMessage("StateOn")});
            case TransitSectionAction.STARTBELL:
                return rbx.getString("StartBellFull");
            case TransitSectionAction.STOPBELL:
                return rbx.getString("StopBellFull");
            case TransitSectionAction.SOUNDHORN:
                return java.text.MessageFormat.format(rbx.getString("SoundHornFull"),
                        new Object[]{tsa.getDataWhat1()});
            case TransitSectionAction.SOUNDHORNPATTERN:
                return java.text.MessageFormat.format(rbx.getString("SoundHornPatternFull"),
                        new Object[]{tsa.getStringWhat(), "" + tsa.getDataWhat1(), "" + tsa.getDataWhat2()});
            case TransitSectionAction.LOCOFUNCTION:
                if (tsa.getStringWhat().equals("Off")) {
                    return java.text.MessageFormat.format(rbx.getString("LocoFunctionFull"),
                            new Object[]{"" + tsa.getDataWhat1(), Bundle.getMessage("StateOff")});
                }
                return java.text.MessageFormat.format(rbx.getString("LocoFunctionFull"),
                        new Object[]{"" + tsa.getDataWhat1(), Bundle.getMessage("StateOn")});
            case TransitSectionAction.SETSENSORACTIVE:
                return java.text.MessageFormat.format(rbx.getString("SetSensorActiveFull"),
                        new Object[]{tsa.getStringWhat()});
            case TransitSectionAction.SETSENSORINACTIVE:
                return java.text.MessageFormat.format(rbx.getString("SetSensorInactiveFull"),
                        new Object[]{tsa.getStringWhat()});
            case TransitSectionAction.HOLDSIGNAL:
                return java.text.MessageFormat.format(rbx.getString("HoldSignalFull"),
                        new Object[]{tsa.getStringWhat()});
            case TransitSectionAction.RELEASESIGNAL:
                return java.text.MessageFormat.format(rbx.getString("ReleaseSignalFull"),
                        new Object[]{tsa.getStringWhat()});
            default:
                log.warn("Unhandled transit section action What code: {}", tsa.getWhatCode());
                break;
        }
        return "WHAT";
    }

    private String getSectionNameByRow(int r) {
        String s = sectionList.get(r).getSystemName();
        String u = sectionList.get(r).getUserName();
        if ((u != null) && (!u.equals(""))) {
            return (s + " ( " + u + " )");
        }
        return s;
    }

    /**
     * Table model for Sections in Create/Edit Transit window.
     */
    public class SectionTableModel extends javax.swing.table.AbstractTableModel implements
            java.beans.PropertyChangeListener {

        public static final int SEQUENCE_COLUMN = 0;
        public static final int SECTIONNAME_COLUMN = 1;
        public static final int ACTION_COLUMN = 2;
        public static final int SEC_DIRECTION_COLUMN = 3;
        public static final int ALTERNATE_COLUMN = 4;
        public static final int SAFE_COLUMN = 5;
        public static final int STOPALLOCATING_SENSOR = 6;
        public static final int NUMBER_OF_COLUMNS = 7;

        public SectionTableModel() {
            super();
            sectionManager.addPropertyChangeListener(this);
        }

        @Override
        public void propertyChange(java.beans.PropertyChangeEvent e) {
            if (e.getPropertyName().equals("length")) {
                // a new NamedBean is available in the manager
                fireTableDataChanged();
            }
            if (e.getSource() instanceof jmri.SensorManager) {
                if (e.getPropertyName().equals("DisplayListName")) {
                    updateSensorList();
                }
            }
        }

        @Override
        public Class<?> getColumnClass(int c) {
            if (c == ACTION_COLUMN) {
                return JButton.class;
            }
            if (c == SAFE_COLUMN) {
                return Boolean.class;
            }
            if (c == STOPALLOCATING_SENSOR) {
                return JComboBox.class;
            }
            return String.class;
        }

        @Override
        public int getColumnCount() {
            return NUMBER_OF_COLUMNS;
        }

        @Override
        public int getRowCount() {
            return (sectionList.size());
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            if (c == ACTION_COLUMN) {
                return (true);
            } else if (c == SAFE_COLUMN) {
                return(true);
            } else if (c == STOPALLOCATING_SENSOR) {
                return(true);
            }
            return (false);
        }

        @Override
        public String getColumnName(int col) {
            switch (col) {
                case SEQUENCE_COLUMN:
                    return rbx.getString("SequenceColName");
                case SECTIONNAME_COLUMN:
                    return Bundle.getMessage("BeanNameSection");
                case ACTION_COLUMN:
                    return rbx.getString("ActionColName");
                case SEC_DIRECTION_COLUMN:
                    return rbx.getString("DirectionColName");
                case ALTERNATE_COLUMN:
                    return rbx.getString("AlternateColName");
                case SAFE_COLUMN:
                    return rbx.getString("SafeColName");
                case STOPALLOCATING_SENSOR:
                    return rbx.getString("StopAllocationColName");
               default:
                    return "";
            }
        }

        @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "DB_DUPLICATE_SWITCH_CLAUSES",
                                justification="better to keep cases in column order rather than to combine")
        public int getPreferredWidth(int col) {
            switch (col) {
                case SEQUENCE_COLUMN:
                    return new JTextField(8).getPreferredSize().width;
                case SECTIONNAME_COLUMN:
                    return new JTextField(17).getPreferredSize().width;
                case ACTION_COLUMN:
                    return new JTextField(12).getPreferredSize().width;
                case SEC_DIRECTION_COLUMN:
                    return new JTextField(12).getPreferredSize().width;
                case ALTERNATE_COLUMN:
                    return new JTextField(12).getPreferredSize().width;
                case SAFE_COLUMN:
                    return new JTextField(4).getPreferredSize().width;
                case STOPALLOCATING_SENSOR:
                    return new JTextField(12).getPreferredSize().width;
                default:
                    // fall through
                    break;
            }
            return new JTextField(5).getPreferredSize().width;
        }

        @Override
        public Object getValueAt(int r, int c) {
            int rx = r;
            if (rx > sectionList.size()) {
                return null;
            }
            switch (c) {
                case SEQUENCE_COLUMN:
                    return ("" + sequence[rx]);
                case SECTIONNAME_COLUMN:
                    return (getSectionNameByRow(rx));
                case ACTION_COLUMN:
                    return rbx.getString("AddEditActions");
                case SEC_DIRECTION_COLUMN:
                    if (direction[rx] == Section.FORWARD) {
                        return rbx.getString("SectionForward");
                    } else if (direction[rx] == Section.REVERSE) {
                        return rbx.getString("SectionReverse");
                    }
                    return Bundle.getMessage("BeanStateUnknown");
                case ALTERNATE_COLUMN:
                    if (alternate[rx]) {
                        return rbx.getString("Alternate");
                    }
                    return rbx.getString("Primary");
                case SAFE_COLUMN:
                    boolean val = safe[rx];
                    return Boolean.valueOf(val);
                case STOPALLOCATING_SENSOR:
                    String sensor = sensorStopAllocation[rx];
                    JComboBox<String> cb = new JComboBox<>(sensorList);
                    String name = "";
                    if (sensor != null) {
                        name = sensor;
                    }
                    cb.setSelectedItem(name);
                    return cb;
                default:
                    return Bundle.getMessage("BeanStateUnknown");
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public void setValueAt(Object value, int row, int col) {
            if (col == ACTION_COLUMN) {
                addEditActionsPressed(row);
            } else if (col == SAFE_COLUMN) {
                boolean val = ((Boolean) value).booleanValue();
                safe[row] = val; // use checkbox to show Safe
            } else if (col == STOPALLOCATING_SENSOR) {
                JComboBox<String> cb = (JComboBox<String>) value;
                if (cb.getSelectedIndex() < 0) {
                    sensorStopAllocation[row] = "";
                } else {
                    sensorStopAllocation[row] = (String) cb.getSelectedItem();
                }
             }
            return;
        }
    }

    private void updateSensorList() {
        Set<Sensor> nameSet = jmri.InstanceManager.sensorManagerInstance().getNamedBeanSet();
        String[] displayList = new String[nameSet.size()];
        int i = 0;
        for (Sensor nBean : nameSet) {
            if (nBean != null) {
                displayList[i++] = nBean.getDisplayName();
            }
        }
        java.util.Arrays.sort(displayList);
        sensorList = new String[displayList.length + 1];
        sensorList[0] = "";
        i = 1;
        for (String name : displayList) {
            sensorList[i] = name;
            i++;
        }
    }


    /**
     * Table model for Actions in Special Actions window. Currently shows max. 5
     * rows.
     */
    public class SpecialActionTableModel extends javax.swing.table.AbstractTableModel implements
            java.beans.PropertyChangeListener {

        public static final int WHEN_COLUMN = 0;
        public static final int WHAT_COLUMN = 1;
        public static final int EDIT_COLUMN = 2;
        public static final int REMOVE_COLUMN = 3;

        public SpecialActionTableModel() {
            super();
            sectionManager.addPropertyChangeListener(this);
        }

        @Override
        public void propertyChange(java.beans.PropertyChangeEvent e) {
            if (e.getPropertyName().equals("length")) {
                // a new NamedBean is available in the manager
                fireTableDataChanged();
            }
        }

        @Override
        public Class<?> getColumnClass(int c) {
            if (c == WHEN_COLUMN) {
                return String.class;
            }
            if (c == WHAT_COLUMN) {
                return String.class;
            }
            if (c == EDIT_COLUMN) {
                return JButton.class;
            }
            if (c == REMOVE_COLUMN) {
                return JButton.class;
            }
            return String.class;
        }

        @Override
        public int getColumnCount() {
            return REMOVE_COLUMN + 1;
        }

        @Override
        public int getRowCount() {
            return (action[activeRow].size());
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            if (c == WHEN_COLUMN) {
                return (false);
            }
            if (c == WHAT_COLUMN) {
                return (false);
            }
            if (c == EDIT_COLUMN) {
                return (true);
            }
            if (c == REMOVE_COLUMN) {
                return (true);
            }
            return (false);
        }

        @Override
        public String getColumnName(int col) {
            if (col == WHEN_COLUMN) {
                return rbx.getString("WhenColName");
            } else if (col == WHAT_COLUMN) {
                return rbx.getString("WhatColName");
            }
            return "";
        }

        @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "DB_DUPLICATE_SWITCH_CLAUSES",
                                justification="better to keep cases in column order rather than to combine")
        public int getPreferredWidth(int col) {
            switch (col) {
                case WHEN_COLUMN:
                    return new JTextField(50).getPreferredSize().width;
                case WHAT_COLUMN:
                    return new JTextField(50).getPreferredSize().width;
                case EDIT_COLUMN:
                    return new JTextField(8).getPreferredSize().width;
                case REMOVE_COLUMN:
                    return new JTextField(8).getPreferredSize().width;
                default:
                    // fall through
                    break;
            }
            return new JTextField(8).getPreferredSize().width;
        }

        @Override
        public Object getValueAt(int r, int c) {
            int rx = r;
            if (rx > action[activeRow].size()) {
                return null;
            }
            switch (c) {
                case WHEN_COLUMN:
                    return (getWhenText(rx));
                case WHAT_COLUMN:
                    return (getWhatText(rx));
                case EDIT_COLUMN:
                    return Bundle.getMessage("ButtonEdit");
                case REMOVE_COLUMN:
                    return Bundle.getMessage("ButtonDelete");
                default:
                    return Bundle.getMessage("BeanStateUnknown"); // normally not in use
            }
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            if (col == EDIT_COLUMN) {
                // set up to edit
                editAction(row);
            }
            if (col == REMOVE_COLUMN) {
                deleteAction(row);
            }
            return;
        }
    }

    @Override
    protected String getClassName() {
        return TransitTableAction.class.getName();
    }

    @Override
    public String getClassDescription() {
        return Bundle.getMessage("TitleTransitTable");
    }

    private final static Logger log = LoggerFactory.getLogger(TransitTableAction.class);

}
