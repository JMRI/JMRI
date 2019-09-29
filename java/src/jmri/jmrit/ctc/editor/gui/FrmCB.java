package jmri.jmrit.ctc.editor.gui;

import jmri.jmrit.ctc.editor.code.AwtWindowProperties;
import jmri.jmrit.ctc.editor.code.CheckJMRIObject;
import jmri.jmrit.ctc.editor.code.CodeButtonHandlerDataRoutines;
import jmri.jmrit.ctc.editor.code.CommonSubs;
import jmri.jmrit.ctc.editor.code.ProgramProperties;
import java.util.ArrayList;
import jmri.jmrit.ctc.ctcserialdata.CTCSerialData;
import jmri.jmrit.ctc.ctcserialdata.CodeButtonHandlerData;
import jmri.jmrit.ctc.ctcserialdata.ProjectsCommonSubs;

/**
 *
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 */
public class FrmCB extends javax.swing.JFrame {

    /**
     *
     */
    private static final String FORM_PROPERTIES = "DlgCB";  // NOI18N
    private static final String PREFIX = "_mOS";            // NOI18N
    private final AwtWindowProperties _mAwtWindowProperties;
    private boolean _mClosedNormally = false;
    public boolean closedNormally() { return _mClosedNormally; }
    private final CodeButtonHandlerData _mCodeButtonHandlerData;
    private final ProgramProperties _mProgramProperties;
    private final CTCSerialData _mCTCSerialData;
    private final CheckJMRIObject _mCheckJMRIObject;
    private final ArrayList<Integer> _mArrayListOfSelectableOSSectionUniqueIDs;

    private String _mCodeButtonInternalSensorOrig;
    private String _mOSSectionOccupiedExternalSensorOrig;
    private String _mOSSectionOccupiedExternalSensor2Orig;
    private int _mOSSectionSwitchSlavedToUniqueIDIndexOrig;
    private int _mCodeButtonDelayTimeOrig;
    private void initOrig() {
        _mCodeButtonInternalSensorOrig = _mCodeButtonHandlerData._mCodeButtonInternalSensor;
        _mOSSectionOccupiedExternalSensorOrig = _mCodeButtonHandlerData._mOSSectionOccupiedExternalSensor;
        _mOSSectionOccupiedExternalSensor2Orig = _mCodeButtonHandlerData._mOSSectionOccupiedExternalSensor2;
        _mOSSectionSwitchSlavedToUniqueIDIndexOrig = _mOSSectionSwitchSlavedToUniqueID.getSelectedIndex();
        _mCodeButtonDelayTimeOrig = _mCodeButtonHandlerData._mCodeButtonDelayTime;
    }
    private boolean dataChanged() {
        if (!_mCodeButtonInternalSensorOrig.equals(_mCodeButtonInternalSensor.getText())) return true;
        if (!_mOSSectionOccupiedExternalSensorOrig.equals(_mOSSectionOccupiedExternalSensor.getSelectedItem())) return true;
        if (!_mOSSectionOccupiedExternalSensor2Orig.equals(_mOSSectionOccupiedExternalSensor2.getSelectedItem())) return true;
        if (_mOSSectionSwitchSlavedToUniqueIDIndexOrig != _mOSSectionSwitchSlavedToUniqueID.getSelectedIndex()) return true;
        if (CommonSubs.getIntFromJTextFieldNoThrow(_mCodeButtonDelayTime) != _mCodeButtonDelayTimeOrig) return true;
        return false;
    }

    public FrmCB(   AwtWindowProperties awtWindowProperties, CodeButtonHandlerData codeButtonHandlerData, ProgramProperties programProperties,
                    CTCSerialData ctcSerialData, CheckJMRIObject checkJMRIObject) {
        super();
        initComponents();
        CommonSubs.addHelpMenu(this, "package.jmri.jmrit.ctc.CTC_frmCB", true);  // NOI18N
        _mAwtWindowProperties = awtWindowProperties;
        _mCodeButtonHandlerData = codeButtonHandlerData;
        _mProgramProperties = programProperties;
        _mCTCSerialData = ctcSerialData;
        _mCheckJMRIObject = checkJMRIObject;
        _mArrayListOfSelectableOSSectionUniqueIDs = CommonSubs.getArrayListOfSelectableOSSectionUniqueIDs(_mCTCSerialData.getCodeButtonHandlerDataArrayList());
        _mCodeButtonInternalSensor.setText(_mCodeButtonHandlerData._mCodeButtonInternalSensor);
        CommonSubs.populateJComboBoxWithBeans(_mOSSectionOccupiedExternalSensor, "Sensor", _mCodeButtonHandlerData._mOSSectionOccupiedExternalSensor, true);   // NOI18N
        CommonSubs.populateJComboBoxWithBeans(_mOSSectionOccupiedExternalSensor2, "Sensor", _mCodeButtonHandlerData._mOSSectionOccupiedExternalSensor2, true);  // NOI18N
        CommonSubs.populateJComboBoxWithColumnDescriptionsAndSelectViaUniqueID(_mOSSectionSwitchSlavedToUniqueID, _mCTCSerialData, _mCodeButtonHandlerData._mOSSectionSwitchSlavedToUniqueID);
        CommonSubs.setMillisecondsEdit(_mCodeButtonDelayTime);
        _mCodeButtonDelayTime.setText(Integer.toString(_mCodeButtonHandlerData._mCodeButtonDelayTime));
        initOrig();
        _mAwtWindowProperties.setWindowState(this, FORM_PROPERTIES);
        this.getRootPane().setDefaultButton(_mSaveAndClose);
    }

    public static boolean dialogCodeButtonHandlerDataValid(CheckJMRIObject checkJMRIObject, CodeButtonHandlerData codeButtonHandlerData) {
//  Checks:
        if (ProjectsCommonSubs.isNullOrEmptyString(codeButtonHandlerData._mCodeButtonInternalSensor)) return false;
        if (ProjectsCommonSubs.isNullOrEmptyString(codeButtonHandlerData._mOSSectionOccupiedExternalSensor)) return false;
        return checkJMRIObject.validClassWithPrefix(PREFIX, codeButtonHandlerData);
    }

//  Validate all internal fields as much as possible:
    private ArrayList<String> formFieldsValid() {
        ArrayList<String> errors = new ArrayList<>();
//  Checks:
        CommonSubs.checkJTextFieldNotEmpty(_mCodeButtonInternalSensor, _mCodeButtonInternalSensorPrompt, errors);
        CommonSubs.checkJComboBoxNotEmpty(_mOSSectionOccupiedExternalSensor, _mOSSectionOccupiedExternalSensorPrompt, errors);
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

        _mCodeButtonInternalSensorPrompt = new javax.swing.JLabel();
        _mCodeButtonInternalSensor = new javax.swing.JTextField();
        _mSaveAndClose = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        _mOSSectionOccupiedExternalSensorPrompt = new javax.swing.JLabel();
        _mOSSectionSwitchSlavedToUniqueID = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        _mCodeButtonDelayTime = new javax.swing.JFormattedTextField();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        _mOSSectionOccupiedExternalSensorPrompt1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        _mOSSectionOccupiedExternalSensor = new javax.swing.JComboBox<>();
        _mOSSectionOccupiedExternalSensor2 = new javax.swing.JComboBox<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle(Bundle.getMessage("TitleDlgCB"));
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        _mCodeButtonInternalSensorPrompt.setText(Bundle.getMessage("LabelDlgCBSensor"));

        _mSaveAndClose.setText(Bundle.getMessage("ButtonSaveClose"));
        _mSaveAndClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mSaveAndCloseActionPerformed(evt);
            }
        });

        jButton2.setText(Bundle.getMessage("ButtonReapply"));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        _mOSSectionOccupiedExternalSensorPrompt.setText(Bundle.getMessage("LabelDlgCBPriSensor"));

        _mOSSectionSwitchSlavedToUniqueID.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mOSSectionSwitchSlavedToUniqueIDActionPerformed(evt);
            }
        });

        jLabel3.setText(Bundle.getMessage("LabelDlgCBSwitch"));

        jLabel4.setText(Bundle.getMessage("InfoDlgCBNormal"));

        jLabel9.setText(Bundle.getMessage("InfoBlank"));

        _mCodeButtonDelayTime.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        jLabel5.setText(Bundle.getMessage("LabelDlgCBDelay"));

        jLabel6.setText(Bundle.getMessage("InfoDlgCBZero"));

        jLabel7.setText(Bundle.getMessage("InfoDlgCBDelay"));

        jLabel1.setText(Bundle.getMessage("InfoRequired"));

        _mOSSectionOccupiedExternalSensorPrompt1.setText(Bundle.getMessage("LabelDlgCBSecSensor"));

        jLabel2.setText(Bundle.getMessage("InfoOptional"));

        jLabel8.setText(Bundle.getMessage("InfoDlgCBLockAll"));

        jLabel10.setText(Bundle.getMessage("InfoDlgCBLockTO"));

        _mOSSectionOccupiedExternalSensor.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        _mOSSectionOccupiedExternalSensor2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(5, 5, 5)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(_mOSSectionOccupiedExternalSensorPrompt1)
                                    .addComponent(_mOSSectionOccupiedExternalSensorPrompt)
                                    .addComponent(_mCodeButtonInternalSensorPrompt)
                                    .addComponent(jLabel8)
                                    .addComponent(jLabel10))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(_mOSSectionOccupiedExternalSensor2, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(_mCodeButtonInternalSensor, javax.swing.GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE)
                                    .addComponent(_mOSSectionOccupiedExternalSensor, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING)))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel4)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(_mOSSectionSwitchSlavedToUniqueID, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel9))))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addGap(35, 35, 35)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel7)
                                    .addComponent(jLabel6)
                                    .addComponent(_mCodeButtonDelayTime, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(170, 170, 170)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(_mSaveAndClose)
                            .addComponent(jButton2))))
                .addContainerGap(125, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mCodeButtonInternalSensorPrompt)
                    .addComponent(_mCodeButtonInternalSensor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mOSSectionOccupiedExternalSensorPrompt)
                    .addComponent(jLabel1)
                    .addComponent(_mOSSectionOccupiedExternalSensor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel8)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mOSSectionOccupiedExternalSensorPrompt1)
                    .addComponent(jLabel2)
                    .addComponent(_mOSSectionOccupiedExternalSensor2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel10)
                .addGap(26, 26, 26)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mOSSectionSwitchSlavedToUniqueID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mCodeButtonDelayTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7)
                .addGap(29, 29, 29)
                .addComponent(_mSaveAndClose)
                .addGap(18, 18, 18)
                .addComponent(jButton2)
                .addGap(19, 19, 19))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void _mSaveAndCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mSaveAndCloseActionPerformed
        if (CommonSubs.missingFieldsErrorDialogDisplayed(this, formFieldsValid(), false)) {
            return; // Do not allow exit or transfer of data.
        }
        _mCodeButtonHandlerData._mCodeButtonInternalSensor = _mCodeButtonInternalSensor.getText();
        _mCodeButtonHandlerData._mOSSectionOccupiedExternalSensor = (String) _mOSSectionOccupiedExternalSensor.getSelectedItem();
        _mCodeButtonHandlerData._mOSSectionOccupiedExternalSensor2 = (String) _mOSSectionOccupiedExternalSensor2.getSelectedItem();
        int selectedIndex = _mOSSectionSwitchSlavedToUniqueID.getSelectedIndex();
        if (selectedIndex > 0) { // None and skip blank entry
            _mCodeButtonHandlerData._mOSSectionSwitchSlavedToUniqueID = _mArrayListOfSelectableOSSectionUniqueIDs.get(selectedIndex - 1);  // Correct for blank entry
        } else if (selectedIndex == 0) { // Blank entry:
            _mCodeButtonHandlerData._mOSSectionSwitchSlavedToUniqueID = CodeButtonHandlerData.SWITCH_NOT_SLAVED;
        }
        _mCodeButtonHandlerData._mCodeButtonDelayTime = CommonSubs.getIntFromJTextFieldNoThrow(_mCodeButtonDelayTime);
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
        temp = CodeButtonHandlerDataRoutines.uECBHDWSD_CodeButton(_mProgramProperties, temp);
        _mCodeButtonInternalSensor.setText(temp._mCodeButtonInternalSensor);
        _mOSSectionOccupiedExternalSensor.setSelectedItem(temp._mOSSectionOccupiedExternalSensor);
        _mOSSectionOccupiedExternalSensor2.setSelectedItem(temp._mOSSectionOccupiedExternalSensor2);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void _mOSSectionSwitchSlavedToUniqueIDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mOSSectionSwitchSlavedToUniqueIDActionPerformed
//  Verify user didn't select "self", since I don't want to screw up array indexes by eliminating it:
        int selectedIndex = _mOSSectionSwitchSlavedToUniqueID.getSelectedIndex();
        if (selectedIndex > 0) { // None and skip blank entry
            int osSectionSelectedUniqueID = _mArrayListOfSelectableOSSectionUniqueIDs.get(selectedIndex - 1);  // Correct for blank entry
            if (osSectionSelectedUniqueID == _mCodeButtonHandlerData._mUniqueID) {
                _mOSSectionSwitchSlavedToUniqueID.setSelectedIndex(0); // Back to blank!
            }
        }
    }//GEN-LAST:event__mOSSectionSwitchSlavedToUniqueIDActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JFormattedTextField _mCodeButtonDelayTime;
    private javax.swing.JTextField _mCodeButtonInternalSensor;
    private javax.swing.JLabel _mCodeButtonInternalSensorPrompt;
    private javax.swing.JComboBox<String> _mOSSectionOccupiedExternalSensor;
    private javax.swing.JComboBox<String> _mOSSectionOccupiedExternalSensor2;
    private javax.swing.JLabel _mOSSectionOccupiedExternalSensorPrompt;
    private javax.swing.JLabel _mOSSectionOccupiedExternalSensorPrompt1;
    private javax.swing.JComboBox<String> _mOSSectionSwitchSlavedToUniqueID;
    private javax.swing.JButton _mSaveAndClose;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    // End of variables declaration//GEN-END:variables
}
