package jmri.jmrit.beantable;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.ResourceBundle;
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
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import jmri.Block;
import jmri.EntryPoint;
import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.Section;
import jmri.SectionManager;
import jmri.Sensor;
import jmri.Transit;
import jmri.TransitManager;
import jmri.TransitSection;
import jmri.TransitSectionAction;
import jmri.util.JmriJFrame;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a TransitTable GUI.
 *
 * <P This file is part of JMRI. <P JMRI is open source software; you can
 * redistribute it and/or modify it under the terms of version 2 of the GNU
 * General Public License as published by the Free Software Foundation. See the
 * "COPYING" file for a copy of this license. <P JMRI is distributed in the hope
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * @author	Dave Duchamp Copyright (C) 2008, 2010, 2011
 */
public class TransitTableAction extends AbstractTableAction {

    /**
     * Create an action with a specific title. <P Note that the argument is the
     * Action title, not the title of the resulting frame. Perhaps this should
     * be changed? @param actionName
     */
    public TransitTableAction(String actionName) {
        super(actionName);
        // set manager - no need to use InstanceManager here
        transitManager = jmri.InstanceManager.transitManagerInstance();
        // disable ourself if there is no Transit manager available
        if (sectionManager == null) {
            setEnabled(false);
        }

    }

    public TransitTableAction() {
        this(Bundle.getMessage("TitleTransitTable"));
    }

    static final ResourceBundle rbx = ResourceBundle.getBundle("jmri.jmrit.beantable.SectionTransitTableBundle");

    /**
     * Create the JTable DataModel, along with the changes for the specific case
     * of Transit objects
     */
    protected void createModel() {
        m = new BeanTableDataModel() {

            static public final int EDITCOL = NUMCOLUMN;
            static public final int DUPLICATECOL = EDITCOL + 1;

            public String getValue(String name) {
                if (name == null) {
                    log.warn("requested getValue(null)");
                    return "(no name)";
                }
                Transit z = InstanceManager.transitManagerInstance().getBySystemName(name);
                if (z == null) {
                    log.debug("requested getValue(\"" + name + "\"), Transit doesn't exist");
                    return "(no Transit)";
                }
                return "Transit";
            }

            public Manager getManager() {
                return InstanceManager.transitManagerInstance();
            }

            public NamedBean getBySystemName(String name) {
                return InstanceManager.transitManagerInstance().getBySystemName(name);
            }

            public NamedBean getByUserName(String name) {
                return InstanceManager.transitManagerInstance().getByUserName(name);
            }
            /*public int getDisplayDeleteMsg() { return InstanceManager.getDefault(jmri.UserPreferencesManager.class).getMultipleChoiceOption(getClassName(),"delete"); }
             public void setDisplayDeleteMsg(int boo) { InstanceManager.getDefault(jmri.UserPreferencesManager.class).setMultipleChoiceOption(getClassName(), "delete", boo); }*/

            protected String getMasterClassName() {
                return getClassName();
            }

            public void clickOn(NamedBean t) {
            }

            public int getColumnCount() {
                return DUPLICATECOL + 1;
            }

            public Object getValueAt(int row, int col) {
                if (col == VALUECOL) {
                    // some error checking
                    if (row >= sysNameList.size()) {
                        log.debug("row is greater than name list");
                        return "";
                    }
                    Transit z = (Transit) getBySystemName(sysNameList.get(row));
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

            public void setValueAt(Object value, int row, int col) {
                if (col == EDITCOL) {
                    class WindowMaker implements Runnable {

                        int row;

                        WindowMaker(int r) {
                            row = r;
                        }

                        public void run() {
                            String sName = (String) getValueAt(row, SYSNAMECOL);
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

                        public void run() {
                            String sName = (String) getValueAt(row, SYSNAMECOL);
                            duplicatePressed(sName);
                        }
                    }
                    WindowMaker t = new WindowMaker(row);
                    javax.swing.SwingUtilities.invokeLater(t);
                } else {
                    super.setValueAt(value, row, col);
                }
            }

            public String getColumnName(int col) {
                if (col == EDITCOL) {
                    return "";   // no namne on Edit column
                }
                if (col == DUPLICATECOL) {
                    return "";   // no namne on Duplicate column
                }
                return super.getColumnName(col);
            }

            public Class<?> getColumnClass(int col) {
                if (col == VALUECOL) {
                    return String.class;  // not a button
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

            public void configValueColumn(JTable table) {
                // value column isn't button, so config is null
            }

            protected boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
                return true;
                // return (e.getPropertyName().indexOf("alue")=0);
            }

            public JButton configureButton() {
                log.error("configureButton should not have been called");
                return null;
            }

            protected String getBeanType() {
                return "Transit";
            }
        };
    }

    protected void setTitle() {
        f.setTitle(Bundle.getMessage("TitleTransitTable"));
    }

    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.TransitTable";
    }

    // instance variables
    private boolean editMode = false;
    private boolean duplicateMode = false;
    private TransitManager transitManager = null;
    private SectionManager sectionManager = InstanceManager.sectionManagerInstance();
    private Transit curTransit = null;
    private SectionTableModel sectionTableModel = null;
    private ArrayList<Section> sectionList = new ArrayList<>();
    private int[] direction = new int[150];
    private int[] sequence = new int[150];
    @SuppressWarnings("unchecked")
    private ArrayList<TransitSectionAction>[] action = new ArrayList[150];
    private boolean[] alternate = new boolean[150];
    private int maxSections = 150;  // must be equal to the dimension of the above arrays
    private ArrayList<Section> primarySectionBoxList = new ArrayList<>();
    private int[] priSectionDirection = new int[150];
    private ArrayList<Section> alternateSectionBoxList = new ArrayList<>();
    private int[] altSectionDirection = new int[150];
    private ArrayList<Section> insertAtBeginningBoxList = new ArrayList<>();
    private int[] insertAtBeginningDirection = new int[150];
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
    JButton removeLastSection = null;
    JButton removeFirstSection = null;
    JButton insertAtBeginning = null;
    JComboBox<String> insertAtBeginningBox = new JComboBox<>();
    JLabel seqNumLabel = new JLabel(rbx.getString("LabelSeqNum"));
    JTextField seqNum = new JTextField(5);
    JButton replacePrimaryForSequence = null;
    JButton deleteAlternateForSequence = null;
    JButton addAlternateForSequence = null;
    JComboBox<String> alternateSectionBox = new JComboBox<>();
    JButton addAlternateSection = null;
    JCheckBox _autoSystemName = new JCheckBox(Bundle.getMessage("LabelAutoSysName"));
    jmri.UserPreferencesManager pref;
    String systemNameAuto = this.getClass().getName() + ".AutoSystemName";

    /**
     * Responds to the Add... button and the Edit buttons in Transit Table
     */
    protected void addPressed(ActionEvent e) {
        editMode = false;
        duplicateMode = false;
        if ((sectionManager.getSystemNameList().size()) > 0) {
            addEditPressed();
        } else {
            javax.swing.JOptionPane.showMessageDialog(null, rbx
                    .getString("Message21"), rbx.getString("ErrorTitle"),
                    javax.swing.JOptionPane.ERROR_MESSAGE);
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
            p = new JPanel();
            p.setLayout(new FlowLayout());
            p.add(sysNameLabel);
            p.add(sysNameFixed);
            p.add(sysName);
            sysName.setToolTipText(rbx.getString("TransitSystemNameHint"));
            p.add(new JLabel("     "));
            p.add(userNameLabel);
            p.add(userName);
            userName.setToolTipText(rbx.getString("TransitUserNameHint"));
            addFrame.getContentPane().add(p);
            p = new JPanel();
            ((FlowLayout) p.getLayout()).setVgap(0);
            p.add(_autoSystemName);
            _autoSystemName.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    autoSystemName();
                }
            });
            if (pref.getSimplePreferenceState(systemNameAuto)) {
                _autoSystemName.setSelected(true);
            }
            addFrame.getContentPane().add(p);
            addFrame.getContentPane().add(new JSeparator());
            JPanel p1 = new JPanel();
            p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
            JPanel p11 = new JPanel();
            p11.setLayout(new FlowLayout());
            p11.add(new JLabel(rbx.getString("SectionTableMessage")));
            p1.add(p11);
            JPanel p12 = new JPanel();
            // initialize table of sections
            sectionTableModel = new SectionTableModel();
            JTable sectionTable = new JTable(sectionTableModel);
            sectionTable.setRowSelectionAllowed(false);
            sectionTable.setPreferredScrollableViewportSize(new java.awt.Dimension(650, 150));
            TableColumnModel sectionColumnModel = sectionTable.getColumnModel();
            TableColumn sequenceColumn = sectionColumnModel.getColumn(SectionTableModel.SEQUENCE_COLUMN);
            sequenceColumn.setResizable(true);
            sequenceColumn.setMinWidth(50);
            sequenceColumn.setMaxWidth(70);
            TableColumn sectionColumn = sectionColumnModel.getColumn(SectionTableModel.SECTIONNAME_COLUMN);
            sectionColumn.setResizable(true);
            sectionColumn.setMinWidth(150);
            sectionColumn.setMaxWidth(210);
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
            TableColumn alternateColumn = sectionColumnModel.getColumn(SectionTableModel.ALTERNATE_COLUMN);
            alternateColumn.setResizable(true);
            alternateColumn.setMinWidth(140);
            alternateColumn.setMaxWidth(170);
            JScrollPane sectionTableScrollPane = new JScrollPane(sectionTable);
            p12.add(sectionTableScrollPane, BorderLayout.CENTER);
            p1.add(p12);
            JPanel p13 = new JPanel();
            p13.setLayout(new FlowLayout());
            p13.add(primarySectionBox);
            primarySectionBox.setToolTipText(rbx.getString("PrimarySectionBoxHint"));
            p13.add(addNextSection = new JButton(rbx.getString("AddPrimaryButton")));
            addNextSection.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    addNextSectionPressed(e);
                }
            });
            addNextSection.setToolTipText(rbx.getString("AddPrimaryButtonHint"));
            p1.add(p13);
            JPanel p14 = new JPanel();
            p14.setLayout(new FlowLayout());
            p14.add(alternateSectionBox);
            alternateSectionBox.setToolTipText(rbx.getString("AlternateSectionBoxHint"));
            p14.add(addAlternateSection = new JButton(rbx.getString("AddAlternateButton")));
            addAlternateSection.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    addAlternateSectionPressed(e);
                }
            });
            addAlternateSection.setToolTipText(rbx.getString("AddAlternateButtonHint"));
            p14.add(new JLabel("        "));
            p14.add(insertAtBeginningBox);
            insertAtBeginningBox.setToolTipText(rbx.getString("InsertAtBeginningBoxHint"));
            p14.add(insertAtBeginning = new JButton(rbx.getString("InsertAtBeginningButton")));
            insertAtBeginning.addActionListener(new ActionListener() {
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
                public void actionPerformed(ActionEvent e) {
                    deleteAllSections(e);
                }
            });
            deleteSections.setToolTipText(rbx.getString("DeleteSectionsButtonHint"));
            p15.add(new JLabel("     "));
            p15.add(removeLastSection = new JButton(rbx.getString("RemoveLastButton")));
            removeLastSection.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    removeLastSectionPressed(e);
                }
            });
            removeLastSection.setToolTipText(rbx.getString("RemoveLastButtonHint"));
            p15.add(new JLabel("     "));
            p15.add(removeFirstSection = new JButton(rbx.getString("RemoveFirstButton")));
            removeFirstSection.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    removeFirstSectionPressed(e);
                }
            });
            removeFirstSection.setToolTipText(rbx.getString("RemoveFirstButtonHint"));
            p1.add(p15);
            JPanel p16 = new JPanel();
            p16.setLayout(new FlowLayout());
            p16.add(seqNumLabel);
            p16.add(new JLabel("   "));
            p16.add(seqNum);
            seqNum.setToolTipText(rbx.getString("SeqNumHint"));
            p1.add(p16);
            JPanel p17 = new JPanel();
            p17.setLayout(new FlowLayout());
            p17.add(replacePrimaryForSequence = new JButton(rbx.getString("ReplacePrimaryForSeqButton")));
            replacePrimaryForSequence.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    replacePrimaryForSeqPressed(e);
                }
            });
            replacePrimaryForSequence.setToolTipText(rbx.getString("ReplacePrimaryForSeqButtonHint"));
            p17.add(new JLabel("     "));
            p17.add(deleteAlternateForSequence = new JButton(rbx.getString("DeleteAlternateForSeqButton")));
            deleteAlternateForSequence.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    deleteAlternateForSeqPressed(e);
                }
            });
            deleteAlternateForSequence.setToolTipText(rbx.getString("DeleteAlternateForSeqButtonHint"));
            p17.add(new JLabel("     "));
            p17.add(addAlternateForSequence = new JButton(rbx.getString("AddAlternateForSeqButton")));
            addAlternateForSequence.addActionListener(new ActionListener() {
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
                public void actionPerformed(ActionEvent e) {
                    cancelPressed(e);
                }
            });
            cancel.setToolTipText(rbx.getString("CancelButtonHint"));
            pb.add(create = new JButton(Bundle.getMessage("ButtonCreate")));
            create.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    createPressed(e);
                }
            });
            create.setToolTipText(rbx.getString("SectionCreateButtonHint"));
            pb.add(update = new JButton(Bundle.getMessage("ButtonUpdate")));
            update.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    updatePressed(e);
                }
            });
            update.setToolTipText(rbx.getString("SectionUpdateButtonHint"));
            addFrame.getContentPane().add(pb);
        }
        if (editMode) {
            // setup for edit window
            _autoSystemName.setVisible(false);
            sysNameLabel.setEnabled(true);
            create.setVisible(false);
            update.setVisible(true);
            sysName.setVisible(false);
            sysNameFixed.setVisible(true);
            initializeEditInformation();
        } else {
            // setup for create window
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
            ArrayList<TransitSection> tsList = curTransit.getTransitSectionList();
            for (int i = 0; i < tsList.size(); i++) {
                TransitSection ts = tsList.get(i);
                if (ts != null) {
                    sectionList.add(ts.getSection());
                    sequence[i] = ts.getSequenceNumber();
                    direction[i] = ts.getDirection();
                    action[i] = ts.getTransitSectionActionList();
                    alternate[i] = ts.isAlternate();
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
        }
        curSection = null;
        curSectionDirection = 0;
        prevSection = null;
        prevSectionDirection = 0;
        curSequenceNum = 0;
        initializeSectionCombos();
        sectionTableModel.fireTableDataChanged();
    }

    void addNextSectionPressed(ActionEvent e) {
        if (sectionList.size() > maxSections) {
            javax.swing.JOptionPane.showMessageDialog(addFrame, rbx
                    .getString("Message23"), rbx.getString("ErrorTitle"),
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (primarySectionBoxList.size() == 0) {
            javax.swing.JOptionPane.showMessageDialog(addFrame, rbx
                    .getString("Message25"), rbx.getString("ErrorTitle"),
                    javax.swing.JOptionPane.ERROR_MESSAGE);
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
                int k = j - 2;
                while ((k >= 0) && alternate[k]) {
                    k--;
                }
                prevSection = sectionList.get(k);
                prevSectionDirection = direction[k];
            }
            sectionList.remove(j);
            initializeSectionCombos();
        }
        sectionTableModel.fireTableDataChanged();
    }

    void insertAtBeginningPressed(ActionEvent e) {
        if (sectionList.size() > maxSections) {
            javax.swing.JOptionPane.showMessageDialog(addFrame, rbx
                    .getString("Message23"), rbx.getString("ErrorTitle"),
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (insertAtBeginningBoxList.size() == 0) {
            javax.swing.JOptionPane.showMessageDialog(addFrame, rbx
                    .getString("Message35"), rbx.getString("ErrorTitle"),
                    javax.swing.JOptionPane.ERROR_MESSAGE);
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
            }
            direction[0] = insertAtBeginningDirection[index];
            curSequenceNum++;
            sequence[0] = 1;
            alternate[0] = false;
            action[0] = new ArrayList<>();
            if (curSequenceNum == 2) {
                prevSectionDirection = direction[0];
                prevSection = s;
            }
            initializeSectionCombos();
        }
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
            }
            for (int k = 0; k < keep; k++) {
                sectionList.remove(0);
            }
            curSequenceNum--;
            initializeSectionCombos();
        }
        sectionTableModel.fireTableDataChanged();
    }

    void replacePrimaryForSeqPressed(ActionEvent e) {
        int seq = getSeqNum();
        if (seq == 0) {
            return;
        }
        Section sOld = null;
        ArrayList<Section> altOldList = new ArrayList<>();
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
            log.error("Missing primary Section for seq = " + seq);
            return;
        }
        ArrayList<Section> possibles = new ArrayList<>();
        int[] possiblesDirection = new int[150];
        ArrayList<String> possibleNames = new ArrayList<>();
        List<String> allSections = sectionManager.getSystemNameList();
        for (int i = 0; i < allSections.size(); i++) {
            Section mayBeSection = null;
            String mayBeName = allSections.get(i);
            int mayBeDirection = 0;
            Section s = sectionManager.getBySystemName(mayBeName);
            if ((s != null) && (s != sOld) && (s != beforeSection)
                    && (s != afterSection) && (!inSectionList(s, altOldList))) {
                if (beforeSection != null) {
                    if (forwardConnected(s, beforeSection, beforeSectionDirection)) {
                        if ((s.getUserName() != null) && (!s.getUserName().equals(""))) {
                            mayBeName = mayBeName + "( " + s.getUserName() + " )";
                        }
                        mayBeSection = s;
                        mayBeDirection = Section.FORWARD;
                    } else if (reverseConnected(s, beforeSection, beforeSectionDirection)) {
                        if ((s.getUserName() != null) && (!s.getUserName().equals(""))) {
                            mayBeName = mayBeName + "( " + s.getUserName() + " )";
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
                        if ((s.getUserName() != null) && (!s.getUserName().equals(""))) {
                            mayBeName = mayBeName + "( " + s.getUserName() + " )";
                        }
                        mayBeSection = s;
                        mayBeDirection = Section.REVERSE;
                    } else if (reverseConnected(s, afterSection, afterSectionDirection)) {
                        if ((s.getUserName() != null) && (!s.getUserName().equals(""))) {
                            mayBeName = mayBeName + "( " + s.getUserName() + " )";
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
            javax.swing.JOptionPane.showMessageDialog(addFrame,
                    java.text.MessageFormat.format(rbx.getString("Message36"),
                            new Object[]{"" + seq}), rbx.getString("ErrorTitle"),
                    javax.swing.JOptionPane.ERROR_MESSAGE);
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

    boolean inSectionList(Section s, ArrayList<Section> sList) {
        for (int i = 0; i < sList.size(); i++) {
            if (sList.get(i) == s) {
                return true;
            }
        }
        return false;
    }

    int getSeqNum() {
        int n = 0;
        try {
            n = Integer.parseInt(seqNum.getText());
        } catch (NumberFormatException ex) {
            log.error("Unable to convert " + seqNum.getText() + " to a number");
        }
        if ((n < 1) || (n > curSequenceNum)) {
            javax.swing.JOptionPane.showMessageDialog(null, rbx
                    .getString("Message34"), rbx.getString("ErrorTitle"),
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            return 0;
        }
        return n;
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
                    }
                    sectionList.remove(i);
                }
            }
            initializeSectionCombos();
        }
        sectionTableModel.fireTableDataChanged();
    }

    void addAlternateForSeqPressed(ActionEvent e) {
        if (sectionList.size() > maxSections) {
            javax.swing.JOptionPane.showMessageDialog(addFrame, rbx
                    .getString("Message23"), rbx.getString("ErrorTitle"),
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        int seq = getSeqNum();
        if (seq == 0) {
            return;
        }
        Section primarySection = null;
        ArrayList<Section> altOldList = new ArrayList<>();
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
            log.error("Missing primary Section for seq = " + seq);
            return;
        }
        ArrayList<Section> possibles = new ArrayList<>();
        int[] possiblesDirection = new int[150];
        ArrayList<String> possibleNames = new ArrayList<>();
        List<String> allSections = sectionManager.getSystemNameList();
        for (int i = 0; i < allSections.size(); i++) {
            Section mayBeSection = null;
            String mayBeName = allSections.get(i);
            int mayBeDirection = 0;
            Section s = sectionManager.getBySystemName(mayBeName);
            if ((s != null) && (s != primarySection) && (s != beforeSection)
                    && (s != afterSection) && (!inSectionList(s, altOldList))) {
                if (beforeSection != null) {
                    if (forwardConnected(s, beforeSection, beforeSectionDirection)) {
                        if ((s.getUserName() != null) && (!s.getUserName().equals(""))) {
                            mayBeName = mayBeName + "( " + s.getUserName() + " )";
                        }
                        mayBeSection = s;
                        mayBeDirection = Section.FORWARD;
                    } else if (reverseConnected(s, beforeSection, beforeSectionDirection)) {
                        if ((s.getUserName() != null) && (!s.getUserName().equals(""))) {
                            mayBeName = mayBeName + "( " + s.getUserName() + " )";
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
                        if ((s.getUserName() != null) && (!s.getUserName().equals(""))) {
                            mayBeName = mayBeName + "( " + s.getUserName() + " )";
                        }
                        mayBeSection = s;
                        mayBeDirection = Section.REVERSE;
                    } else if (reverseConnected(s, afterSection, afterSectionDirection)) {
                        if ((s.getUserName() != null) && (!s.getUserName().equals(""))) {
                            mayBeName = mayBeName + "( " + s.getUserName() + " )";
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
            javax.swing.JOptionPane.showMessageDialog(addFrame,
                    java.text.MessageFormat.format(rbx.getString("Message37"),
                            new Object[]{"" + seq}), rbx.getString("ErrorTitle"),
                    javax.swing.JOptionPane.ERROR_MESSAGE);
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
        }
        direction[index] = possiblesDirection[k];
        sequence[index] = sequence[index - 1];
        alternate[index] = true;
        action[index] = new ArrayList<>();
        initializeSectionCombos();

        sectionTableModel.fireTableDataChanged();
    }

    void addAlternateSectionPressed(ActionEvent e) {
        if (sectionList.size() > maxSections) {
            javax.swing.JOptionPane.showMessageDialog(addFrame, rbx
                    .getString("Message23"), rbx.getString("ErrorTitle"),
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (alternateSectionBoxList.size() == 0) {
            javax.swing.JOptionPane.showMessageDialog(addFrame, rbx
                    .getString("Message24"), rbx.getString("ErrorTitle"),
                    javax.swing.JOptionPane.ERROR_MESSAGE);
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
            initializeSectionCombos();
        }
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
            String sName = sysName.getText().toUpperCase();
            curTransit = transitManager.createNewTransit(sName, uName);
        }
        if (curTransit == null) {
            javax.swing.JOptionPane.showMessageDialog(addFrame, rbx
                    .getString("Message22"), rbx.getString("ErrorTitle"),
                    javax.swing.JOptionPane.ERROR_MESSAGE);
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
                javax.swing.JOptionPane.showMessageDialog(addFrame, rbx
                        .getString("Message22"), rbx.getString("ErrorTitle"),
                        javax.swing.JOptionPane.ERROR_MESSAGE);
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
            javax.swing.JOptionPane.showMessageDialog(addFrame, rbx
                    .getString("Message26"), rbx.getString("ErrorTitle"),
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            return false;
        }
// djd debugging - need to add code to check Transit Information
// add code here as needed
        return true;
    }

    private boolean setTransitInformation() {
        if (curTransit == null) {
            return false;
        }
        curTransit.removeAllSections();
        for (int i = 0; i < sectionList.size(); i++) {
            TransitSection ts = new TransitSection(sectionList.get(i),
                    sequence[i], direction[i], alternate[i]);
            if (ts.equals(null)) {
                log.error("Trouble creating TransitSection");
                return false;
            }
            ArrayList<TransitSectionAction> list = action[i];
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
        List<String> allSections = sectionManager.getSystemNameList();
        primarySectionBox.removeAllItems();
        alternateSectionBox.removeAllItems();
        insertAtBeginningBox.removeAllItems();
        primarySectionBoxList.clear();
        alternateSectionBoxList.clear();
        insertAtBeginningBoxList.clear();
        if (sectionList.size() == 0) {
            // no Sections currently in Transit - all Sections and all Directions OK
            for (int i = 0; i < allSections.size(); i++) {
                String sName = allSections.get(i);
                Section s = sectionManager.getBySystemName(sName);
                if (s != null) {
                    if ((s.getUserName() != null) && (!s.getUserName().equals(""))) {
                        sName = sName + "( " + s.getUserName() + " )";
                    }
                    primarySectionBox.addItem(sName);
                    primarySectionBoxList.add(s);
                    priSectionDirection[primarySectionBoxList.size() - 1] = Section.FORWARD;
                }
            }
        } else {
            // limit to Sections that connect to the current Section and are not the previous Section
            for (int i = 0; i < allSections.size(); i++) {
                String sName = allSections.get(i);
                Section s = sectionManager.getBySystemName(sName);
                if (s != null) {
                    if ((s != prevSection) && (forwardConnected(s, curSection, curSectionDirection))) {
                        if ((s.getUserName() != null) && (!s.getUserName().equals(""))) {
                            sName = sName + "( " + s.getUserName() + " )";
                        }
                        primarySectionBox.addItem(sName);
                        primarySectionBoxList.add(s);
                        priSectionDirection[primarySectionBoxList.size() - 1] = Section.FORWARD;
                    } else if ((s != prevSection) && (reverseConnected(s, curSection, curSectionDirection))) {
                        if ((s.getUserName() != null) && (!s.getUserName().equals(""))) {
                            sName = sName + "( " + s.getUserName() + " )";
                        }
                        primarySectionBox.addItem(sName);
                        primarySectionBoxList.add(s);
                        priSectionDirection[primarySectionBoxList.size() - 1] = Section.REVERSE;
                    }
                }
            }
            // check if there are any alternate Section choices
            if (prevSection != null) {
                for (int i = 0; i < allSections.size(); i++) {
                    String sName = allSections.get(i);
                    Section s = sectionManager.getBySystemName(sName);
                    if (s != null) {
                        if ((notIncludedWithSeq(s, curSequenceNum))
                                && forwardConnected(s, prevSection, prevSectionDirection)) {
                            if ((s.getUserName() != null) && (!s.getUserName().equals(""))) {
                                sName = sName + "( " + s.getUserName() + " )";
                            }
                            alternateSectionBox.addItem(sName);
                            alternateSectionBoxList.add(s);
                            altSectionDirection[alternateSectionBoxList.size() - 1] = Section.FORWARD;
                        } else if (notIncludedWithSeq(s, curSequenceNum)
                                && reverseConnected(s, prevSection, prevSectionDirection)) {
                            if ((s.getUserName() != null) && (!s.getUserName().equals(""))) {
                                sName = sName + "( " + s.getUserName() + " )";
                            }
                            alternateSectionBox.addItem(sName);
                            alternateSectionBoxList.add(s);
                            altSectionDirection[alternateSectionBoxList.size() - 1] = Section.REVERSE;
                        }
                    }
                }
            }
            // check if there are any Sections available to be inserted at beginning
            Section firstSection = sectionList.get(0);
            int testDirection = Section.FORWARD;
            if (direction[0] == Section.FORWARD) {
                testDirection = Section.REVERSE;
            }
            for (int i = 0; i < allSections.size(); i++) {
                String sName = allSections.get(i);
                Section s = sectionManager.getBySystemName(sName);
                if (s != null) {
                    if ((s != firstSection) && (forwardConnected(s, firstSection, testDirection))) {
                        if ((s.getUserName() != null) && (!s.getUserName().equals(""))) {
                            sName = sName + "( " + s.getUserName() + " )";
                        }
                        insertAtBeginningBox.addItem(sName);
                        insertAtBeginningBoxList.add(s);
                        insertAtBeginningDirection[insertAtBeginningBoxList.size() - 1] = Section.REVERSE;
                    } else if ((s != firstSection) && (reverseConnected(s, firstSection, testDirection))) {
                        if ((s.getUserName() != null) && (!s.getUserName().equals(""))) {
                            sName = sName + "( " + s.getUserName() + " )";
                        }
                        insertAtBeginningBox.addItem(sName);
                        insertAtBeginningBoxList.add(s);
                        insertAtBeginningDirection[insertAtBeginningBoxList.size() - 1] = Section.FORWARD;
                    }
                }
            }
        }
    }

    @SuppressWarnings("unused")
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
            List<EntryPoint> s2Entries = new ArrayList<>();
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
            List<EntryPoint> s2Entries = new ArrayList<>();
            if (restrictedDirection == Section.FORWARD) {
                s2Entries = (ArrayList<EntryPoint>) s2.getReverseEntryPointList();
            } else if (restrictedDirection == Section.REVERSE) {
                s2Entries = (ArrayList<EntryPoint>) s2.getForwardEntryPointList();
            } else {
                s2Entries = (ArrayList<EntryPoint>) s2.getEntryPointList();
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

    // variables for view actions window
    private int activeRow = 0;
    private SpecialActionTableModel actionTableModel = null;
    private JmriJFrame actionTableFrame = null;
    private JLabel fixedSectionLabel = new JLabel("X");

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
            JLabel sectionNameLabel = new JLabel(rbx
                    .getString("SectionName") + ": ");
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
            JButton testButton = new JButton(rbx.getString("ButtonDelete"));
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
            // add view action panel buttons
            JPanel but = new JPanel();
            but.setLayout(new BoxLayout(but, BoxLayout.Y_AXIS));
            JPanel panel4 = new JPanel();
            panel4.setLayout(new FlowLayout());
            JButton newActionButton = new JButton(rbx.getString("ButtonAddNewAction"));
            panel4.add(newActionButton);
            newActionButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    newActionPressed(e);
                }
            });
            newActionButton.setToolTipText(rbx.getString("NewActionButtonHint"));
            JButton doneButton = new JButton(rbx.getString("ButtonDone"));
            panel4.add(doneButton);
            doneButton.addActionListener(new ActionListener() {
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

    // variables for add/edit action window
    private boolean editActionMode = false;
    private JmriJFrame addEditActionFrame = null;
    private TransitSectionAction curTSA = null;
    private JComboBox<String> whenBox = new JComboBox<>();
    private JTextField whenDataField = new JTextField(7);
    private JTextField whenStringField = new JTextField(17);
    private JComboBox<String> whatBox = new JComboBox<>();
    private JTextField whatData1Field = new JTextField(7);
    private JTextField whatData2Field = new JTextField(7);
    private JTextField whatStringField = new JTextField(17);
    private JButton updateActionButton = null;
    private JButton createActionButton = null;
    private JButton cancelAddEditActionButton = null;
    private JComboBox<String> blockBox = new JComboBox<>();
    private ArrayList<Block> blockList = new ArrayList<>();
    private JRadioButton onButton = new JRadioButton(rbx.getString("On"));
    private JRadioButton offButton = new JRadioButton(rbx.getString("Off"));
    private JLabel doneSensorLabel = new JLabel(rbx.getString("DoneSensorLabel"));
    private JTextField doneSensorField = new JTextField(17);

    private void addEditActionWindow() {
        if (addEditActionFrame == null) {
            // set up add/edit action window
            addEditActionFrame = new JmriJFrame(rbx.getString("TitleAddEditAction"));
            addEditActionFrame.addHelpMenu(
                    "package.jmri.jmrit.beantable.TransitSectionAddEditAction", true);
            addEditActionFrame.setLocation(120, 80);
            Container contentPane = addEditActionFrame.getContentPane();
            contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
            JPanel panelx = new JPanel();
            panelx.setLayout(new BoxLayout(panelx, BoxLayout.Y_AXIS));
            JPanel panel1 = new JPanel();
            panel1.setLayout(new FlowLayout());
            panel1.add(new JLabel(rbx.getString("WhenText")));
            initializeWhenBox();
            panel1.add(whenBox);
            whenBox.setToolTipText(rbx.getString("WhenBoxTip"));
            whenBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setWhen(whenBox.getSelectedIndex() + 1);
                }
            });
            panel1.add(whenStringField);
            initializeBlockBox();
            panel1.add(blockBox);
            panelx.add(panel1);
            JPanel panel11 = new JPanel();
            panel11.setLayout(new FlowLayout());
            panel11.add(new JLabel("    " + rbx.getString("OptionalDelay") + ": "));
            panel11.add(whenDataField);
            whenDataField.setToolTipText(rbx.getString("HintDelayData"));
            panel11.add(new JLabel(rbx.getString("Milliseconds")));
            panelx.add(panel11);
            JPanel sp = new JPanel();
            sp.setLayout(new FlowLayout());
            sp.add(new JLabel("     "));
            panelx.add(sp);
            JPanel panel2 = new JPanel();
            panel2.setLayout(new FlowLayout());
            panel2.add(new JLabel(rbx.getString("WhatText")));
            initializeWhatBox();
            panel2.add(whatBox);
            whatBox.setToolTipText(rbx.getString("WhatBoxTip"));
            whatBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setWhat(whatBox.getSelectedIndex() + 1);
                }
            });
            panel2.add(whatStringField);
            panelx.add(panel2);
            JPanel panel21 = new JPanel();
            panel21.setLayout(new FlowLayout());
            panel21.add(whatData1Field);
            panel21.add(whatData2Field);
            ButtonGroup onOffGroup = new ButtonGroup();
            onOffGroup.add(onButton);
            onOffGroup.add(offButton);
            panel21.add(onButton);
            panel21.add(offButton);
            panel21.add(doneSensorLabel);
            panel21.add(doneSensorField);
            panelx.add(panel21);
            contentPane.add(panelx);
            contentPane.add(new JSeparator());
            // add buttons
            JPanel but = new JPanel();
            but.setLayout(new FlowLayout());
            createActionButton = new JButton(rbx.getString("CreateActionButton"));
            but.add(createActionButton);
            createActionButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    createActionPressed(e);
                }
            });
            createActionButton.setToolTipText(rbx.getString("CreateActionButtonHint"));
            updateActionButton = new JButton(rbx.getString("UpdateActionButton"));
            but.add(updateActionButton);
            updateActionButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    updateActionPressed(e);
                }
            });
            updateActionButton.setToolTipText(rbx.getString("UpdateActionButtonHint"));
            but.add(cancelAddEditActionButton = new JButton(Bundle.getMessage("ButtonCancel")));
            cancelAddEditActionButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    cancelAddEditActionPressed(e);
                }
            });
            cancelAddEditActionButton.setToolTipText(rbx.getString("CancelButtonHint"));
            contentPane.add(but);
        }
        if (editActionMode) {
            // initialize window for the action being edited
            updateActionButton.setVisible(true);
            createActionButton.setVisible(false);
            whenDataField.setText("" + curTSA.getDataWhen());
            whenStringField.setText(curTSA.getStringWhen());
            whatData1Field.setText("" + curTSA.getDataWhat1());
            whatData2Field.setText("" + curTSA.getDataWhat2());
            whatStringField.setText(curTSA.getStringWhat());
            onButton.setSelected(true);
            if (curTSA.getStringWhat().equals("Off")) {
                offButton.setSelected(true);
            }
            setWhen(curTSA.getWhenCode());
            setWhat(curTSA.getWhatCode());
            setBlockBox();
        } else {
            // initialize for add new action
            whenDataField.setText("");
            whenStringField.setText("");
            whatData1Field.setText("");
            whatData2Field.setText("");
            whatStringField.setText("");
            onButton.setSelected(true);
            setWhen(1);
            setWhat(1);
            updateActionButton.setVisible(false);
            createActionButton.setVisible(true);
            setBlockBox();
        }
        addEditActionFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (addEditActionFrame != null) {
                    addEditActionFrame.setVisible(false);
                }
            }
        });
        addEditActionFrame.pack();
        addEditActionFrame.setVisible(true);
    }

    private void setWhen(int code) {
        whenBox.setSelectedIndex(code - 1);
        whenStringField.setVisible(false);
        blockBox.setVisible(false);
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
                whenStringField.setVisible(true);
                whenStringField.setToolTipText(rbx.getString("HintSensorEntry"));
                break;
        }
        addEditActionFrame.pack();
        addEditActionFrame.setVisible(true);
    }

    private void setWhat(int code) {
        whatBox.setSelectedIndex(code - 1);
        whatStringField.setVisible(false);
        whatData1Field.setVisible(false);
        whatData2Field.setVisible(false);
        onButton.setVisible(false);
        offButton.setVisible(false);
        doneSensorLabel.setVisible(false);
        doneSensorField.setVisible(false);
        switch (code) {
            case TransitSectionAction.PAUSE:
                whatData1Field.setVisible(true);
                whatData1Field.setToolTipText(rbx.getString("HintPauseData"));
                break;
            case TransitSectionAction.SETMAXSPEED:
                whatData1Field.setVisible(true);
                whatData1Field.setToolTipText(rbx.getString("HintSetSpeedData1"));
                break;
            case TransitSectionAction.SETCURRENTSPEED:
                whatData1Field.setVisible(true);
                whatData1Field.setToolTipText(rbx.getString("HintSetSpeedData1"));
                break;
            case TransitSectionAction.RAMPTRAINSPEED:
                whatData1Field.setVisible(true);
                whatData1Field.setToolTipText(rbx.getString("HintSetSpeedData1"));
                break;
            case TransitSectionAction.TOMANUALMODE:
                doneSensorLabel.setVisible(true);
                doneSensorField.setVisible(true);
                doneSensorField.setToolTipText(rbx.getString("HintDoneSensor"));
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
                whatData1Field.setVisible(true);
                whatData1Field.setToolTipText(rbx.getString("HintSoundHornData1"));
                break;
            case TransitSectionAction.SOUNDHORNPATTERN:
                whatData1Field.setVisible(true);
                whatData1Field.setToolTipText(rbx.getString("HintSoundHornPatternData1"));
                whatData2Field.setVisible(true);
                whatData2Field.setToolTipText(rbx.getString("HintSoundHornPatternData2"));
                whatStringField.setVisible(true);
                whatStringField.setToolTipText(rbx.getString("HintSoundHornPatternString"));
                break;
            case TransitSectionAction.LOCOFUNCTION:
                whatData1Field.setVisible(true);
                whatData1Field.setToolTipText(rbx.getString("HintLocoFunctionData1"));
                onButton.setVisible(true);
                offButton.setVisible(true);
                onButton.setToolTipText(rbx.getString("HintLocoFunctionOnOff"));
                offButton.setToolTipText(rbx.getString("HintLocoFunctionOnOff"));
                break;
            case TransitSectionAction.SETSENSORACTIVE:
            case TransitSectionAction.SETSENSORINACTIVE:
                whatStringField.setVisible(true);
                whatStringField.setToolTipText(rbx.getString("HintSensorEntry"));
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

    // handle button presses in add/edit action window
    private void createActionPressed(ActionEvent e) {
        if ((!validateWhenData()) || (!validateWhatData())) {
            return;
        }
        // entered data is OK, create a special action
        curTSA = new TransitSectionAction(tWhen, tWhat, tWhenData, tWhatData1, tWhatData2, tWhenString, tWhatString);
        if (curTSA == null) {
            log.error("Failure when creating new TransitSectionAction");
        }
        ArrayList<TransitSectionAction> list = action[activeRow];
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
        addEditActionFrame.dispose();  // remove from Window menu
        addEditActionFrame = null;
    }

    private boolean validateWhenData() {
        tWhen = whenBox.getSelectedIndex() + 1;
        String s = whenDataField.getText();
        tWhenData = 0;
        if ((s != null) && (!s.equals(""))) {
            try {
                tWhenData = Integer.parseInt(s);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(addEditActionFrame, (rbx.getString("DelayError") + "\n" + e),
                        rbx.getString("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                log.error("Exception when parsing Field: " + e);
                return false;
            }
            if ((tWhenData < 0) || (tWhenData > 65500)) {
                JOptionPane.showMessageDialog(addEditActionFrame, (rbx.getString("DelayRangeError")),
                        rbx.getString("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        tWhenString = "";
        if ((tWhen == TransitSectionAction.SENSORACTIVE) || (tWhen == TransitSectionAction.SENSORINACTIVE)) {
            tWhenString = whenStringField.getText();
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
            // no sensor entered
            JOptionPane.showMessageDialog(addEditActionFrame, (rbx.getString("NoSensorError")),
                    rbx.getString("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        // get the sensor corresponding to this name
        Sensor s = InstanceManager.sensorManagerInstance().getSensor(sName);
        if (s == null) {
            // There is no sensor corresponding to this name
            JOptionPane.showMessageDialog(addEditActionFrame, (rbx.getString("SensorEntryError")),
                    rbx.getString("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (!sName.equals(s.getUserName())) {
            if (when) {
                tWhenString = sName.toUpperCase();
            } else {
                tWhatString = sName.toUpperCase();
            }
        }
        return true;
    }

    private boolean validateWhatData() {
        tWhat = whatBox.getSelectedIndex() + 1;
        tWhatData1 = 0;
        tWhatData2 = 0;
        tWhatString = "";
        switch (tWhat) {
            case TransitSectionAction.PAUSE:
                if (!readWhatData1(rbx.getString("PauseTime"), 1, 65500)) {
                    return false;
                }
                break;
            case TransitSectionAction.SETMAXSPEED:
            case TransitSectionAction.SETCURRENTSPEED:
                if (!readWhatData1(rbx.getString("SpeedPercentage"), 1, 99)) {
                    return false;
                }
                break;
            case TransitSectionAction.RAMPTRAINSPEED:
                if (!readWhatData1(rbx.getString("SpeedPercentage"), 1, 99)) {
                    return false;
                }
                break;
            case TransitSectionAction.TOMANUALMODE:
                tWhatString = doneSensorField.getText();
                if (tWhatString.length() >= 1) {
                    if (!validateSensor(tWhatString, false)) {
                        tWhatString = "";
                    }
                }
                break;
            case TransitSectionAction.SETLIGHT:
                tWhatString = "On";
                if (offButton.isSelected()) {
                    tWhatString = "Off";
                }
                break;
            case TransitSectionAction.STARTBELL:
            case TransitSectionAction.STOPBELL:
                break;
            case TransitSectionAction.SOUNDHORN:
                if (!readWhatData1(rbx.getString("HornBlastLength"), 100, 65500)) {
                    return false;
                }
                break;
            case TransitSectionAction.SOUNDHORNPATTERN:
                if (!readWhatData1(rbx.getString("ShortBlastLength"), 100, 65500)) {
                    return false;
                }
                if (!readWhatData2(rbx.getString("LongBlastLength"), 100, 65500)) {
                    return false;
                }
                tWhatString = whatStringField.getText();
                if ((tWhatString == null) || tWhatString == "" || (tWhatString.length() < 1)) {
                    JOptionPane.showMessageDialog(addEditActionFrame, (rbx.getString("MissingPattern")),
                            rbx.getString("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                tWhatString = tWhatString.toLowerCase();
                for (int i = 0; i < tWhatString.length(); i++) {
                    char c = tWhatString.charAt(i);
                    if ((c != 's') && (c != 'l')) {
                        JOptionPane.showMessageDialog(addEditActionFrame, (rbx.getString("ErrorPattern")),
                                rbx.getString("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                }
                whatStringField.setText(tWhatString);
                break;
            case TransitSectionAction.LOCOFUNCTION:
                if (!readWhatData1(rbx.getString("FunctionNumber"), 0, 28)) {
                    return false;
                }
                tWhatString = "On";
                if (offButton.isSelected()) {
                    tWhatString = "Off";
                }
                break;
            case TransitSectionAction.SETSENSORACTIVE:
            case TransitSectionAction.SETSENSORINACTIVE:
                tWhatString = whatStringField.getText();
                if (!validateSensor(tWhatString, false)) {
                    return false;
                }
                break;
        }
        return true;
    }

    private boolean readWhatData1(String err, int min, int max) {
        String s = whatData1Field.getText();
        if ((s == null) || (s.equals(""))) {
            JOptionPane.showMessageDialog(addEditActionFrame,
                    java.text.MessageFormat.format(rbx.getString("MissingEntryError"),
                            new Object[]{err}),
                    rbx.getString("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try {
            tWhatData1 = Integer.parseInt(s);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(addEditActionFrame,
                    java.text.MessageFormat.format(rbx.getString("EntryError") + e,
                            new Object[]{err}),
                    rbx.getString("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            log.error("Exception when parsing " + err + " Field: " + e);
            return false;
        }
        if ((tWhatData1 < min) || (tWhatData1 > max)) {
            JOptionPane.showMessageDialog(addEditActionFrame,
                    java.text.MessageFormat.format(rbx.getString("EntryRangeError"),
                            new Object[]{err, "" + min, "" + max}),
                    rbx.getString("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private boolean readWhatData2(String err, int min, int max) {
        String s = whatData2Field.getText();
        if ((s == null) || (s.equals(""))) {
            JOptionPane.showMessageDialog(addEditActionFrame,
                    java.text.MessageFormat.format(rbx.getString("MissingEntryError"),
                            new Object[]{err}),
                    rbx.getString("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try {
            tWhatData2 = Integer.parseInt(s);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(addEditActionFrame,
                    java.text.MessageFormat.format(rbx.getString("EntryError") + e,
                            new Object[]{err}),
                    rbx.getString("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            log.error("Exception when parsing " + err + " Field: " + e);
            return false;
        }
        if ((tWhatData2 < min) || (tWhatData2 > max)) {
            JOptionPane.showMessageDialog(addEditActionFrame,
                    java.text.MessageFormat.format(rbx.getString("EntryRangeError"),
                            new Object[]{err, "" + min, "" + max}),
                    rbx.getString("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            return false;
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
        }
        return "WHAT";
    }

    private void initializeBlockBox() {
        blockList = sectionList.get(activeRow).getBlockList();
        blockBox.removeAllItems();
        for (int i = 0; i < blockList.size(); i++) {
            String s = blockList.get(i).getSystemName();
            if ((blockList.get(i).getUserName() != null) && (!blockList.get(i).getUserName().equals(""))) {
                s = s + "(" + blockList.get(i).getUserName() + ")";
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
    /*
     * Notes: For the following, r = row in the Special Actions table.
     *        A TransitSectionAction must be available for this row.
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
        }
        return "WHEN";
    }
    /*
     * Notes: For the following, r = row in the Special Actions table.
     *        A TransitSectionAction must be available for this row.
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
                return java.text.MessageFormat.format(rbx.getString("SetLightFull"),
                        new Object[]{rbx.getString(tsa.getStringWhat())});
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
                return java.text.MessageFormat.format(rbx.getString("LocoFunctionFull"),
                        new Object[]{"" + tsa.getDataWhat1(), rbx.getString(tsa.getStringWhat())});
            case TransitSectionAction.SETSENSORACTIVE:
                return java.text.MessageFormat.format(rbx.getString("SetSensorActiveFull"),
                        new Object[]{tsa.getStringWhat()});
            case TransitSectionAction.SETSENSORINACTIVE:
                return java.text.MessageFormat.format(rbx.getString("SetSensorInactiveFull"),
                        new Object[]{tsa.getStringWhat()});
        }
        return "WHAT";
    }

    private String getSectionNameByRow(int r) {
        String s = sectionList.get(r).getSystemName();
        String u = sectionList.get(r).getUserName();
        if ((u != null) && (!u.equals(""))) {
            return (s + "( " + u + " )");
        }
        return s;
    }

    /**
     * Table model for Sections in Create/Edit Transit window
     */
    public class SectionTableModel extends javax.swing.table.AbstractTableModel implements
            java.beans.PropertyChangeListener {

        public static final int SEQUENCE_COLUMN = 0;
        public static final int SECTIONNAME_COLUMN = 1;
        public static final int ACTION_COLUMN = 2;
        public static final int SEC_DIRECTION_COLUMN = 3;
        public static final int ALTERNATE_COLUMN = 4;

        public SectionTableModel() {
            super();
            sectionManager.addPropertyChangeListener(this);
        }

        public void propertyChange(java.beans.PropertyChangeEvent e) {
            if (e.getPropertyName().equals("length")) {
                // a new NamedBean is available in the manager
                fireTableDataChanged();
            }
        }

        public Class<?> getColumnClass(int c) {
            if (c == ACTION_COLUMN) {
                return JButton.class;
            }
            return String.class;
        }

        public int getColumnCount() {
            return ALTERNATE_COLUMN + 1;
        }

        public int getRowCount() {
            return (sectionList.size());
        }

        public boolean isCellEditable(int r, int c) {
            if (c == ACTION_COLUMN) {
                return (true);
            }
            return (false);
        }

        public String getColumnName(int col) {
            switch (col) {
                case SEQUENCE_COLUMN:
                    return rbx.getString("SequenceColName");
                case SECTIONNAME_COLUMN:
                    return rbx.getString("SectionName");
                case ACTION_COLUMN:
                    return rbx.getString("ActionColName");
                case SEC_DIRECTION_COLUMN:
                    return rbx.getString("DirectionColName");
                case ALTERNATE_COLUMN:
                    return rbx.getString("AlternateColName");
                default:
                    return "";
            }
        }

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
            }
            return new JTextField(5).getPreferredSize().width;
        }

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
                    return rbx.getString("Unknown");
                case ALTERNATE_COLUMN:
                    if (alternate[rx]) {
                        return rbx.getString("Alternate");
                    }
                    return rbx.getString("Primary");
                default:
                    return rbx.getString("Unknown");
            }
        }

        public void setValueAt(Object value, int row, int col) {
            if (col == ACTION_COLUMN) {
                addEditActionsPressed(row);
            }
            return;
        }
    }

    /**
     * Table model for Actions in Special Actions window
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

        public void propertyChange(java.beans.PropertyChangeEvent e) {
            if (e.getPropertyName().equals("length")) {
                // a new NamedBean is available in the manager
                fireTableDataChanged();
            }
        }

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

        public int getColumnCount() {
            return REMOVE_COLUMN + 1;
        }

        public int getRowCount() {
            return (action[activeRow].size());
        }

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

        public String getColumnName(int col) {
            if (col == WHEN_COLUMN) {
                return rbx.getString("WhenColName");
            } else if (col == WHAT_COLUMN) {
                return rbx.getString("WhatColName");
            }
            return "";
        }

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
            }
            return new JTextField(8).getPreferredSize().width;
        }

        public Object getValueAt(int r, int c) {
            int rx = r;
            if (rx > sectionList.size()) {
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
                    return rbx.getString("Unknown");
            }
        }

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

    protected String getClassName() {
        return TransitTableAction.class.getName();
    }

    public String getClassDescription() {
        return Bundle.getMessage("TitleTransitTable");
    }

    private final static Logger log = LoggerFactory.getLogger(TransitTableAction.class.getName());
}
