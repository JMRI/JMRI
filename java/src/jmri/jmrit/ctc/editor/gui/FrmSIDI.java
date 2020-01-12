package jmri.jmrit.ctc.editor.gui;

import jmri.jmrit.ctc.editor.code.AwtWindowProperties;
import jmri.jmrit.ctc.editor.code.CheckJMRIObject;
import jmri.jmrit.ctc.editor.code.CodeButtonHandlerDataRoutines;
import jmri.jmrit.ctc.editor.code.CommonSubs;
import jmri.jmrit.ctc.editor.code.ProgramProperties;
import java.util.ArrayList;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import jmri.jmrit.ctc.ctcserialdata.CodeButtonHandlerData;
import jmri.jmrit.ctc.ctcserialdata.ProjectsCommonSubs;

/**
 *
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 */
public class FrmSIDI extends javax.swing.JFrame {

    /**
     * Creates new form dlgSIDI
     */
    private static final String FORM_PROPERTIES = "DlgSIDI";    // NOI18N
    private static final String PREFIX = "_mSIDI_";             // NOI18N
    private final AwtWindowProperties _mAwtWindowProperties;
    private boolean _mClosedNormally = false;
    public boolean closedNormally() { return _mClosedNormally; }
    private final CodeButtonHandlerData _mCodeButtonHandlerData;
    private final ProgramProperties _mProgramProperties;
    private final CheckJMRIObject _mCheckJMRIObject;
    private final boolean _mSignalHeadSelected;

    private String _mSIDI_LeftInternalSensorOrig;
    private String _mSIDI_NormalInternalSensorOrig;
    private String _mSIDI_RightInternalSensorOrig;
    private int _mSIDI_CodingAndResponseTimeOrig;
    private int _mSIDI_TimeLockingIntervalOrig;

    private ArrayList<String> _mLeftRightTrafficSignalsArrayListOrig = new ArrayList<>();
    private ArrayList<String> _mRightLeftTrafficSignalsArrayListOrig = new ArrayList<>();
    private void initOrig(ArrayList<String> signalArrayList1, ArrayList<String> signalArrayList2) {
        _mSIDI_LeftInternalSensorOrig = _mCodeButtonHandlerData._mSIDI_LeftInternalSensor;
        _mSIDI_NormalInternalSensorOrig = _mCodeButtonHandlerData._mSIDI_NormalInternalSensor;
        _mSIDI_RightInternalSensorOrig = _mCodeButtonHandlerData._mSIDI_RightInternalSensor;
        _mSIDI_CodingAndResponseTimeOrig = _mCodeButtonHandlerData._mSIDI_CodingTimeInMilliseconds;
        _mSIDI_TimeLockingIntervalOrig = _mCodeButtonHandlerData._mSIDI_TimeLockingTimeInMilliseconds;
        for (int index = 0; index < signalArrayList1.size(); index++) {
            _mLeftRightTrafficSignalsArrayListOrig.add(signalArrayList1.get(index));
        }
        for (int index = 0; index < signalArrayList2.size(); index++) {
            _mRightLeftTrafficSignalsArrayListOrig.add(signalArrayList2.get(index));
        }
    }

    private boolean dataChanged() {
        if (!_mSIDI_LeftInternalSensorOrig.equals(_mSIDI_LeftInternalSensor.getText())) return true;
        if (!_mSIDI_NormalInternalSensorOrig.equals(_mSIDI_NormalInternalSensor.getText())) return true;
        if (!_mSIDI_RightInternalSensorOrig.equals(_mSIDI_RightInternalSensor.getText())) return true;
        if (CommonSubs.getIntFromJTextFieldNoThrow(_mSIDI_CodingAndResponseTime) != _mSIDI_CodingAndResponseTimeOrig) return true;
        if (CommonSubs.getIntFromJTextFieldNoThrow(_mSIDI_TimeLockingInterval) != _mSIDI_TimeLockingIntervalOrig) return true;
        int tableLength = CommonSubs.compactDefaultTableModel(_mSIDI_TableOfLeftToRightTrafficExternalSignalNamesDefaultTableModel);
        if (tableLength != _mLeftRightTrafficSignalsArrayListOrig.size()) return true;
        for (int index = 0; index < tableLength; index++) {
            if (!_mLeftRightTrafficSignalsArrayListOrig.get(index).equals(_mSIDI_TableOfLeftToRightTrafficExternalSignalNamesDefaultTableModel.getValueAt(index, 0))) return true;
        }
        tableLength = CommonSubs.compactDefaultTableModel(_mSIDI_TableOfRightToLeftTrafficExternalSignalNamesDefaultTableModel);
        if (tableLength != _mRightLeftTrafficSignalsArrayListOrig.size()) return true;
        for (int index = 0; index < tableLength; index++) {
            if (!_mRightLeftTrafficSignalsArrayListOrig.get(index).equals(_mSIDI_TableOfRightToLeftTrafficExternalSignalNamesDefaultTableModel.getValueAt(index, 0))) return true;
        }
        return false;
    }

    private final DefaultTableModel _mSIDI_TableOfLeftToRightTrafficExternalSignalNamesDefaultTableModel;
    private final DefaultTableModel _mSIDI_TableOfRightToLeftTrafficExternalSignalNamesDefaultTableModel;

    public FrmSIDI( AwtWindowProperties awtWindowProperties, CodeButtonHandlerData codeButtonHandlerData,
                    ProgramProperties programProperties, CheckJMRIObject checkJMRIObject,
                    boolean signalHeadSelected) {
        super();
        initComponents();
        CommonSubs.addHelpMenu(this, "package.jmri.jmrit.ctc.CTC_frmSIDI", true);  // NOI18N
        _mAwtWindowProperties = awtWindowProperties;
        _mCodeButtonHandlerData = codeButtonHandlerData;
        _mProgramProperties = programProperties;
        _mCheckJMRIObject = checkJMRIObject;
        _mSignalHeadSelected = signalHeadSelected;
        CommonSubs.setMillisecondsEdit(_mSIDI_CodingAndResponseTime);
        CommonSubs.setMillisecondsEdit(_mSIDI_TimeLockingInterval);
        _mSIDI_LeftInternalSensor.setText(_mCodeButtonHandlerData._mSIDI_LeftInternalSensor);
        _mSIDI_NormalInternalSensor.setText(_mCodeButtonHandlerData._mSIDI_NormalInternalSensor);
        _mSIDI_RightInternalSensor.setText(_mCodeButtonHandlerData._mSIDI_RightInternalSensor);
        _mSIDI_CodingAndResponseTime.setText(Integer.toString(_mCodeButtonHandlerData._mSIDI_CodingTimeInMilliseconds));
        _mSIDI_TimeLockingInterval.setText(Integer.toString(_mCodeButtonHandlerData._mSIDI_TimeLockingTimeInMilliseconds));
        _mSIDI_TableOfLeftToRightTrafficExternalSignalNamesDefaultTableModel = (DefaultTableModel)_mSIDI_TableOfLeftToRightTrafficExternalSignalNames.getModel();
        ArrayList<String> signalsArrayList1 = ProjectsCommonSubs.getArrayListFromCSV(_mCodeButtonHandlerData._mSIDI_LeftRightTrafficSignalsCSVList);
        int signalsArrayListSize1 = signalsArrayList1.size();
        if (signalsArrayListSize1 > _mSIDI_TableOfLeftToRightTrafficExternalSignalNames.getRowCount()) { // Has more than default (100 as of this writing) rows:
            _mSIDI_TableOfLeftToRightTrafficExternalSignalNamesDefaultTableModel.setRowCount(signalsArrayListSize1);
        }
        for (int index = 0; index < signalsArrayListSize1; index++) {
            _mSIDI_TableOfLeftToRightTrafficExternalSignalNamesDefaultTableModel.setValueAt(signalsArrayList1.get(index), index, 0);
        }
        _mSIDI_TableOfRightToLeftTrafficExternalSignalNamesDefaultTableModel = (DefaultTableModel)_mSIDI_TableOfRightToLeftTrafficExternalSignalNames.getModel();
        ArrayList<String> signalsArrayList2 = ProjectsCommonSubs.getArrayListFromCSV(_mCodeButtonHandlerData._mSIDI_RightLeftTrafficSignalsCSVList);
        int signalsArrayListSize2 = signalsArrayList2.size();
        if (signalsArrayListSize2 > _mSIDI_TableOfRightToLeftTrafficExternalSignalNames.getRowCount()) { // Has more than default (100 as of this writing) rows:
            _mSIDI_TableOfRightToLeftTrafficExternalSignalNamesDefaultTableModel.setRowCount(signalsArrayListSize2);
        }
        for (int index = 0; index < signalsArrayListSize2; index++) {
            _mSIDI_TableOfRightToLeftTrafficExternalSignalNamesDefaultTableModel.setValueAt(signalsArrayList2.get(index), index, 0);
        }

//  This is TYPICAL of the poor quality of Java coding by supposed advanced programmers.
//  I searched the entire Oracle Web sites that publishes documentation on Java, and NOWHERE
//  is this mentioned.  HOW IN THE HELL is anyone supposed to find out about this?
//  And WHY would the default be the other way?  Why don't they just admit they are poor programmers!
//  Where is a list of properties available and their corresponding functions?
        _mSIDI_TableOfLeftToRightTrafficExternalSignalNames.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);    // NOI18N
        _mSIDI_TableOfRightToLeftTrafficExternalSignalNames.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);    // NOI18N
        initOrig(signalsArrayList1, signalsArrayList2);
        _mAwtWindowProperties.setWindowState(this, FORM_PROPERTIES);
        this.getRootPane().setDefaultButton(_mSaveAndClose);

        enableSignalListComboBox(_mSIDI_TableOfLeftToRightTrafficExternalSignalNames);
        enableSignalListComboBox(_mSIDI_TableOfRightToLeftTrafficExternalSignalNames);
    }

    public static boolean dialogCodeButtonHandlerDataValid(CheckJMRIObject checkJMRIObject, CodeButtonHandlerData codeButtonHandlerData) {
        if (!codeButtonHandlerData._mSIDI_Enabled) return true; // Not enabled, can be no error!
//  For interrelationship(s) checks:
        boolean leftInternalSensorPresent = !ProjectsCommonSubs.isNullOrEmptyString(codeButtonHandlerData._mSIDI_LeftInternalSensor);
        boolean entriesInLeftRightTrafficSignalsCSVList = !codeButtonHandlerData._mSIDI_LeftRightTrafficSignalsCSVList.isEmpty();
        boolean rightInternalSensorPresent = !ProjectsCommonSubs.isNullOrEmptyString(codeButtonHandlerData._mSIDI_RightInternalSensor);
        boolean entriesInRightLeftTrafficSignalsCSVList = !codeButtonHandlerData._mSIDI_RightLeftTrafficSignalsCSVList.isEmpty();
//  Checks:
        if (ProjectsCommonSubs.isNullOrEmptyString(codeButtonHandlerData._mSIDI_NormalInternalSensor)) return false;
        if (!leftInternalSensorPresent && !rightInternalSensorPresent) return false;
        if (leftInternalSensorPresent && !entriesInRightLeftTrafficSignalsCSVList) return false;
        if (rightInternalSensorPresent && !entriesInLeftRightTrafficSignalsCSVList) return false;
        if (!leftInternalSensorPresent && entriesInRightLeftTrafficSignalsCSVList) return false;
        if (!rightInternalSensorPresent && entriesInLeftRightTrafficSignalsCSVList) return false;
        for (String signalName : ProjectsCommonSubs.getArrayListFromCSV(codeButtonHandlerData._mSIDI_LeftRightTrafficSignalsCSVList)) {
            if (checkJMRIObject.checkSignal(signalName) == false) return false;
        }
        for (String signalName : ProjectsCommonSubs.getArrayListFromCSV(codeButtonHandlerData._mSIDI_RightLeftTrafficSignalsCSVList)) {
            if (checkJMRIObject.checkSignal(signalName) == false) return false;
        }
        return checkJMRIObject.validClassWithPrefix(PREFIX, codeButtonHandlerData);
    }

//  Validate all internal fields as much as possible:
    private ArrayList<String> formFieldsValid() {
        ArrayList<String> errors = new ArrayList<>();
//  For interrelationship(s) checks:
        boolean leftInternalSensorPresent = CommonSubs.isJTextFieldNotEmpty(_mSIDI_LeftInternalSensor);
        boolean entriesInLeftRightTrafficSignalsCSVList = !CommonSubs.getCSVStringFromDefaultTableModel(_mSIDI_TableOfLeftToRightTrafficExternalSignalNamesDefaultTableModel).isEmpty();
        boolean rightInternalSensorPresent = CommonSubs.isJTextFieldNotEmpty(_mSIDI_RightInternalSensor);
        boolean entriesInRightLeftTrafficSignalsCSVList = !CommonSubs.getCSVStringFromDefaultTableModel(_mSIDI_TableOfRightToLeftTrafficExternalSignalNamesDefaultTableModel).isEmpty();
//  Checks:
        CommonSubs.checkJTextFieldNotEmpty(_mSIDI_NormalInternalSensor, _mSIDI_NormalInternalSensorPrompt, errors);
// <<<<<<< Updated upstream
        if (!leftInternalSensorPresent && !rightInternalSensorPresent) errors.add(Bundle.getMessage("OneOrBothOf") + " \"" + _mSIDI_LeftInternalSensorPrompt.getText() + "\" " + Bundle.getMessage("And") + " \"" + _mSIDI_RightInternalSensorPrompt.getText() + "\" " + Bundle.getMessage("MustBePresent"));   // NOI18N
        if (leftInternalSensorPresent && !entriesInRightLeftTrafficSignalsCSVList) errors.add("\"" + _mSIDI_LeftInternalSensorPrompt.getText() + "\" " + Bundle.getMessage("ErrorDlgSIDIDefineButNoEntriesIn") + " \"" + _mTableOfRightToLeftTrafficSignalNamesPrompt.getText() + "\"");    // NOI18N
        if (rightInternalSensorPresent && !entriesInLeftRightTrafficSignalsCSVList) errors.add("\"" + _mSIDI_RightInternalSensorPrompt.getText() + "\" " + Bundle.getMessage("ErrorDlgSIDIDefineButNoEntriesIn") + " \"" + _mTableOfLeftToRightTrafficSignalNamesPrompt.getText() + "\"");  // NOI18N
        if (!leftInternalSensorPresent && entriesInRightLeftTrafficSignalsCSVList) errors.add("\"" + _mSIDI_LeftInternalSensorPrompt.getText() + "\" " + Bundle.getMessage("ErrorDlgSIDINotDefinedWithEntriesIn") + " \"" + _mTableOfRightToLeftTrafficSignalNamesPrompt.getText() + "\""); // NOI18N
        if (!rightInternalSensorPresent && entriesInLeftRightTrafficSignalsCSVList) errors.add("\"" + _mSIDI_RightInternalSensorPrompt.getText() + "\" " + Bundle.getMessage("ErrorDlgSIDINotDefinedWithEntriesIn") + " \"" + _mTableOfLeftToRightTrafficSignalNamesPrompt.getText() + "\"");   // NOI18N
        _mCheckJMRIObject.analyzeForm(PREFIX, this, errors);
// =======
//         if (!leftInternalSensorPresent && !rightInternalSensorPresent) errors.add("One or both of \"" + _mSIDI_LeftInternalSensorPrompt.getText() + "\" and \"" + _mSIDI_RightInternalSensorPrompt.getText() + "\" must be present.");
//         if (leftInternalSensorPresent && !entriesInRightLeftTrafficSignalsCSVList) errors.add("\"" + _mSIDI_LeftInternalSensorPrompt.getText() + "\" defined but no entries in \"" + _mTableOfRightToLeftTrafficSignalNamesPrompt.getText() + "\"");
//         if (rightInternalSensorPresent && !entriesInLeftRightTrafficSignalsCSVList) errors.add("\"" + _mSIDI_RightInternalSensorPrompt.getText() + "\" defined but no entries in \"" + _mTableOfLeftToRightTrafficSignalNamesPrompt.getText() + "\"");
//         if (!leftInternalSensorPresent && entriesInRightLeftTrafficSignalsCSVList) errors.add("\"" + _mSIDI_LeftInternalSensorPrompt.getText() + "\" not defined with entries in \"" + _mTableOfRightToLeftTrafficSignalNamesPrompt.getText() + "\"");
//         if (!rightInternalSensorPresent && entriesInLeftRightTrafficSignalsCSVList) errors.add("\"" + _mSIDI_RightInternalSensorPrompt.getText() + "\" not defined with entries in \"" + _mTableOfLeftToRightTrafficSignalNamesPrompt.getText() + "\"");
//         _mCheckJMRIObject.analyzeForm(PREFIX, this, errors);
// >>>>>>> Stashed changes
        return errors;
    }

    /**
     * Add a signal head/mast combo box as the default cell editor.
     * @param table The signal table to be modified.
     */
    public void enableSignalListComboBox(JTable table) {
        // Create the signals combo box
        JComboBox<String> comboBox = new JComboBox<>();
        if (_mSignalHeadSelected) {
            CommonSubs.populateJComboBoxWithBeans(comboBox, "SignalHead", null, true);
        } else {
            CommonSubs.populateJComboBoxWithBeans(comboBox, "SignalMast", null, true);
        }

        // Update the signal list cell editor
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setCellEditor(new javax.swing.DefaultCellEditor(comboBox));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")  // NOI18N
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        _mSaveAndClose = new javax.swing.JButton();
        _mSIDI_LeftInternalSensorPrompt = new javax.swing.JLabel();
        _mSIDI_LeftInternalSensor = new javax.swing.JTextField();
        _mSIDI_NormalInternalSensorPrompt = new javax.swing.JLabel();
        _mSIDI_NormalInternalSensor = new javax.swing.JTextField();
        _mSIDI_RightInternalSensor = new javax.swing.JTextField();
        _mSIDI_RightInternalSensorPrompt = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        _mSIDI_CodingAndResponseTime = new javax.swing.JFormattedTextField();
        _mSIDI_TimeLockingInterval = new javax.swing.JFormattedTextField();
        jLabel22 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        _mSIDI_TableOfLeftToRightTrafficExternalSignalNames = new javax.swing.JTable();
        _mTableOfLeftToRightTrafficSignalNamesPrompt = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        _mSIDI_TableOfRightToLeftTrafficExternalSignalNames = new javax.swing.JTable();
        _mTableOfRightToLeftTrafficSignalNamesPrompt = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle(Bundle.getMessage("TitleSIDI"));
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        _mSaveAndClose.setText(Bundle.getMessage("ButtonSaveClose"));
        _mSaveAndClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mSaveAndCloseActionPerformed(evt);
            }
        });

        _mSIDI_LeftInternalSensorPrompt.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        _mSIDI_LeftInternalSensorPrompt.setText(Bundle.getMessage("LabelSIDILeft"));

        _mSIDI_NormalInternalSensorPrompt.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        _mSIDI_NormalInternalSensorPrompt.setText(Bundle.getMessage("LabelSIDINormal"));

        _mSIDI_RightInternalSensorPrompt.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        _mSIDI_RightInternalSensorPrompt.setText(Bundle.getMessage("LabelSIDIRight"));

        jLabel19.setText(Bundle.getMessage("LabelSIDICodeTime"));

        _mSIDI_CodingAndResponseTime.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        _mSIDI_TimeLockingInterval.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        jLabel22.setText(Bundle.getMessage("LabelSIDILockTime"));

        _mSIDI_TableOfLeftToRightTrafficExternalSignalNames.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null}
            },
            new String [] {
                ""
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        _mSIDI_TableOfLeftToRightTrafficExternalSignalNames.setRowHeight(18);
        jScrollPane1.setViewportView(_mSIDI_TableOfLeftToRightTrafficExternalSignalNames);

        _mTableOfLeftToRightTrafficSignalNamesPrompt.setText(Bundle.getMessage("LabelSIDILRTraffic"));

        _mSIDI_TableOfRightToLeftTrafficExternalSignalNames.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null}
            },
            new String [] {
                ""
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        _mSIDI_TableOfRightToLeftTrafficExternalSignalNames.setRowHeight(18);
        jScrollPane2.setViewportView(_mSIDI_TableOfRightToLeftTrafficExternalSignalNames);

        _mTableOfRightToLeftTrafficSignalNamesPrompt.setText(Bundle.getMessage("LabelSIDIRLTraffic"));

        jButton1.setText(Bundle.getMessage("ButtonSIDIBoth"));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel8.setText(Bundle.getMessage("InfoSIDIRemove"));

        jLabel9.setText(Bundle.getMessage("InfoSIDICompact"));

        jButton2.setText(Bundle.getMessage("ButtonReapply"));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(_mSIDI_LeftInternalSensorPrompt, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(_mSIDI_RightInternalSensorPrompt, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel19)
                                    .addComponent(_mSIDI_NormalInternalSensorPrompt, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel22))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(_mSIDI_CodingAndResponseTime, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(_mSIDI_LeftInternalSensor, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(_mSIDI_NormalInternalSensor, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(_mSIDI_TimeLockingInterval, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(_mSIDI_RightInternalSensor, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(_mSaveAndClose)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel9)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(_mTableOfLeftToRightTrafficSignalNamesPrompt)
                                .addGap(82, 82, 82)
                                .addComponent(_mTableOfRightToLeftTrafficSignalNamesPrompt)
                                .addGap(117, 117, 117))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton1)
                        .addGap(215, 215, 215))))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel8)
                .addGap(109, 109, 109))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mSIDI_LeftInternalSensorPrompt)
                    .addComponent(_mSIDI_LeftInternalSensor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(_mTableOfLeftToRightTrafficSignalNamesPrompt)
                    .addComponent(_mTableOfRightToLeftTrafficSignalNamesPrompt))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 363, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 363, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(_mSIDI_NormalInternalSensor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(_mSIDI_NormalInternalSensorPrompt))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(_mSIDI_RightInternalSensor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(_mSIDI_RightInternalSensorPrompt))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(_mSIDI_CodingAndResponseTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel19))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(_mSIDI_TimeLockingInterval, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel22))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(_mSaveAndClose)
                            .addComponent(jLabel9))
                        .addGap(95, 95, 95)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void _mSaveAndCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mSaveAndCloseActionPerformed
        if (CommonSubs.missingFieldsErrorDialogDisplayed(this, formFieldsValid(), false)) {
            return; // Do not allow exit or transfer of data.
        }
        _mCodeButtonHandlerData._mSIDI_LeftInternalSensor = _mSIDI_LeftInternalSensor.getText();
        _mCodeButtonHandlerData._mSIDI_NormalInternalSensor = _mSIDI_NormalInternalSensor.getText();
        _mCodeButtonHandlerData._mSIDI_RightInternalSensor = _mSIDI_RightInternalSensor.getText();
        _mCodeButtonHandlerData._mSIDI_CodingTimeInMilliseconds = CommonSubs.getIntFromJTextFieldNoThrow(_mSIDI_CodingAndResponseTime);
        _mCodeButtonHandlerData._mSIDI_TimeLockingTimeInMilliseconds = CommonSubs.getIntFromJTextFieldNoThrow(_mSIDI_TimeLockingInterval);
        _mCodeButtonHandlerData._mSIDI_LeftRightTrafficSignalsCSVList = CommonSubs.getCSVStringFromDefaultTableModel(_mSIDI_TableOfLeftToRightTrafficExternalSignalNamesDefaultTableModel);
        _mCodeButtonHandlerData._mSIDI_RightLeftTrafficSignalsCSVList = CommonSubs.getCSVStringFromDefaultTableModel(_mSIDI_TableOfRightToLeftTrafficExternalSignalNamesDefaultTableModel);
        _mClosedNormally = true;
        _mAwtWindowProperties.saveWindowState(this, FORM_PROPERTIES);
        dispose();
    }//GEN-LAST:event__mSaveAndCloseActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        _mAwtWindowProperties.saveWindowState(this, FORM_PROPERTIES);
        if (CommonSubs.allowClose(this, dataChanged())) dispose();
    }//GEN-LAST:event_formWindowClosing

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        CommonSubs.compactDefaultTableModel(_mSIDI_TableOfLeftToRightTrafficExternalSignalNamesDefaultTableModel);
        CommonSubs.compactDefaultTableModel(_mSIDI_TableOfRightToLeftTrafficExternalSignalNamesDefaultTableModel);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        CodeButtonHandlerData temp = _mCodeButtonHandlerData.deepCopy();
        temp = CodeButtonHandlerDataRoutines.uECBHDWSD_SIDI(_mProgramProperties, temp);
        _mSIDI_LeftInternalSensor.setText(temp._mSIDI_LeftInternalSensor);
        _mSIDI_NormalInternalSensor.setText(temp._mSIDI_NormalInternalSensor);
        _mSIDI_RightInternalSensor.setText(temp._mSIDI_RightInternalSensor);
    }//GEN-LAST:event_jButton2ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JFormattedTextField _mSIDI_CodingAndResponseTime;
    private javax.swing.JTextField _mSIDI_LeftInternalSensor;
    private javax.swing.JLabel _mSIDI_LeftInternalSensorPrompt;
    private javax.swing.JTextField _mSIDI_NormalInternalSensor;
    private javax.swing.JLabel _mSIDI_NormalInternalSensorPrompt;
    private javax.swing.JTextField _mSIDI_RightInternalSensor;
    private javax.swing.JLabel _mSIDI_RightInternalSensorPrompt;
    private javax.swing.JTable _mSIDI_TableOfLeftToRightTrafficExternalSignalNames;
    private javax.swing.JTable _mSIDI_TableOfRightToLeftTrafficExternalSignalNames;
    private javax.swing.JFormattedTextField _mSIDI_TimeLockingInterval;
    private javax.swing.JButton _mSaveAndClose;
    private javax.swing.JLabel _mTableOfLeftToRightTrafficSignalNamesPrompt;
    private javax.swing.JLabel _mTableOfRightToLeftTrafficSignalNamesPrompt;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    // End of variables declaration//GEN-END:variables
}
