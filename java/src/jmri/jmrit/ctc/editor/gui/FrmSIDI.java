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
import jmri.jmrit.ctc.NBHSensor;
import jmri.jmrit.ctc.NBHSignal;
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
    private CodeButtonHandlerData.TRAFFIC_DIRECTION _mSIDI_TrafficDirectionTemp;

    private int _mSIDI_CodingAndResponseTimeOrig;
    private int _mSIDI_TimeLockingIntervalOrig;
    private CodeButtonHandlerData.TRAFFIC_DIRECTION _mSIDI_TrafficDirectionOrig;

    private ArrayList<String> _mLeftRightTrafficSignalsArrayListOrig = new ArrayList<>();
    private ArrayList<String> _mRightLeftTrafficSignalsArrayListOrig = new ArrayList<>();

    private void initOrig(ArrayList<String> signalArrayList1, ArrayList<String> signalArrayList2) {
        _mSIDI_CodingAndResponseTimeOrig = _mCodeButtonHandlerData._mSIDI_CodingTimeInMilliseconds;
        _mSIDI_TimeLockingIntervalOrig = _mCodeButtonHandlerData._mSIDI_TimeLockingTimeInMilliseconds;
        _mSIDI_TrafficDirectionOrig = _mCodeButtonHandlerData._mSIDI_TrafficDirection;
        for (int index = 0; index < signalArrayList1.size(); index++) {
            _mLeftRightTrafficSignalsArrayListOrig.add(signalArrayList1.get(index));
        }
        for (int index = 0; index < signalArrayList2.size(); index++) {
            _mRightLeftTrafficSignalsArrayListOrig.add(signalArrayList2.get(index));
        }
    }

    private boolean dataChanged() {
        if (CommonSubs.getIntFromJTextFieldNoThrow(_mSIDI_CodingAndResponseTime) != _mSIDI_CodingAndResponseTimeOrig) return true;
        if (CommonSubs.getIntFromJTextFieldNoThrow(_mSIDI_TimeLockingInterval) != _mSIDI_TimeLockingIntervalOrig) return true;
        if (_mSIDI_TrafficDirectionOrig != _mSIDI_TrafficDirectionTemp) return true;

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
        CommonSubs.populateJComboBoxWithBeans(_mSIDI_LeftInternalSensor, "Sensor", _mCodeButtonHandlerData._mSIDI_LeftInternalSensor.getHandleName(), false);   // NOI18N
        CommonSubs.populateJComboBoxWithBeans(_mSIDI_NormalInternalSensor, "Sensor", _mCodeButtonHandlerData._mSIDI_NormalInternalSensor.getHandleName(), false);   // NOI18N
        CommonSubs.populateJComboBoxWithBeans(_mSIDI_RightInternalSensor, "Sensor", _mCodeButtonHandlerData._mSIDI_RightInternalSensor.getHandleName(), false);   // NOI18N
        _mSIDI_CodingAndResponseTime.setText(Integer.toString(_mCodeButtonHandlerData._mSIDI_CodingTimeInMilliseconds));
        _mSIDI_TimeLockingInterval.setText(Integer.toString(_mCodeButtonHandlerData._mSIDI_TimeLockingTimeInMilliseconds));
        _mSIDI_TableOfLeftToRightTrafficExternalSignalNamesDefaultTableModel = (DefaultTableModel)_mSIDI_TableOfLeftToRightTrafficExternalSignalNames.getModel();
        ArrayList<String> signalsArrayList1 = ProjectsCommonSubs.getArrayListOfSignalNames(_mCodeButtonHandlerData._mSIDI_LeftRightTrafficSignals);
        int signalsArrayListSize1 = signalsArrayList1.size();
        if (signalsArrayListSize1 > _mSIDI_TableOfLeftToRightTrafficExternalSignalNames.getRowCount()) { // Has more than default (100 as of this writing) rows:
            _mSIDI_TableOfLeftToRightTrafficExternalSignalNamesDefaultTableModel.setRowCount(signalsArrayListSize1);
        }
        for (int index = 0; index < signalsArrayListSize1; index++) {
            _mSIDI_TableOfLeftToRightTrafficExternalSignalNamesDefaultTableModel.setValueAt(signalsArrayList1.get(index), index, 0);
        }
        _mSIDI_TableOfRightToLeftTrafficExternalSignalNamesDefaultTableModel = (DefaultTableModel)_mSIDI_TableOfRightToLeftTrafficExternalSignalNames.getModel();
        ArrayList<String> signalsArrayList2 = ProjectsCommonSubs.getArrayListOfSignalNames(_mCodeButtonHandlerData._mSIDI_RightLeftTrafficSignals);
        int signalsArrayListSize2 = signalsArrayList2.size();
        if (signalsArrayListSize2 > _mSIDI_TableOfRightToLeftTrafficExternalSignalNames.getRowCount()) { // Has more than default (100 as of this writing) rows:
            _mSIDI_TableOfRightToLeftTrafficExternalSignalNamesDefaultTableModel.setRowCount(signalsArrayListSize2);
        }
        for (int index = 0; index < signalsArrayListSize2; index++) {
            _mSIDI_TableOfRightToLeftTrafficExternalSignalNamesDefaultTableModel.setValueAt(signalsArrayList2.get(index), index, 0);
        }
        _mSIDI_TrafficDirectionTemp =_mCodeButtonHandlerData._mSIDI_TrafficDirection;
        switch (_mSIDI_TrafficDirectionTemp) {
            default:
            case BOTH:
                _mSIDI_BothTrafficButton.setSelected(true);
                break;
            case LEFT:
                _mSIDI_LeftTrafficButton.setSelected(true);
                break;
            case RIGHT:
                _mSIDI_RightTrafficButton.setSelected(true);
                break;
        }

        initOrig(signalsArrayList1, signalsArrayList2);
        _mAwtWindowProperties.setWindowState(this, FORM_PROPERTIES);
        this.getRootPane().setDefaultButton(_mSaveAndClose);

        enableSignalListComboBox(_mSIDI_TableOfLeftToRightTrafficExternalSignalNames);
        enableSignalListComboBox(_mSIDI_TableOfRightToLeftTrafficExternalSignalNames);
    }

    public static boolean dialogCodeButtonHandlerDataValid(CheckJMRIObject checkJMRIObject, CodeButtonHandlerData codeButtonHandlerData) {
        if (!codeButtonHandlerData._mSIDI_Enabled) return true; // Not enabled, can be no error!
//  For interrelationship(s) checks:
        boolean leftTrafficDirection = codeButtonHandlerData._mSIDI_TrafficDirection != CodeButtonHandlerData.TRAFFIC_DIRECTION.RIGHT;
        boolean rightTrafficDirection = codeButtonHandlerData._mSIDI_TrafficDirection != CodeButtonHandlerData.TRAFFIC_DIRECTION.LEFT;

        boolean entriesInLeftRightTrafficSignals = codeButtonHandlerData._mSIDI_LeftRightTrafficSignals.size() != 0;
        boolean entriesInRightLeftTrafficSignals = codeButtonHandlerData._mSIDI_RightLeftTrafficSignals.size() != 0;

//  Checks:
        if (ProjectsCommonSubs.isNullOrEmptyString(codeButtonHandlerData._mSIDI_NormalInternalSensor.getHandleName())) return false;
        if (leftTrafficDirection && !entriesInRightLeftTrafficSignals) return false;
        if (rightTrafficDirection && !entriesInLeftRightTrafficSignals) return false;
        if (!leftTrafficDirection && entriesInRightLeftTrafficSignals) return false;
        if (!rightTrafficDirection && entriesInLeftRightTrafficSignals) return false;
        for (String signalName : ProjectsCommonSubs.getArrayListOfSignalNames(codeButtonHandlerData._mSIDI_LeftRightTrafficSignals)) {
            if (checkJMRIObject.checkSignal(signalName) == false) return false;
        }
        for (String signalName : ProjectsCommonSubs.getArrayListOfSignalNames(codeButtonHandlerData._mSIDI_RightLeftTrafficSignals)) {
            if (checkJMRIObject.checkSignal(signalName) == false) return false;
        }
        return checkJMRIObject.validClassWithPrefix(PREFIX, codeButtonHandlerData);
    }

//  Validate all internal fields as much as possible:
    private ArrayList<String> formFieldsValid() {
        ArrayList<String> errors = new ArrayList<>();
//  For interrelationship(s) checks:

        boolean leftTrafficDirection = _mSIDI_TrafficDirectionTemp != CodeButtonHandlerData.TRAFFIC_DIRECTION.RIGHT;
        boolean rightTrafficDirection = _mSIDI_TrafficDirectionTemp != CodeButtonHandlerData.TRAFFIC_DIRECTION.LEFT;

        // Convert the 100 row lists into occupied signal entries.
        boolean entriesInLeftRightTrafficSignals =
                ProjectsCommonSubs.getArrayListOfSignals(
                    CommonSubs.getStringArrayFromDefaultTableModel(_mSIDI_TableOfLeftToRightTrafficExternalSignalNamesDefaultTableModel)
                    ).size() != 0;
        boolean entriesInRightLeftTrafficSignals =
                ProjectsCommonSubs.getArrayListOfSignals(
                    CommonSubs.getStringArrayFromDefaultTableModel(_mSIDI_TableOfRightToLeftTrafficExternalSignalNamesDefaultTableModel)
                    ).size() != 0;

//  Checks:

        if (leftTrafficDirection && !entriesInRightLeftTrafficSignals) errors.add(Bundle.getMessage("ErrorDlgSIDIDefineButNoEntriesIn", Bundle.getMessage("LabelSIDILeftTraffic"), _mTableOfRightToLeftTrafficSignalNamesPrompt.getText()));    // NOI18N
        if (rightTrafficDirection && !entriesInLeftRightTrafficSignals) errors.add(Bundle.getMessage("ErrorDlgSIDIDefineButNoEntriesIn", Bundle.getMessage("LabelSIDIRightTraffic"), _mTableOfLeftToRightTrafficSignalNamesPrompt.getText()));

        if (!leftTrafficDirection && entriesInRightLeftTrafficSignals) errors.add(Bundle.getMessage("ErrorDlgSIDINotDefinedWithEntriesIn", Bundle.getMessage("LabelSIDILeftTraffic"), _mTableOfRightToLeftTrafficSignalNamesPrompt.getText()));    // NOI18N
        if (!rightTrafficDirection && entriesInLeftRightTrafficSignals) errors.add(Bundle.getMessage("ErrorDlgSIDINotDefinedWithEntriesIn", Bundle.getMessage("LabelSIDIRightTraffic"), _mTableOfLeftToRightTrafficSignalNamesPrompt.getText()));

        _mCheckJMRIObject.analyzeForm(PREFIX, this, errors);
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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        _mSIDI_TrafficDirection = new javax.swing.ButtonGroup();
        panelLeftColumn = new javax.swing.JPanel();
        _mSIDI_LeftInternalSensorPrompt = new javax.swing.JLabel();
        _mSIDI_LeftInternalSensor = new javax.swing.JComboBox<>();
        _mSIDI_NormalInternalSensorPrompt = new javax.swing.JLabel();
        _mSIDI_NormalInternalSensor = new javax.swing.JComboBox<>();
        _mSIDI_RightInternalSensorPrompt = new javax.swing.JLabel();
        _mSIDI_RightInternalSensor = new javax.swing.JComboBox<>();
        _mSIDI_LableCodeTimeLabel = new javax.swing.JLabel();
        _mSIDI_CodingAndResponseTime = new javax.swing.JFormattedTextField();
        _mSIDI_LockTimeLabel = new javax.swing.JLabel();
        _mSIDI_TimeLockingInterval = new javax.swing.JFormattedTextField();
        _mSIDI_TrafficDirectionGroupPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        _mSIDI_LeftTrafficButton = new javax.swing.JRadioButton();
        _mSIDI_BothTrafficButton = new javax.swing.JRadioButton();
        _mSIDI_RightTrafficButton = new javax.swing.JRadioButton();
        _mSIDI_SaveCloseLabel = new javax.swing.JLabel();
        _mSaveAndClose = new javax.swing.JButton();
        _mSIDI_CompactListsButton = new javax.swing.JButton();
        _mSIDI_CompactListsLabel = new javax.swing.JLabel();
        _mSIDI_ReapplyPatterns = new javax.swing.JButton();
        _mSIDI_LeftRightSignals = new javax.swing.JPanel();
        _mTableOfLeftToRightTrafficSignalNamesPrompt = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        _mSIDI_TableOfLeftToRightTrafficExternalSignalNames = new javax.swing.JTable();
        _mSIDI_RightLeftSignals = new javax.swing.JPanel();
        _mTableOfRightToLeftTrafficSignalNamesPrompt = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        _mSIDI_TableOfRightToLeftTrafficExternalSignalNames = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle(Bundle.getMessage("TitleSIDI"));
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        _mSIDI_LeftInternalSensorPrompt.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        _mSIDI_LeftInternalSensorPrompt.setText(Bundle.getMessage("LabelSIDILeft"));

        _mSIDI_LeftInternalSensor.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        _mSIDI_NormalInternalSensorPrompt.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        _mSIDI_NormalInternalSensorPrompt.setText(Bundle.getMessage("LabelSIDINormal"));

        _mSIDI_NormalInternalSensor.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        _mSIDI_RightInternalSensorPrompt.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        _mSIDI_RightInternalSensorPrompt.setText(Bundle.getMessage("LabelSIDIRight"));

        _mSIDI_RightInternalSensor.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        _mSIDI_LableCodeTimeLabel.setText(Bundle.getMessage("LabelSIDICodeTime"));

        _mSIDI_CodingAndResponseTime.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        _mSIDI_LockTimeLabel.setText(Bundle.getMessage("LabelSIDILockTime"));

        _mSIDI_TimeLockingInterval.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText(Bundle.getMessage("LabelSIDITrafficDirection"));
        jLabel1.setToolTipText("");
        jLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        _mSIDI_TrafficDirection.add(_mSIDI_LeftTrafficButton);
        _mSIDI_LeftTrafficButton.setText(Bundle.getMessage("LabelSIDILeftTraffic"));
        _mSIDI_LeftTrafficButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mSIDI_LeftTrafficButtonActionPerformed(evt);
            }
        });

        _mSIDI_TrafficDirection.add(_mSIDI_BothTrafficButton);
        _mSIDI_BothTrafficButton.setText(Bundle.getMessage("LabelSIDIBothTraffic"));
        _mSIDI_BothTrafficButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mSIDI_BothTrafficButtonActionPerformed(evt);
            }
        });

        _mSIDI_TrafficDirection.add(_mSIDI_RightTrafficButton);
        _mSIDI_RightTrafficButton.setText(Bundle.getMessage("LabelSIDIRightTraffic"));
        _mSIDI_RightTrafficButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mSIDI_RightTrafficButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout _mSIDI_TrafficDirectionGroupPanelLayout = new javax.swing.GroupLayout(_mSIDI_TrafficDirectionGroupPanel);
        _mSIDI_TrafficDirectionGroupPanel.setLayout(_mSIDI_TrafficDirectionGroupPanelLayout);
        _mSIDI_TrafficDirectionGroupPanelLayout.setHorizontalGroup(
            _mSIDI_TrafficDirectionGroupPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(_mSIDI_TrafficDirectionGroupPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(_mSIDI_LeftTrafficButton)
                .addGap(18, 18, 18)
                .addComponent(_mSIDI_BothTrafficButton)
                .addGap(18, 18, 18)
                .addComponent(_mSIDI_RightTrafficButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, _mSIDI_TrafficDirectionGroupPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(58, 58, 58))
        );
        _mSIDI_TrafficDirectionGroupPanelLayout.setVerticalGroup(
            _mSIDI_TrafficDirectionGroupPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(_mSIDI_TrafficDirectionGroupPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(_mSIDI_TrafficDirectionGroupPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mSIDI_LeftTrafficButton)
                    .addComponent(_mSIDI_BothTrafficButton)
                    .addComponent(_mSIDI_RightTrafficButton)))
        );

        _mSIDI_SaveCloseLabel.setText(Bundle.getMessage("InfoSIDICompact"));

        _mSaveAndClose.setText(Bundle.getMessage("ButtonSaveClose"));
        _mSaveAndClose.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mSaveAndCloseActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelLeftColumnLayout = new javax.swing.GroupLayout(panelLeftColumn);
        panelLeftColumn.setLayout(panelLeftColumnLayout);
        panelLeftColumnLayout.setHorizontalGroup(
            panelLeftColumnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelLeftColumnLayout.createSequentialGroup()
                .addContainerGap(28, Short.MAX_VALUE)
                .addGroup(panelLeftColumnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(_mSIDI_TrafficDirectionGroupPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelLeftColumnLayout.createSequentialGroup()
                        .addComponent(_mSaveAndClose)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(_mSIDI_SaveCloseLabel))
                    .addGroup(panelLeftColumnLayout.createSequentialGroup()
                        .addGroup(panelLeftColumnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(_mSIDI_LockTimeLabel)
                            .addComponent(_mSIDI_LableCodeTimeLabel)
                            .addComponent(_mSIDI_NormalInternalSensorPrompt)
                            .addComponent(_mSIDI_LeftInternalSensorPrompt)
                            .addComponent(_mSIDI_RightInternalSensorPrompt))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelLeftColumnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(_mSIDI_RightInternalSensor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(_mSIDI_NormalInternalSensor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(_mSIDI_LeftInternalSensor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(_mSIDI_CodingAndResponseTime, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(_mSIDI_TimeLockingInterval, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(24, 24, 24))
        );
        panelLeftColumnLayout.setVerticalGroup(
            panelLeftColumnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLeftColumnLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(panelLeftColumnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mSIDI_LeftInternalSensorPrompt)
                    .addComponent(_mSIDI_LeftInternalSensor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelLeftColumnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mSIDI_NormalInternalSensorPrompt)
                    .addComponent(_mSIDI_NormalInternalSensor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelLeftColumnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mSIDI_RightInternalSensorPrompt)
                    .addComponent(_mSIDI_RightInternalSensor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelLeftColumnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mSIDI_LableCodeTimeLabel)
                    .addComponent(_mSIDI_CodingAndResponseTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelLeftColumnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mSIDI_LockTimeLabel)
                    .addComponent(_mSIDI_TimeLockingInterval, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(38, 38, 38)
                .addComponent(_mSIDI_TrafficDirectionGroupPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(36, 36, 36)
                .addGroup(panelLeftColumnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mSaveAndClose)
                    .addComponent(_mSIDI_SaveCloseLabel))
                .addContainerGap(98, Short.MAX_VALUE))
        );

        _mSIDI_CompactListsButton.setText(Bundle.getMessage("ButtonSIDIBoth"));
        _mSIDI_CompactListsButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mSIDI_CompactListsButtonActionPerformed(evt);
            }
        });

        _mSIDI_CompactListsLabel.setText(Bundle.getMessage("InfoSIDIRemove"));

        _mSIDI_ReapplyPatterns.setText(Bundle.getMessage("ButtonReapply"));
        _mSIDI_ReapplyPatterns.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mSIDI_ReapplyPatternsActionPerformed(evt);
            }
        });

        _mTableOfLeftToRightTrafficSignalNamesPrompt.setText(Bundle.getMessage("LabelSIDILRTraffic"));

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
            Class<?>[] types = new Class<?> [] {
                java.lang.String.class
            };

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        _mSIDI_TableOfLeftToRightTrafficExternalSignalNames.setRowHeight(18);
        jScrollPane1.setViewportView(_mSIDI_TableOfLeftToRightTrafficExternalSignalNames);

        javax.swing.GroupLayout _mSIDI_LeftRightSignalsLayout = new javax.swing.GroupLayout(_mSIDI_LeftRightSignals);
        _mSIDI_LeftRightSignals.setLayout(_mSIDI_LeftRightSignalsLayout);
        _mSIDI_LeftRightSignalsLayout.setHorizontalGroup(
            _mSIDI_LeftRightSignalsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(_mSIDI_LeftRightSignalsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(_mSIDI_LeftRightSignalsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(_mTableOfLeftToRightTrafficSignalNamesPrompt))
                .addContainerGap())
        );
        _mSIDI_LeftRightSignalsLayout.setVerticalGroup(
            _mSIDI_LeftRightSignalsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(_mSIDI_LeftRightSignalsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(_mTableOfLeftToRightTrafficSignalNamesPrompt)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 363, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(18, Short.MAX_VALUE))
        );

        _mTableOfRightToLeftTrafficSignalNamesPrompt.setText(Bundle.getMessage("LabelSIDIRLTraffic"));

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
            Class<?>[] types = new Class<?> [] {
                java.lang.String.class
            };

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        _mSIDI_TableOfRightToLeftTrafficExternalSignalNames.setRowHeight(18);
        jScrollPane2.setViewportView(_mSIDI_TableOfRightToLeftTrafficExternalSignalNames);

        javax.swing.GroupLayout _mSIDI_RightLeftSignalsLayout = new javax.swing.GroupLayout(_mSIDI_RightLeftSignals);
        _mSIDI_RightLeftSignals.setLayout(_mSIDI_RightLeftSignalsLayout);
        _mSIDI_RightLeftSignalsLayout.setHorizontalGroup(
            _mSIDI_RightLeftSignalsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(_mSIDI_RightLeftSignalsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(_mSIDI_RightLeftSignalsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(_mTableOfRightToLeftTrafficSignalNamesPrompt)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );
        _mSIDI_RightLeftSignalsLayout.setVerticalGroup(
            _mSIDI_RightLeftSignalsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(_mSIDI_RightLeftSignalsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(_mTableOfRightToLeftTrafficSignalNamesPrompt)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 363, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(18, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(_mSIDI_ReapplyPatterns)
                        .addGap(39, 39, 39)
                        .addComponent(_mSIDI_CompactListsLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(_mSIDI_CompactListsButton)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(panelLeftColumn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(_mSIDI_LeftRightSignals, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(_mSIDI_RightLeftSignals, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(17, 17, 17))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(panelLeftColumn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(_mSIDI_RightLeftSignals, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(_mSIDI_ReapplyPatterns)
                            .addComponent(_mSIDI_CompactListsLabel)
                            .addComponent(_mSIDI_CompactListsButton))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(_mSIDI_LeftRightSignals, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void _mSaveAndCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mSaveAndCloseActionPerformed
        if (CommonSubs.missingFieldsErrorDialogDisplayed(this, formFieldsValid(), false)) {
            return; // Do not allow exit or transfer of data.
        }

        _mCodeButtonHandlerData._mSIDI_LeftInternalSensor = CommonSubs.getNBHSensor((String) _mSIDI_LeftInternalSensor.getSelectedItem(), false);
        _mCodeButtonHandlerData._mSIDI_NormalInternalSensor = CommonSubs.getNBHSensor((String) _mSIDI_NormalInternalSensor.getSelectedItem(), false);
        _mCodeButtonHandlerData._mSIDI_RightInternalSensor = CommonSubs.getNBHSensor((String) _mSIDI_RightInternalSensor.getSelectedItem(), false);

        _mCodeButtonHandlerData._mSIDI_CodingTimeInMilliseconds = CommonSubs.getIntFromJTextFieldNoThrow(_mSIDI_CodingAndResponseTime);
        _mCodeButtonHandlerData._mSIDI_TimeLockingTimeInMilliseconds = CommonSubs.getIntFromJTextFieldNoThrow(_mSIDI_TimeLockingInterval);
        _mCodeButtonHandlerData._mSIDI_TrafficDirection = _mSIDI_TrafficDirectionTemp;

        _mCodeButtonHandlerData._mSIDI_LeftRightTrafficSignals = ProjectsCommonSubs.getArrayListOfSignals(CommonSubs.getStringArrayFromDefaultTableModel(_mSIDI_TableOfLeftToRightTrafficExternalSignalNamesDefaultTableModel));
        _mCodeButtonHandlerData._mSIDI_RightLeftTrafficSignals = ProjectsCommonSubs.getArrayListOfSignals(CommonSubs.getStringArrayFromDefaultTableModel(_mSIDI_TableOfRightToLeftTrafficExternalSignalNamesDefaultTableModel));
        _mClosedNormally = true;
        _mAwtWindowProperties.saveWindowState(this, FORM_PROPERTIES);
        dispose();
    }//GEN-LAST:event__mSaveAndCloseActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        _mAwtWindowProperties.saveWindowState(this, FORM_PROPERTIES);
        if (CommonSubs.allowClose(this, dataChanged())) dispose();
    }//GEN-LAST:event_formWindowClosing

    private void _mSIDI_CompactListsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mSIDI_CompactListsButtonActionPerformed
        CommonSubs.compactDefaultTableModel(_mSIDI_TableOfLeftToRightTrafficExternalSignalNamesDefaultTableModel);
        CommonSubs.compactDefaultTableModel(_mSIDI_TableOfRightToLeftTrafficExternalSignalNamesDefaultTableModel);
    }//GEN-LAST:event__mSIDI_CompactListsButtonActionPerformed

    private void _mSIDI_ReapplyPatternsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mSIDI_ReapplyPatternsActionPerformed
        CodeButtonHandlerData temp = _mCodeButtonHandlerData;
        temp = CodeButtonHandlerDataRoutines.uECBHDWSD_SIDI(_mProgramProperties, temp);
        CommonSubs.populateJComboBoxWithBeans(_mSIDI_LeftInternalSensor, "Sensor", temp._mSIDI_LeftInternalSensor.getHandleName(), false);   // NOI18N
        CommonSubs.populateJComboBoxWithBeans(_mSIDI_NormalInternalSensor, "Sensor", temp._mSIDI_NormalInternalSensor.getHandleName(), false);   // NOI18N
        CommonSubs.populateJComboBoxWithBeans(_mSIDI_RightInternalSensor, "Sensor", temp._mSIDI_RightInternalSensor.getHandleName(), false);   // NOI18N
    }//GEN-LAST:event__mSIDI_ReapplyPatternsActionPerformed

    private void _mSIDI_LeftTrafficButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mSIDI_LeftTrafficButtonActionPerformed
        _mSIDI_TrafficDirectionTemp = CodeButtonHandlerData.TRAFFIC_DIRECTION.LEFT;

        _mSIDI_TableOfLeftToRightTrafficExternalSignalNames.setEnabled(false);
        _mSIDI_TableOfRightToLeftTrafficExternalSignalNames.setEnabled(true);

        _mTableOfLeftToRightTrafficSignalNamesPrompt.setEnabled(false);
        _mTableOfRightToLeftTrafficSignalNamesPrompt.setEnabled(true);
    }//GEN-LAST:event__mSIDI_LeftTrafficButtonActionPerformed

    private void _mSIDI_BothTrafficButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mSIDI_BothTrafficButtonActionPerformed
        _mSIDI_TrafficDirectionTemp = CodeButtonHandlerData.TRAFFIC_DIRECTION.BOTH;

        _mSIDI_TableOfLeftToRightTrafficExternalSignalNames.setEnabled(true);
        _mSIDI_TableOfRightToLeftTrafficExternalSignalNames.setEnabled(true);

        _mTableOfLeftToRightTrafficSignalNamesPrompt.setEnabled(true);
        _mTableOfRightToLeftTrafficSignalNamesPrompt.setEnabled(true);
    }//GEN-LAST:event__mSIDI_BothTrafficButtonActionPerformed

    private void _mSIDI_RightTrafficButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mSIDI_RightTrafficButtonActionPerformed
        _mSIDI_TrafficDirectionTemp = CodeButtonHandlerData.TRAFFIC_DIRECTION.RIGHT;

        _mSIDI_TableOfRightToLeftTrafficExternalSignalNames.setEnabled(false);
        _mSIDI_TableOfLeftToRightTrafficExternalSignalNames.setEnabled(true);

        _mTableOfRightToLeftTrafficSignalNamesPrompt.setEnabled(false);
        _mTableOfLeftToRightTrafficSignalNamesPrompt.setEnabled(true);
    }//GEN-LAST:event__mSIDI_RightTrafficButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton _mSIDI_BothTrafficButton;
    private javax.swing.JFormattedTextField _mSIDI_CodingAndResponseTime;
    private javax.swing.JButton _mSIDI_CompactListsButton;
    private javax.swing.JLabel _mSIDI_CompactListsLabel;
    private javax.swing.JLabel _mSIDI_LableCodeTimeLabel;
    private javax.swing.JComboBox<String> _mSIDI_LeftInternalSensor;
    private javax.swing.JLabel _mSIDI_LeftInternalSensorPrompt;
    private javax.swing.JPanel _mSIDI_LeftRightSignals;
    private javax.swing.JRadioButton _mSIDI_LeftTrafficButton;
    private javax.swing.JLabel _mSIDI_LockTimeLabel;
    private javax.swing.JComboBox<String> _mSIDI_NormalInternalSensor;
    private javax.swing.JLabel _mSIDI_NormalInternalSensorPrompt;
    private javax.swing.JButton _mSIDI_ReapplyPatterns;
    private javax.swing.JComboBox<String> _mSIDI_RightInternalSensor;
    private javax.swing.JLabel _mSIDI_RightInternalSensorPrompt;
    private javax.swing.JPanel _mSIDI_RightLeftSignals;
    private javax.swing.JRadioButton _mSIDI_RightTrafficButton;
    private javax.swing.JLabel _mSIDI_SaveCloseLabel;
    private javax.swing.JTable _mSIDI_TableOfLeftToRightTrafficExternalSignalNames;
    private javax.swing.JTable _mSIDI_TableOfRightToLeftTrafficExternalSignalNames;
    private javax.swing.JFormattedTextField _mSIDI_TimeLockingInterval;
    private javax.swing.ButtonGroup _mSIDI_TrafficDirection;
    private javax.swing.JPanel _mSIDI_TrafficDirectionGroupPanel;
    private javax.swing.JButton _mSaveAndClose;
    private javax.swing.JLabel _mTableOfLeftToRightTrafficSignalNamesPrompt;
    private javax.swing.JLabel _mTableOfRightToLeftTrafficSignalNamesPrompt;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPanel panelLeftColumn;
    // End of variables declaration//GEN-END:variables
}
