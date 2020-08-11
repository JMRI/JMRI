package jmri.jmrit.ctc.editor.gui;

import jmri.jmrit.ctc.editor.code.AwtWindowProperties;
import jmri.jmrit.ctc.editor.code.CheckJMRIObject;
import jmri.jmrit.ctc.editor.code.CodeButtonHandlerDataRoutines;
import jmri.jmrit.ctc.editor.code.CommonSubs;
import jmri.jmrit.ctc.editor.code.ProgramProperties;

import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.JComboBox;

import jmri.InstanceManager;
import jmri.TurnoutManager;
import jmri.jmrit.ctc.ctcserialdata.*;

/**
 *
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 * 
 * 7/30/2020 Bug Fix (found by a user in the field, as reported to Dave Sand):
 * The user can in two different O.S. sections (or in the
 * same O.S. section) mention the same turnout multiple times.  Now, of course
 * this is illegal, however the program does NOT (at present) enforce this.
 * Then at run-time, this causes an "infinite loop" as each tries to fight
 * against the other(s), and eventually the stack overflows.
 * To Fix: At start of form, get a list of all other O.S. sections turnouts
 * that are mentioned, this is a "static" exclusion list for this time only
 * run" of this form.
 * In addition, when editing one of the lists, the other 3 lists selected
 * items also are excluded "dynamically".
 */
public class FrmTUL extends javax.swing.JFrame {

    /**
     * Creates new form DlgTUL
     */
    private static final String FORM_PROPERTIES = "DlgTUL";     // NOI18N
    private static final String PREFIX = "_mTUL_";              // NOI18N
    private final AwtWindowProperties _mAwtWindowProperties;
    private final CTCSerialData _mCTCSerialData;
    private boolean _mClosedNormally = false;
    public boolean closedNormally() { return _mClosedNormally; }
    private final CodeButtonHandlerData _mCodeButtonHandlerData;
    private final ProgramProperties _mProgramProperties;
    private final CheckJMRIObject _mCheckJMRIObject;
//  For support of no dups:  
    private final HashSet<String> _mStartingHashSetOfTurnouts = new HashSet<>();
    private final ArrayList<String> _mArrayListOfThisRecordsUsedLockedTurnouts = new ArrayList<>(); // Should "always" be 4 in length.  See "initializeAll4LockedTurnoutJComboBoxesAndSupportingData"
    private boolean _mIgnoreActionEvent = false;
//  End of support of no dups.    

    private String _mTUL_DispatcherInternalSensorLockToggleOrig;
    private String _mTUL_ExternalTurnoutOrig;
    private boolean _mTUL_ExternalTurnoutFeedbackDifferentOrig;
    private String _mTUL_DispatcherInternalSensorUnlockedIndicatorOrig;
    private boolean _mTUL_NoDispatcherControlOfSwitchOrig;
    private boolean _mTUL_ndcos_WhenLockedSwitchStateIsClosedOrig;
    private CodeButtonHandlerData.LOCK_IMPLEMENTATION _mTUL_LockImplementationOrig;
    private String _mTUL_AdditionalExternalTurnout1Orig;
    private boolean _mTUL_AdditionalExternalTurnout1FeedbackDifferentOrig;
    private String _mTUL_AdditionalExternalTurnout2Orig;
    private boolean _mTUL_AdditionalExternalTurnout2FeedbackDifferentOrig;
    private String _mTUL_AdditionalExternalTurnout3Orig;
    private boolean _mTUL_AdditionalExternalTurnout3FeedbackDifferentOrig;

    private void initOrig() {
        _mTUL_DispatcherInternalSensorLockToggleOrig = _mCodeButtonHandlerData._mTUL_DispatcherInternalSensorLockToggle;
        _mTUL_ExternalTurnoutOrig = _mCodeButtonHandlerData._mTUL_ExternalTurnout;
        _mTUL_ExternalTurnoutFeedbackDifferentOrig = _mCodeButtonHandlerData._mTUL_ExternalTurnoutFeedbackDifferent;
        _mTUL_DispatcherInternalSensorUnlockedIndicatorOrig = _mCodeButtonHandlerData._mTUL_DispatcherInternalSensorUnlockedIndicator;
        _mTUL_NoDispatcherControlOfSwitchOrig = _mCodeButtonHandlerData._mTUL_NoDispatcherControlOfSwitch;
        _mTUL_ndcos_WhenLockedSwitchStateIsClosedOrig = _mCodeButtonHandlerData._mTUL_ndcos_WhenLockedSwitchStateIsClosed;
        _mTUL_LockImplementationOrig = _mCodeButtonHandlerData._mTUL_LockImplementation;
        _mTUL_AdditionalExternalTurnout1Orig = _mCodeButtonHandlerData._mTUL_AdditionalExternalTurnout1;
        _mTUL_AdditionalExternalTurnout1FeedbackDifferentOrig = _mCodeButtonHandlerData._mTUL_AdditionalExternalTurnout1FeedbackDifferent;
        _mTUL_AdditionalExternalTurnout2Orig = _mCodeButtonHandlerData._mTUL_AdditionalExternalTurnout2;
        _mTUL_AdditionalExternalTurnout2FeedbackDifferentOrig = _mCodeButtonHandlerData._mTUL_AdditionalExternalTurnout2FeedbackDifferent;
        _mTUL_AdditionalExternalTurnout3Orig = _mCodeButtonHandlerData._mTUL_AdditionalExternalTurnout3;
        _mTUL_AdditionalExternalTurnout3FeedbackDifferentOrig = _mCodeButtonHandlerData._mTUL_AdditionalExternalTurnout3FeedbackDifferent;
    }
    private boolean dataChanged() {
        if (!_mTUL_DispatcherInternalSensorLockToggleOrig.equals(_mTUL_DispatcherInternalSensorLockToggle.getText())) return true;
        if (!_mTUL_ExternalTurnoutOrig.equals(_mTUL_ExternalTurnout.getSelectedItem())) return true;
        if (_mTUL_ExternalTurnoutFeedbackDifferentOrig != _mTUL_ExternalTurnoutFeedbackDifferent.isSelected()) return true;
        if (!_mTUL_DispatcherInternalSensorUnlockedIndicatorOrig.equals(_mTUL_DispatcherInternalSensorUnlockedIndicator.getText())) return true;
        if (_mTUL_NoDispatcherControlOfSwitchOrig != _mTUL_NoDispatcherControlOfSwitch.isSelected()) return true;
        if (_mTUL_ndcos_WhenLockedSwitchStateIsClosedOrig != _mTUL_ndcos_WhenLockedSwitchStateIsClosed.isSelected()) return true;
        if (_mTUL_LockImplementationOrig != CodeButtonHandlerData.LOCK_IMPLEMENTATION.getLockImplementation(_mTUL_LockImplementation)) return true;
        if (!_mTUL_AdditionalExternalTurnout1Orig.equals(_mTUL_AdditionalExternalTurnout1.getSelectedItem())) return true;
        if (_mTUL_AdditionalExternalTurnout1FeedbackDifferentOrig != _mTUL_AdditionalExternalTurnout1FeedbackDifferent.isSelected()) return true;
        if (!_mTUL_AdditionalExternalTurnout2Orig.equals(_mTUL_AdditionalExternalTurnout2.getSelectedItem())) return true;
        if (_mTUL_AdditionalExternalTurnout2FeedbackDifferentOrig != _mTUL_AdditionalExternalTurnout2FeedbackDifferent.isSelected()) return true;
        if (!_mTUL_AdditionalExternalTurnout3Orig.equals(_mTUL_AdditionalExternalTurnout3.getSelectedItem())) return true;
        if (_mTUL_AdditionalExternalTurnout3FeedbackDifferentOrig != _mTUL_AdditionalExternalTurnout3FeedbackDifferent.isSelected()) return true;
        return false;
    }

    public FrmTUL(  AwtWindowProperties awtWindowProperties, 
                    CTCSerialData ctcSerialData,
                    CodeButtonHandlerData codeButtonHandlerData,
                    ProgramProperties programProperties, CheckJMRIObject checkJMRIObject) {
        super();
        initComponents();
        CommonSubs.addHelpMenu(this, "package.jmri.jmrit.ctc.CTC_frmTUL", true);  // NOI18N
        _mAwtWindowProperties = awtWindowProperties;
        _mCTCSerialData = ctcSerialData;
        _mCodeButtonHandlerData = codeButtonHandlerData;
        _mProgramProperties = programProperties;
        _mCheckJMRIObject = checkJMRIObject;
        CommonSubs.numberButtonGroup(_mTUL_LockImplementation);
        CommonSubs.setButtonSelected(_mTUL_LockImplementation, _mCodeButtonHandlerData._mTUL_LockImplementation.getInt());
        _mTUL_DispatcherInternalSensorLockToggle.setText(_mCodeButtonHandlerData._mTUL_DispatcherInternalSensorLockToggle);
        _mTUL_ExternalTurnoutFeedbackDifferent.setSelected(_mCodeButtonHandlerData._mTUL_ExternalTurnoutFeedbackDifferent);
        _mTUL_DispatcherInternalSensorUnlockedIndicator.setText(_mCodeButtonHandlerData._mTUL_DispatcherInternalSensorUnlockedIndicator);
        _mTUL_NoDispatcherControlOfSwitch.setSelected(_mCodeButtonHandlerData._mTUL_NoDispatcherControlOfSwitch);
        _mTUL_ndcos_WhenLockedSwitchStateIsClosed.setSelected(_mCodeButtonHandlerData._mTUL_ndcos_WhenLockedSwitchStateIsClosed);
        _mTUL_AdditionalExternalTurnout1FeedbackDifferent.setSelected(_mCodeButtonHandlerData._mTUL_AdditionalExternalTurnout1FeedbackDifferent);
        _mTUL_AdditionalExternalTurnout2FeedbackDifferent.setSelected(_mCodeButtonHandlerData._mTUL_AdditionalExternalTurnout2FeedbackDifferent);
        _mTUL_AdditionalExternalTurnout3FeedbackDifferent.setSelected(_mCodeButtonHandlerData._mTUL_AdditionalExternalTurnout3FeedbackDifferent);
        initializeAll4LockedTurnoutJComboBoxesAndSupportingData();
        initOrig();
        _mAwtWindowProperties.setWindowState(this, FORM_PROPERTIES);
        this.getRootPane().setDefaultButton(_mSaveAndClose);
    }

    public static boolean dialogCodeButtonHandlerDataValid(CheckJMRIObject checkJMRIObject, CodeButtonHandlerData codeButtonHandlerData) {
        if (!codeButtonHandlerData._mTUL_Enabled) return true; // Not enabled, can be no error!
//  Checks:
        if (ProjectsCommonSubs.isNullOrEmptyString(codeButtonHandlerData._mTUL_ExternalTurnout)) return false;
        return checkJMRIObject.validClassWithPrefix(PREFIX, codeButtonHandlerData);
    }

//  Validate all internal fields as much as possible:
    private ArrayList<String> formFieldsValid() {
        ArrayList<String> errors = new ArrayList<>();
//  Checks:
        CommonSubs.checkJComboBoxNotEmpty(_mTUL_ExternalTurnout, _mTUL_ActualTurnoutPrompt, errors);
        _mCheckJMRIObject.analyzeForm(PREFIX, this, errors);
        return errors;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        _mTUL_LockImplementation = new javax.swing.ButtonGroup();
        _mSaveAndClose = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        _mTUL_DispatcherInternalSensorLockToggle = new javax.swing.JTextField();
        _mTUL_ExternalTurnoutFeedbackDifferent = new javax.swing.JCheckBox();
        jLabel4 = new javax.swing.JLabel();
        _mTUL_ActualTurnoutPrompt = new javax.swing.JLabel();
        _mTUL_DispatcherInternalSensorUnlockedIndicator = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        _mTUL_NoDispatcherControlOfSwitch = new javax.swing.JCheckBox();
        jLabel7 = new javax.swing.JLabel();
        _mTUL_ndcos_WhenLockedSwitchStateIsClosed = new javax.swing.JCheckBox();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jRadioButton1 = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        jRadioButton2 = new javax.swing.JRadioButton();
        jLabel11 = new javax.swing.JLabel();
        _mTUL_AdditionalExternalTurnout1FeedbackDifferent = new javax.swing.JCheckBox();
        _mTUL_AdditionalExternalTurnout2FeedbackDifferent = new javax.swing.JCheckBox();
        _mTUL_AdditionalExternalTurnout3FeedbackDifferent = new javax.swing.JCheckBox();
        _mTUL_ExternalTurnout = new javax.swing.JComboBox<>();
        _mTUL_AdditionalExternalTurnout1 = new javax.swing.JComboBox<>();
        _mTUL_AdditionalExternalTurnout2 = new javax.swing.JComboBox<>();
        _mTUL_AdditionalExternalTurnout3 = new javax.swing.JComboBox<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle(Bundle.getMessage("TitleDlgTUL"));
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

        jLabel2.setText(Bundle.getMessage("LabelDlgTULSensor"));

        _mTUL_ExternalTurnoutFeedbackDifferent.setText(" ");

        jLabel4.setText(Bundle.getMessage("InfoDlgTULFeedback"));

        _mTUL_ActualTurnoutPrompt.setText(Bundle.getMessage("LabelDlgTULToName"));

        jLabel6.setText(Bundle.getMessage("LabelDlgTULInd"));

        _mTUL_NoDispatcherControlOfSwitch.setText(" ");

        jLabel7.setText(Bundle.getMessage("LabelDlgTULNoDisp"));

        _mTUL_ndcos_WhenLockedSwitchStateIsClosed.setText(" ");

        jLabel8.setText(Bundle.getMessage("LabelDlgTULClosed"));

        jLabel9.setText(Bundle.getMessage("InfoDlgTULChecked"));

        jLabel10.setText(Bundle.getMessage("InfoDlgTULConfig"));

        jButton2.setText(Bundle.getMessage("ButtonReapply"));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton1.setText(Bundle.getMessage("ButtonDlgTULJust"));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        _mTUL_LockImplementation.add(jRadioButton1);
        jRadioButton1.setText(Bundle.getMessage("LabelDlgTULGregs"));

        jLabel1.setText(Bundle.getMessage("InfoDlgTULLock"));

        _mTUL_LockImplementation.add(jRadioButton2);
        jRadioButton2.setText(Bundle.getMessage("LabelDlgTULOther"));
        jRadioButton2.setEnabled(false);

        jLabel11.setText(Bundle.getMessage("LabelDlgTULToOpt"));

        _mTUL_AdditionalExternalTurnout1FeedbackDifferent.setText(" ");

        _mTUL_AdditionalExternalTurnout2FeedbackDifferent.setText(" ");

        _mTUL_AdditionalExternalTurnout3FeedbackDifferent.setText(" ");

        _mTUL_ExternalTurnout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mTUL_ExternalTurnoutActionPerformed(evt);
            }
        });

        _mTUL_AdditionalExternalTurnout1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mTUL_AdditionalExternalTurnout1ActionPerformed(evt);
            }
        });

        _mTUL_AdditionalExternalTurnout2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mTUL_AdditionalExternalTurnout2ActionPerformed(evt);
            }
        });

        _mTUL_AdditionalExternalTurnout3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mTUL_AdditionalExternalTurnout3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addGap(35, 35, 35)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel2)
                                .addComponent(jLabel6)))
                        .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton1)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(_mTUL_ActualTurnoutPrompt, javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel11, javax.swing.GroupLayout.Alignment.TRAILING)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(_mSaveAndClose)
                    .addComponent(_mTUL_DispatcherInternalSensorLockToggle, javax.swing.GroupLayout.PREFERRED_SIZE, 228, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(_mTUL_NoDispatcherControlOfSwitch)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel9))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(_mTUL_ndcos_WhenLockedSwitchStateIsClosed)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel10))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jRadioButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRadioButton2))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(_mTUL_AdditionalExternalTurnout3, 0, 133, Short.MAX_VALUE)
                            .addComponent(_mTUL_AdditionalExternalTurnout2, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(_mTUL_AdditionalExternalTurnout1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(_mTUL_ExternalTurnout, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(_mTUL_ExternalTurnoutFeedbackDifferent)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel4))
                            .addComponent(_mTUL_AdditionalExternalTurnout1FeedbackDifferent)
                            .addComponent(_mTUL_AdditionalExternalTurnout2FeedbackDifferent)
                            .addComponent(_mTUL_AdditionalExternalTurnout3FeedbackDifferent)))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jButton2)
                        .addComponent(_mTUL_DispatcherInternalSensorUnlockedIndicator, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 228, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(_mTUL_DispatcherInternalSensorLockToggle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mTUL_ActualTurnoutPrompt)
                    .addComponent(_mTUL_ExternalTurnoutFeedbackDifferent)
                    .addComponent(jLabel4)
                    .addComponent(_mTUL_ExternalTurnout, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(_mTUL_AdditionalExternalTurnout1FeedbackDifferent)
                    .addComponent(_mTUL_AdditionalExternalTurnout1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mTUL_AdditionalExternalTurnout2FeedbackDifferent)
                    .addComponent(_mTUL_AdditionalExternalTurnout2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mTUL_AdditionalExternalTurnout3FeedbackDifferent)
                    .addComponent(_mTUL_AdditionalExternalTurnout3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mTUL_DispatcherInternalSensorUnlockedIndicator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addGap(22, 22, 22)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mTUL_NoDispatcherControlOfSwitch)
                    .addComponent(jLabel7)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mTUL_ndcos_WhenLockedSwitchStateIsClosed)
                    .addComponent(jLabel8)
                    .addComponent(jLabel10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jRadioButton1)
                    .addComponent(jRadioButton2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addGap(18, 18, 18)
                .addComponent(_mSaveAndClose)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void _mSaveAndCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mSaveAndCloseActionPerformed
        if (CommonSubs.missingFieldsErrorDialogDisplayed(this, formFieldsValid(), false)) {
            return; // Do not allow exit or transfer of data.
        }
        _mCodeButtonHandlerData._mTUL_DispatcherInternalSensorLockToggle = _mTUL_DispatcherInternalSensorLockToggle.getText();
        _mCodeButtonHandlerData._mTUL_ExternalTurnout = (String) _mTUL_ExternalTurnout.getSelectedItem();
        _mCodeButtonHandlerData._mTUL_ExternalTurnoutFeedbackDifferent = _mTUL_ExternalTurnoutFeedbackDifferent.isSelected();
        _mCodeButtonHandlerData._mTUL_DispatcherInternalSensorUnlockedIndicator = _mTUL_DispatcherInternalSensorUnlockedIndicator.getText();
        _mCodeButtonHandlerData._mTUL_NoDispatcherControlOfSwitch = _mTUL_NoDispatcherControlOfSwitch.isSelected();
        _mCodeButtonHandlerData._mTUL_ndcos_WhenLockedSwitchStateIsClosed = _mTUL_ndcos_WhenLockedSwitchStateIsClosed.isSelected();
        _mCodeButtonHandlerData._mTUL_LockImplementation = CodeButtonHandlerData.LOCK_IMPLEMENTATION.getLockImplementation(_mTUL_LockImplementation);
        _mCodeButtonHandlerData._mTUL_AdditionalExternalTurnout1 = (String) _mTUL_AdditionalExternalTurnout1.getSelectedItem();
        _mCodeButtonHandlerData._mTUL_AdditionalExternalTurnout1FeedbackDifferent = _mTUL_AdditionalExternalTurnout1FeedbackDifferent.isSelected();
        _mCodeButtonHandlerData._mTUL_AdditionalExternalTurnout2 = (String) _mTUL_AdditionalExternalTurnout2.getSelectedItem();
        _mCodeButtonHandlerData._mTUL_AdditionalExternalTurnout2FeedbackDifferent = _mTUL_AdditionalExternalTurnout2FeedbackDifferent.isSelected();
        _mCodeButtonHandlerData._mTUL_AdditionalExternalTurnout3 = (String) _mTUL_AdditionalExternalTurnout3.getSelectedItem();
        _mCodeButtonHandlerData._mTUL_AdditionalExternalTurnout3FeedbackDifferent = _mTUL_AdditionalExternalTurnout3FeedbackDifferent.isSelected();
        _mClosedNormally = true;
        _mAwtWindowProperties.saveWindowState(this, FORM_PROPERTIES);
        dispose();
    }//GEN-LAST:event__mSaveAndCloseActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        _mAwtWindowProperties.saveWindowState(this, FORM_PROPERTIES);
        if (CommonSubs.allowClose(this, dataChanged())) dispose();
    }//GEN-LAST:event_formWindowClosing

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        CodeButtonHandlerData temp = _mCodeButtonHandlerData.deepCopy();
        temp = CodeButtonHandlerDataRoutines.uECBHDWSD_TUL(_mProgramProperties, temp);
        _mTUL_DispatcherInternalSensorLockToggle.setText(temp._mTUL_DispatcherInternalSensorLockToggle);
        _mTUL_DispatcherInternalSensorUnlockedIndicator.setText(temp._mTUL_DispatcherInternalSensorUnlockedIndicator);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        _mTUL_DispatcherInternalSensorLockToggle.setText("");
        _mTUL_DispatcherInternalSensorUnlockedIndicator.setText("");
    }//GEN-LAST:event_jButton1ActionPerformed

    private void _mTUL_ExternalTurnoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mTUL_ExternalTurnoutActionPerformed
        initializeAll4LockedTurnoutJComboBoxesAndSupportingData();
    }//GEN-LAST:event__mTUL_ExternalTurnoutActionPerformed

    private void _mTUL_AdditionalExternalTurnout1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mTUL_AdditionalExternalTurnout1ActionPerformed
        initializeAll4LockedTurnoutJComboBoxesAndSupportingData();
    }//GEN-LAST:event__mTUL_AdditionalExternalTurnout1ActionPerformed

    private void _mTUL_AdditionalExternalTurnout2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mTUL_AdditionalExternalTurnout2ActionPerformed
        initializeAll4LockedTurnoutJComboBoxesAndSupportingData();
    }//GEN-LAST:event__mTUL_AdditionalExternalTurnout2ActionPerformed

    private void _mTUL_AdditionalExternalTurnout3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mTUL_AdditionalExternalTurnout3ActionPerformed
        initializeAll4LockedTurnoutJComboBoxesAndSupportingData();
    }//GEN-LAST:event__mTUL_AdditionalExternalTurnout3ActionPerformed

    private void initializeAll4LockedTurnoutJComboBoxesAndSupportingData() {
        if (_mIgnoreActionEvent) return;    // Process ONLY when the user selectes an item in the combo box (even if the same one), otherwise forget it.
        HashSet<String> hashSetOfExistingLockedTurnoutsExcludingThisOne = _mCTCSerialData.getHashSetOfAllLockedTurnoutsExcludingPassedOne(_mCodeButtonHandlerData); // Once initialized, fixed!
        InstanceManager.getDefault(TurnoutManager.class).getNamedBeanSet().forEach((t) -> { _mStartingHashSetOfTurnouts.add(t.getDisplayName()); });
        _mStartingHashSetOfTurnouts.removeAll(hashSetOfExistingLockedTurnoutsExcludingThisOne);
        _mArrayListOfThisRecordsUsedLockedTurnouts.clear();
        _mArrayListOfThisRecordsUsedLockedTurnouts.add(_mCodeButtonHandlerData._mTUL_ExternalTurnout);
        _mArrayListOfThisRecordsUsedLockedTurnouts.add(_mCodeButtonHandlerData._mTUL_AdditionalExternalTurnout1);
        _mArrayListOfThisRecordsUsedLockedTurnouts.add(_mCodeButtonHandlerData._mTUL_AdditionalExternalTurnout2);
        _mArrayListOfThisRecordsUsedLockedTurnouts.add(_mCodeButtonHandlerData._mTUL_AdditionalExternalTurnout3);
        update1LockedTurnoutJComboBox(_mTUL_ExternalTurnout, 0, _mCodeButtonHandlerData._mTUL_ExternalTurnout);
        update1LockedTurnoutJComboBox(_mTUL_AdditionalExternalTurnout1, 1, _mCodeButtonHandlerData._mTUL_AdditionalExternalTurnout1);
        update1LockedTurnoutJComboBox(_mTUL_AdditionalExternalTurnout2, 2, _mCodeButtonHandlerData._mTUL_AdditionalExternalTurnout2);
        update1LockedTurnoutJComboBox(_mTUL_AdditionalExternalTurnout3, 3, _mCodeButtonHandlerData._mTUL_AdditionalExternalTurnout3);
    }
    
    private void update1LockedTurnoutJComboBox(JComboBox<String> jComboBox, int index, String currentSelection) {
        HashSet<String> ultimateHashSet = new HashSet<>(_mStartingHashSetOfTurnouts);   // Make a deep copy.
        ultimateHashSet.removeAll(returnHashSetSubset(index));
        populateJComboBox(jComboBox, ultimateHashSet, currentSelection);
    }
    /**
     * Just returns a sub-set of _mArrayListOfThisRecordsUsedLockedTurnouts excluding
     * any blank entries, and the one index passed.
     * 
     * @param indexToLeaveOff The JComboBox "field" we will be editing: 0 = _mTUL_ExternalTurnout, 1 = _mTUL_AdditionalExternalTurnout1, 2 = _mTUL_AdditionalExternalTurnout2, 3 = _mTUL_AdditionalExternalTurnout3
     * @return The requested subset of _mArrayListOfThisRecordsUsedLockedTurnouts 
     */
    private HashSet<String> returnHashSetSubset(int indexToLeaveOff) {
        HashSet<String> returnValue = new HashSet<>();
        for (int index = 0; index < _mArrayListOfThisRecordsUsedLockedTurnouts.size(); index++) {
            if (index != indexToLeaveOff && !_mArrayListOfThisRecordsUsedLockedTurnouts.get(index).isEmpty()) { returnValue.add(_mArrayListOfThisRecordsUsedLockedTurnouts.get(index)); }
        }
        return returnValue;
    }
    
    /**
     * 
     * This is a DIRECT plagiarization of Dave Sands CommonSubs.populateJComboBoxWithBeans, repurposed to support
     * what is needed specifically by Turnout Locking.
     * It does not have the flexibility of the original routine, since thats not needed.
     * (see "_mIgnoreActionEvent", since I don't know of another way to do it).
     * 
     * Populate a combo box with bean names using getDisplayName().
     * <p>
     * If a panel xml file has not been loaded, the combo box will behave as a
     * text field (editable), otherwise it will behave as standard combo box (not editable).
     * @param jComboBox The string based combo box to be populated.
     * @param populateWith A hash set that needs to be sorted first, to populate the drop down list with.
     * @param currentSelection The current item to be selected, none if null.
     */
    public void populateJComboBox(JComboBox<String> jComboBox, HashSet<String> populateWith, String currentSelection) {
        _mIgnoreActionEvent = true;
        boolean panelLoaded = InstanceManager.getDefault(jmri.jmrit.ctc.editor.gui.FrmMainForm.class)._mPanelLoaded;
        jComboBox.removeAllItems();
        if (!panelLoaded) {
            // Configure combo box as a pseudo text field
            jComboBox.setEditable(true);
            jComboBox.addItem(currentSelection == null ? "" : currentSelection);
            _mIgnoreActionEvent = false;
            return;
        }
        jComboBox.setEditable(false);
        ArrayList<String> list = new ArrayList<>(populateWith);
        list.sort(new jmri.util.AlphanumComparator());
        list.forEach((item) -> {
            jComboBox.addItem(item);
        });
        jmri.util.swing.JComboBoxUtil.setupComboBoxMaxRows(jComboBox);
        jComboBox.setSelectedItem(currentSelection);
        jComboBox.insertItemAt("", 0);
        if (currentSelection == null || currentSelection.isEmpty()) {
            jComboBox.setSelectedIndex(0);
        }
        _mIgnoreActionEvent = false;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton _mSaveAndClose;
    private javax.swing.JLabel _mTUL_ActualTurnoutPrompt;
    private javax.swing.JComboBox<String> _mTUL_AdditionalExternalTurnout1;
    private javax.swing.JCheckBox _mTUL_AdditionalExternalTurnout1FeedbackDifferent;
    private javax.swing.JComboBox<String> _mTUL_AdditionalExternalTurnout2;
    private javax.swing.JCheckBox _mTUL_AdditionalExternalTurnout2FeedbackDifferent;
    private javax.swing.JComboBox<String> _mTUL_AdditionalExternalTurnout3;
    private javax.swing.JCheckBox _mTUL_AdditionalExternalTurnout3FeedbackDifferent;
    private javax.swing.JTextField _mTUL_DispatcherInternalSensorLockToggle;
    private javax.swing.JTextField _mTUL_DispatcherInternalSensorUnlockedIndicator;
    private javax.swing.JComboBox<String> _mTUL_ExternalTurnout;
    private javax.swing.JCheckBox _mTUL_ExternalTurnoutFeedbackDifferent;
    private javax.swing.ButtonGroup _mTUL_LockImplementation;
    private javax.swing.JCheckBox _mTUL_NoDispatcherControlOfSwitch;
    private javax.swing.JCheckBox _mTUL_ndcos_WhenLockedSwitchStateIsClosed;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    // End of variables declaration//GEN-END:variables
}
