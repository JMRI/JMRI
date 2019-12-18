package jmri.jmrit.ctc.editor.gui;

import jmri.jmrit.ctc.editor.code.AwtWindowProperties;
import jmri.jmrit.ctc.editor.code.CheckJMRIObject;
import jmri.jmrit.ctc.editor.code.CodeButtonHandlerDataRoutines;
import jmri.jmrit.ctc.editor.code.CommonSubs;
import jmri.jmrit.ctc.editor.code.ProgramProperties;
import java.util.ArrayList;
import jmri.jmrit.ctc.ctcserialdata.CodeButtonHandlerData;
import jmri.jmrit.ctc.ctcserialdata.ProjectsCommonSubs;

/**
 *
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 */
public class FrmSWDI extends javax.swing.JFrame {

    private static final String FORM_PROPERTIES = "DlgSWDI";    // NOI18N
    private static final String PREFIX = "_mSWDI_";             // NOI18N
    private final AwtWindowProperties _mAwtWindowProperties;
    private boolean _mClosedNormally = false;
    public boolean closedNormally() { return _mClosedNormally; }
    private final CodeButtonHandlerData _mCodeButtonHandlerData;
    private final ProgramProperties _mProgramProperties;
    private final CheckJMRIObject _mCheckJMRIObject;

    private String _mSWDI_NormalInternalSensorOrig;
    private String _mSWDI_ReversedInternalSensorOrig;
    private String _mSWDI_ExternalTurnoutOrig;
    private int _mSWDI_CodingTimeInMillisecondsOrig;
    private boolean _mSWDI_FeedbackDifferentOrig;
    private CodeButtonHandlerData.TURNOUT_TYPE _mSWDI_GUITurnoutTypeOrig;
    private boolean _mSWDI_GUITurnoutLeftHandOrig;
    private boolean _mSWDI_GUICrossoverLeftHandOrig;

    private void initOrig() {
        _mSWDI_NormalInternalSensorOrig = _mCodeButtonHandlerData._mSWDI_NormalInternalSensor;
        _mSWDI_ReversedInternalSensorOrig = _mCodeButtonHandlerData._mSWDI_ReversedInternalSensor;
        _mSWDI_ExternalTurnoutOrig = _mCodeButtonHandlerData._mSWDI_ExternalTurnout;
        _mSWDI_CodingTimeInMillisecondsOrig = _mCodeButtonHandlerData._mSWDI_CodingTimeInMilliseconds;
        _mSWDI_FeedbackDifferentOrig = _mCodeButtonHandlerData._mSWDI_FeedbackDifferent;
        _mSWDI_GUITurnoutTypeOrig = _mCodeButtonHandlerData._mSWDI_GUITurnoutType;
        _mSWDI_GUITurnoutLeftHandOrig = _mCodeButtonHandlerData._mSWDI_GUITurnoutLeftHand;
        _mSWDI_GUICrossoverLeftHandOrig = _mCodeButtonHandlerData._mSWDI_GUICrossoverLeftHand;
    }
    private boolean dataChanged() {
        if (!_mSWDI_NormalInternalSensorOrig.equals(_mSWDI_NormalInternalSensor.getText())) return true;
        if (!_mSWDI_ReversedInternalSensorOrig.equals(_mSWDI_ReversedInternalSensor.getText())) return true;
        if (!_mSWDI_ExternalTurnoutOrig.equals(_mSWDI_ExternalTurnout.getSelectedItem())) return true;
        if (CommonSubs.getIntFromJTextFieldNoThrow(_mSWDI_CodingTimeInMilliseconds) != _mSWDI_CodingTimeInMillisecondsOrig) return true;
        if (_mSWDI_FeedbackDifferentOrig != _mSWDI_FeedbackDifferent.isSelected()) return true;
        if (_mSWDI_GUITurnoutTypeOrig != CodeButtonHandlerData.TURNOUT_TYPE.getTurnoutType(_mSWDI_GUITurnoutType)) return true;
        if (_mSWDI_GUITurnoutLeftHandOrig != _mSWDI_GUITurnoutLeftHand.isSelected()) return true;
        if (_mSWDI_GUICrossoverLeftHandOrig != _mSWDI_GUICrossoverLeftHand.isSelected()) return true;
        return false;
    }

    public FrmSWDI( AwtWindowProperties awtWindowProperties, CodeButtonHandlerData codeButtonHandlerData,
                    ProgramProperties programProperties, CheckJMRIObject checkJMRIObject) {
        super();
        initComponents();
        CommonSubs.addHelpMenu(this, "package.jmri.jmrit.ctc.CTC_frmSWDI", true);  // NOI18N
        _mAwtWindowProperties = awtWindowProperties;
        _mCodeButtonHandlerData = codeButtonHandlerData;
        _mProgramProperties = programProperties;
        _mCheckJMRIObject = checkJMRIObject;
        CommonSubs.numberButtonGroup(_mSWDI_GUITurnoutType);
        CommonSubs.setButtonSelected(_mSWDI_GUITurnoutType, _mCodeButtonHandlerData._mSWDI_GUITurnoutType.getInt());
        CommonSubs.setMillisecondsEdit(_mSWDI_CodingTimeInMilliseconds);
        _mSWDI_NormalInternalSensor.setText(_mCodeButtonHandlerData._mSWDI_NormalInternalSensor);
        _mSWDI_ReversedInternalSensor.setText(_mCodeButtonHandlerData._mSWDI_ReversedInternalSensor);
        CommonSubs.populateJComboBoxWithBeans(_mSWDI_ExternalTurnout, "Turnout", _mCodeButtonHandlerData._mSWDI_ExternalTurnout, true);
        _mSWDI_CodingTimeInMilliseconds.setText(Integer.toString(_mCodeButtonHandlerData._mSWDI_CodingTimeInMilliseconds));
        _mSWDI_FeedbackDifferent.setSelected(_mCodeButtonHandlerData._mSWDI_FeedbackDifferent);
        _mSWDI_GUITurnoutLeftHand.setSelected(_mCodeButtonHandlerData._mSWDI_GUITurnoutLeftHand);
        _mSWDI_GUICrossoverLeftHand.setSelected(_mCodeButtonHandlerData._mSWDI_GUICrossoverLeftHand);
        initOrig();
        _mAwtWindowProperties.setWindowState(this, FORM_PROPERTIES);
        this.getRootPane().setDefaultButton(_mSaveAndClose);
    }

    public static boolean dialogCodeButtonHandlerDataValid(CheckJMRIObject checkJMRIObject, CodeButtonHandlerData codeButtonHandlerData) {
        if (!codeButtonHandlerData._mSWDI_Enabled) return true; // Not enabled, can be no error!
//  Checks:
        if (ProjectsCommonSubs.isNullOrEmptyString(codeButtonHandlerData._mSWDI_NormalInternalSensor)) return false;
        if (ProjectsCommonSubs.isNullOrEmptyString(codeButtonHandlerData._mSWDI_ReversedInternalSensor)) return false;
        if (ProjectsCommonSubs.isNullOrEmptyString(codeButtonHandlerData._mSWDI_ExternalTurnout)) return false;
        return checkJMRIObject.validClassWithPrefix(PREFIX, codeButtonHandlerData);
    }

//  Validate all internal fields as much as possible:
    private ArrayList<String> formFieldsValid() {
        ArrayList<String> errors = new ArrayList<>();
//  Checks:
        CommonSubs.checkJTextFieldNotEmpty(_mSWDI_NormalInternalSensor, _mSWDI_NormalInternalSensorPrompt, errors);
        CommonSubs.checkJTextFieldNotEmpty(_mSWDI_ReversedInternalSensor, _mSWDI_ReversedInternalSensorPrompt, errors);
        CommonSubs.checkJComboBoxNotEmpty(_mSWDI_ExternalTurnout, _mSWDI_ActualTurnoutPrompt, errors);
        _mCheckJMRIObject.analyzeForm(PREFIX, this, errors);
        return errors;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")  // NOI18N
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        _mSWDI_GUITurnoutType = new javax.swing.ButtonGroup();
        _mSaveAndClose = new javax.swing.JButton();
        _mSWDI_NormalInternalSensorPrompt = new javax.swing.JLabel();
        _mSWDI_NormalInternalSensor = new javax.swing.JTextField();
        _mSWDI_ReversedInternalSensorPrompt = new javax.swing.JLabel();
        _mSWDI_ReversedInternalSensor = new javax.swing.JTextField();
        _mSWDI_ActualTurnoutPrompt = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        _mSWDI_CodingTimeInMilliseconds = new javax.swing.JFormattedTextField();
        _mSWDI_FeedbackDifferent = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        _mSWDI_GUITurnoutLeftHand = new javax.swing.JCheckBox();
        _mSWDI_GUICrossoverLeftHand = new javax.swing.JCheckBox();
        _mTurnout = new javax.swing.JRadioButton();
        _mCrossover = new javax.swing.JRadioButton();
        _mDoubleCrossover = new javax.swing.JRadioButton();
        _mSWDI_ExternalTurnout = new javax.swing.JComboBox<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle(Bundle.getMessage("TitleSWDI"));
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

        _mSWDI_NormalInternalSensorPrompt.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        _mSWDI_NormalInternalSensorPrompt.setText(Bundle.getMessage("LabelSWDINormal"));

        _mSWDI_ReversedInternalSensorPrompt.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        _mSWDI_ReversedInternalSensorPrompt.setText(Bundle.getMessage("LabelSWDIReverse"));

        _mSWDI_ActualTurnoutPrompt.setText(Bundle.getMessage("LabelSWDIToName"));

        jLabel19.setText(Bundle.getMessage("LabelSWDITime"));

        _mSWDI_CodingTimeInMilliseconds.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        _mSWDI_FeedbackDifferent.setText(" ");

        jLabel2.setText(Bundle.getMessage("LabelSWDIFeedback"));

        jButton2.setText(Bundle.getMessage("ButtonReapply"));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jLabel3.setText(Bundle.getMessage("InfoSWDIGUI"));

        _mSWDI_GUITurnoutLeftHand.setText(Bundle.getMessage("LabelSWDILeft"));

        _mSWDI_GUICrossoverLeftHand.setText(Bundle.getMessage("LabelSWDIAlso"));

        _mSWDI_GUITurnoutType.add(_mTurnout);
        _mTurnout.setText(Bundle.getMessage("LabelSWDITurnout"));
        _mTurnout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mTurnoutActionPerformed(evt);
            }
        });

        _mSWDI_GUITurnoutType.add(_mCrossover);
        _mCrossover.setText(Bundle.getMessage("LabelSWDIXOver"));
        _mCrossover.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mCrossoverActionPerformed(evt);
            }
        });

        _mSWDI_GUITurnoutType.add(_mDoubleCrossover);
        _mDoubleCrossover.setText(Bundle.getMessage("LabelSWDIDouble"));
        _mDoubleCrossover.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mDoubleCrossoverActionPerformed(evt);
            }
        });

        _mSWDI_ExternalTurnout.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(_mSWDI_ActualTurnoutPrompt)
                            .addComponent(jLabel19)
                            .addComponent(_mSWDI_ReversedInternalSensorPrompt, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(_mSWDI_NormalInternalSensorPrompt, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(_mSWDI_CodingTimeInMilliseconds, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(_mSWDI_NormalInternalSensor)
                                .addComponent(_mSWDI_ReversedInternalSensor)
                                .addComponent(_mSWDI_ExternalTurnout, 0, 119, Short.MAX_VALUE))
                            .addComponent(_mSWDI_FeedbackDifferent))
                        .addGap(83, 83, 83))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton2)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(_mCrossover)
                                    .addComponent(_mTurnout))
                                .addGap(41, 41, 41)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(_mSWDI_GUITurnoutLeftHand)
                                    .addComponent(_mSWDI_GUICrossoverLeftHand)))
                            .addComponent(_mDoubleCrossover)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(43, 43, 43)
                                .addComponent(_mSaveAndClose)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mSWDI_NormalInternalSensorPrompt, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(_mSWDI_NormalInternalSensor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mSWDI_ReversedInternalSensorPrompt)
                    .addComponent(_mSWDI_ReversedInternalSensor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mSWDI_ActualTurnoutPrompt)
                    .addComponent(_mSWDI_ExternalTurnout, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mSWDI_CodingTimeInMilliseconds, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel19))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(_mSWDI_FeedbackDifferent))
                .addGap(22, 22, 22)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(_mSWDI_GUITurnoutLeftHand)
                    .addComponent(_mTurnout))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mCrossover)
                    .addComponent(_mSWDI_GUICrossoverLeftHand))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(_mDoubleCrossover)
                .addGap(18, 18, 18)
                .addComponent(_mSaveAndClose)
                .addGap(38, 38, 38)
                .addComponent(jButton2)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void _mSaveAndCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mSaveAndCloseActionPerformed
        if (CommonSubs.missingFieldsErrorDialogDisplayed(this, formFieldsValid(), false)) {
            return; // Do not allow exit or transfer of data.
        }
        _mCodeButtonHandlerData._mSWDI_NormalInternalSensor = _mSWDI_NormalInternalSensor.getText();
        _mCodeButtonHandlerData._mSWDI_ReversedInternalSensor = _mSWDI_ReversedInternalSensor.getText();
        _mCodeButtonHandlerData._mSWDI_ExternalTurnout = (String) _mSWDI_ExternalTurnout.getSelectedItem();
        _mCodeButtonHandlerData._mSWDI_CodingTimeInMilliseconds = CommonSubs.getIntFromJTextFieldNoThrow(_mSWDI_CodingTimeInMilliseconds);
        _mCodeButtonHandlerData._mSWDI_FeedbackDifferent = _mSWDI_FeedbackDifferent.isSelected();
        _mCodeButtonHandlerData._mSWDI_GUITurnoutType = CodeButtonHandlerData.TURNOUT_TYPE.getTurnoutType(_mSWDI_GUITurnoutType);
        _mCodeButtonHandlerData._mSWDI_GUITurnoutLeftHand = _mSWDI_GUITurnoutLeftHand.isSelected();
        _mCodeButtonHandlerData._mSWDI_GUICrossoverLeftHand = _mSWDI_GUICrossoverLeftHand.isSelected();
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
        temp = CodeButtonHandlerDataRoutines.uECBHDWSD_SWDI(_mProgramProperties, temp);
        _mSWDI_NormalInternalSensor.setText(temp._mSWDI_NormalInternalSensor);
        _mSWDI_ReversedInternalSensor.setText(temp._mSWDI_ReversedInternalSensor);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void _mTurnoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mTurnoutActionPerformed
        _mSWDI_GUITurnoutLeftHand.setEnabled(true);
        _mSWDI_GUICrossoverLeftHand.setEnabled(false);
    }//GEN-LAST:event__mTurnoutActionPerformed

    private void _mCrossoverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mCrossoverActionPerformed
        _mSWDI_GUITurnoutLeftHand.setEnabled(true);
        _mSWDI_GUICrossoverLeftHand.setEnabled(true);
    }//GEN-LAST:event__mCrossoverActionPerformed

    private void _mDoubleCrossoverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mDoubleCrossoverActionPerformed
        _mSWDI_GUITurnoutLeftHand.setEnabled(false);
        _mSWDI_GUICrossoverLeftHand.setEnabled(false);
    }//GEN-LAST:event__mDoubleCrossoverActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton _mCrossover;
    private javax.swing.JRadioButton _mDoubleCrossover;
    private javax.swing.JLabel _mSWDI_ActualTurnoutPrompt;
    private javax.swing.JFormattedTextField _mSWDI_CodingTimeInMilliseconds;
    private javax.swing.JComboBox<String> _mSWDI_ExternalTurnout;
    private javax.swing.JCheckBox _mSWDI_FeedbackDifferent;
    private javax.swing.JCheckBox _mSWDI_GUICrossoverLeftHand;
    private javax.swing.JCheckBox _mSWDI_GUITurnoutLeftHand;
    private javax.swing.ButtonGroup _mSWDI_GUITurnoutType;
    private javax.swing.JTextField _mSWDI_NormalInternalSensor;
    private javax.swing.JLabel _mSWDI_NormalInternalSensorPrompt;
    private javax.swing.JTextField _mSWDI_ReversedInternalSensor;
    private javax.swing.JLabel _mSWDI_ReversedInternalSensorPrompt;
    private javax.swing.JButton _mSaveAndClose;
    private javax.swing.JRadioButton _mTurnout;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    // End of variables declaration//GEN-END:variables
}
