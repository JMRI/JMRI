package gui;

import code.AwtWindowProperties;
import code.CheckJMRIObject;
import code.CodeButtonHandlerDataRoutines;
import code.CommonSubs;
import code.ProgramProperties;
import java.util.ArrayList;
import jmri.jmrit.ctcserialdata.CTCSerialData;
import jmri.jmrit.ctcserialdata.CodeButtonHandlerData;
import jmri.jmrit.ctcserialdata.ProjectsCommonSubs;

/**
 *
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 */
public class DlgCB extends javax.swing.JDialog {

    /**
     * 
     */
    private static final String FORM_PROPERTIES = "DlgCB";
    private static final String PREFIX = "_mOS";
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
        if (!_mOSSectionOccupiedExternalSensorOrig.equals(_mOSSectionOccupiedExternalSensor.getText())) return true;
        if (!_mOSSectionOccupiedExternalSensor2Orig.equals(_mOSSectionOccupiedExternalSensor2.getText())) return true;
        if (_mOSSectionSwitchSlavedToUniqueIDIndexOrig != _mOSSectionSwitchSlavedToUniqueID.getSelectedIndex()) return true;
        if (CommonSubs.getIntFromJTextFieldNoThrow(_mCodeButtonDelayTime) != _mCodeButtonDelayTimeOrig) return true;
        return false;
    }

    public DlgCB(   java.awt.Frame parent, boolean modal,
                    AwtWindowProperties awtWindowProperties, CodeButtonHandlerData codeButtonHandlerData, ProgramProperties programProperties,
                    CTCSerialData ctcSerialData, CheckJMRIObject checkJMRIObject) {
        super(parent, modal);
        initComponents();
        _mAwtWindowProperties = awtWindowProperties;
        _mCodeButtonHandlerData = codeButtonHandlerData;
        _mProgramProperties = programProperties;
        _mCTCSerialData = ctcSerialData;
        _mCheckJMRIObject = checkJMRIObject;
        _mArrayListOfSelectableOSSectionUniqueIDs = CommonSubs.getArrayListOfSelectableOSSectionUniqueIDs(_mCTCSerialData.getCodeButtonHandlerDataArrayList());
        _mCodeButtonInternalSensor.setText(_mCodeButtonHandlerData._mCodeButtonInternalSensor);
        _mOSSectionOccupiedExternalSensor.setText(_mCodeButtonHandlerData._mOSSectionOccupiedExternalSensor);
        _mOSSectionOccupiedExternalSensor2.setText(_mCodeButtonHandlerData._mOSSectionOccupiedExternalSensor2);
        CommonSubs.populateJComboBoxWithColumnDescriptionsAndSelectViaUniqueID(_mOSSectionSwitchSlavedToUniqueID, _mCTCSerialData, _mCodeButtonHandlerData._mOSSectionSwitchSlavedToUniqueID);
        CommonSubs.setMillisecondsEdit(_mCodeButtonDelayTime);
        _mCodeButtonDelayTime.setText(Integer.toString(_mCodeButtonHandlerData._mCodeButtonDelayTime));
        initOrig();
        _mAwtWindowProperties.setWindowState((java.awt.Window)this, FORM_PROPERTIES); 
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
        CommonSubs.checkJTextFieldNotEmpty(_mOSSectionOccupiedExternalSensor, _mOSSectionOccupiedExternalSensorPrompt, errors);
        _mCheckJMRIObject.analyzeForm(PREFIX, this, errors);
        return errors;
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        _mCodeButtonInternalSensorPrompt = new javax.swing.JLabel();
        _mCodeButtonInternalSensor = new javax.swing.JTextField();
        _mSaveAndClose = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        _mOSSectionOccupiedExternalSensorPrompt = new javax.swing.JLabel();
        _mOSSectionOccupiedExternalSensor = new javax.swing.JTextField();
        _mOSSectionSwitchSlavedToUniqueID = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        _mCodeButtonDelayTime = new javax.swing.JFormattedTextField();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        _mOSSectionOccupiedExternalSensor2 = new javax.swing.JTextField();
        _mOSSectionOccupiedExternalSensorPrompt1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Edit Code Button");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        _mCodeButtonInternalSensorPrompt.setText("Code button sensor:");

        _mSaveAndClose.setText("Save and close");
        _mSaveAndClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mSaveAndCloseActionPerformed(evt);
            }
        });

        jButton2.setText("Reapply patterns - this form ONLY!");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        _mOSSectionOccupiedExternalSensorPrompt.setText("Primary O.S. section occupied sensor:");

        _mOSSectionSwitchSlavedToUniqueID.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mOSSectionSwitchSlavedToUniqueIDActionPerformed(evt);
            }
        });

        jLabel3.setText("Switch slaved to O.S. section #:");

        jLabel4.setText("Leave this blank if this is a normal O.S. section");

        jLabel9.setText("Selecting self forces blank!");

        _mCodeButtonDelayTime.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        jLabel5.setText("No code button delay in milliseconds:");

        jLabel6.setText("Enter 0 for normal code button");

        jLabel7.setText("Enter >0 for tower O.S. section delay (no code button)");

        jLabel1.setText("Required");

        _mOSSectionOccupiedExternalSensorPrompt1.setText("Secondary O.S. section occupied sensor:");

        jLabel2.setText("Optional");

        jLabel8.setText("Locks everything");

        jLabel10.setText("Prevents turnout change, turnout lock");

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
                                    .addComponent(_mCodeButtonInternalSensorPrompt))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(_mCodeButtonInternalSensor, javax.swing.GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE)
                                            .addComponent(_mOSSectionOccupiedExternalSensor))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel1))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(_mOSSectionOccupiedExternalSensor2, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel2))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(10, 10, 10)
                                        .addComponent(jLabel10))))
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
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel7)
                                    .addComponent(jLabel6)
                                    .addComponent(_mCodeButtonDelayTime, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(212, 212, 212)
                        .addComponent(_mSaveAndClose))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(170, 170, 170)
                        .addComponent(jButton2)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel8)
                .addGap(134, 134, 134))
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
                    .addComponent(_mOSSectionOccupiedExternalSensor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(_mOSSectionOccupiedExternalSensorPrompt)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mOSSectionOccupiedExternalSensor2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(_mOSSectionOccupiedExternalSensorPrompt1)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel10)
                .addGap(18, 18, 18)
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
                .addGap(18, 18, 18)
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
        _mCodeButtonHandlerData._mOSSectionOccupiedExternalSensor = _mOSSectionOccupiedExternalSensor.getText();
        _mCodeButtonHandlerData._mOSSectionOccupiedExternalSensor2 = _mOSSectionOccupiedExternalSensor2.getText();
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
        _mOSSectionOccupiedExternalSensor.setText(temp._mOSSectionOccupiedExternalSensor);
        _mOSSectionOccupiedExternalSensor2.setText(temp._mOSSectionOccupiedExternalSensor2);
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
    private javax.swing.JTextField _mOSSectionOccupiedExternalSensor;
    private javax.swing.JTextField _mOSSectionOccupiedExternalSensor2;
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
