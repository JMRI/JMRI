package jmri.jmrit.ctc.editor.gui;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import jmri.jmrit.ctc.ctcserialdata.CTCSerialData;
import jmri.jmrit.ctc.ctcserialdata.CodeButtonHandlerData;
import jmri.jmrit.ctc.ctcserialdata.OtherData;
import jmri.jmrit.ctc.ctcserialdata.ProjectsCommonSubs;
import jmri.jmrit.ctc.ctcserialdata.TrafficLockingData;
import jmri.jmrit.ctc.editor.code.AwtWindowProperties;
import jmri.jmrit.ctc.editor.code.CheckJMRIObject;
import jmri.jmrit.ctc.editor.code.CommonSubs;
import jmri.jmrit.ctc.topology.Topology;
import jmri.jmrit.ctc.topology.TopologyInfo;

/**
 *
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 */
public class FrmTRL extends javax.swing.JFrame {

    /**
     * Creates new form DlgTRL
     */
    private static final String FORM_PROPERTIES = "DlgTRL";     // NOI18N
    private final AwtWindowProperties _mAwtWindowProperties;
    private boolean _mClosedNormally = false;
    public boolean closedNormally() { return _mClosedNormally; }
    private final CodeButtonHandlerData _mCodeButtonHandlerData;
    private final CTCSerialData _mCTCSerialData;
    private final CheckJMRIObject _mCheckJMRIObject;
    private final FrmMainForm _mMainForm;
    private Topology _mTopology;

    private void initOrig() {
    }
    private boolean dataChanged() {
        return false;
    }

    public FrmTRL(  AwtWindowProperties awtWindowProperties, CodeButtonHandlerData codeButtonHandlerData,
                    CTCSerialData ctcSerialData, CheckJMRIObject checkJMRIObject) {
        super();
        initComponents();
        CommonSubs.addHelpMenu(this, "package.jmri.jmrit.ctc.CTC_frmTRL", true);  // NOI18N
        _mMainForm = jmri.InstanceManager.getDefault(FrmMainForm.class);
        _mAwtWindowProperties = awtWindowProperties;
        _mCodeButtonHandlerData = codeButtonHandlerData;
        _mCTCSerialData = ctcSerialData;
        _mCheckJMRIObject = checkJMRIObject;
        initOrig();
        _mAwtWindowProperties.setWindowState(this, FORM_PROPERTIES);
        this.getRootPane().setDefaultButton(_mOK);
        updateRuleCounts();
        this.setTitle(Bundle.getMessage("TitleDlgTRL") + " " + codeButtonHandlerData.myShortStringNoComma());   // NOI18N
        ArrayList<String> listOfOSSectionOccupiedExternalSensors = getListOfExternalSensorsSlaved(codeButtonHandlerData, _mCTCSerialData.getCodeButtonHandlerDataArrayList());
        _mTopology = new Topology(_mCTCSerialData, listOfOSSectionOccupiedExternalSensors, Bundle.getMessage("TLE_Normal"), Bundle.getMessage("TLE_Reverse"));  // NOI18N
        boolean isMastSignalType = _mCTCSerialData.getOtherData()._mSignalSystemType == OtherData.SIGNAL_SYSTEM_TYPE.SIGNALMAST;
        boolean topologyAvailable = _mTopology.isTopologyAvailable() && isMastSignalType;
        _mAutoGenerate.setVisible(topologyAvailable);
        _mReverseLeftRight.setVisible(topologyAvailable);
        _mAutoGenerateWarning.setVisible(topologyAvailable);
    }

    private ArrayList<String> getListOfExternalSensorsSlaved(   CodeButtonHandlerData currentCodeButtonHandlerData,
                                                                ArrayList <CodeButtonHandlerData> codeButtonHandlerDataArrayList) {
        ArrayList<String> returnValue = new ArrayList<>();
        returnValue.add(currentCodeButtonHandlerData._mOSSectionOccupiedExternalSensor.getHandleName());    // Put ours in there at least.
        int currentUniqueID = currentCodeButtonHandlerData._mUniqueID;
        for (CodeButtonHandlerData codeButtonHandlerData : codeButtonHandlerDataArrayList) {
            if (currentCodeButtonHandlerData != codeButtonHandlerData    // Don't check ourselves, we've already put us in the list.
            && codeButtonHandlerData._mOSSectionSwitchSlavedToUniqueID != CodeButtonHandlerData.SWITCH_NOT_SLAVED   // It's referencing someone else:
            && currentUniqueID == codeButtonHandlerData._mOSSectionSwitchSlavedToUniqueID) {  // And it's referening "us"/
                returnValue.add(codeButtonHandlerData._mOSSectionOccupiedExternalSensor.getHandleName());
            }
        }
        return returnValue;
    }

    public static boolean dialogCodeButtonHandlerDataValid(CheckJMRIObject checkJMRIObject, CodeButtonHandlerData codeButtonHandlerData) {
        if (!valid(checkJMRIObject, codeButtonHandlerData._mTRL_LeftTrafficLockingRules)) return false;
        if (!valid(checkJMRIObject, codeButtonHandlerData._mTRL_RightTrafficLockingRules)) return false;
        return true;
    }

    private void updateRuleCounts() {
        _mLeftNumberOfRules.setText(Bundle.getMessage("InfoDlgTRLRules") + " " + Integer.toString(_mCodeButtonHandlerData._mTRL_LeftTrafficLockingRules.size()));   // NOI18N
        _mRightNumberOfRules.setText(Bundle.getMessage("InfoDlgTRLRules") + " " + Integer.toString(_mCodeButtonHandlerData._mTRL_RightTrafficLockingRules.size()));      // NOI18N
        _mLeftNumberOfRulesPrompt.setForeground(valid(_mCheckJMRIObject, _mCodeButtonHandlerData._mTRL_LeftTrafficLockingRules) ? Color.black : Color.red);
        _mRightNumberOfRulesPrompt.setForeground(valid(_mCheckJMRIObject, _mCodeButtonHandlerData._mTRL_RightTrafficLockingRules) ? Color.black : Color.red);
    }

    private static boolean valid(CheckJMRIObject checkJMRIObject, ArrayList<TrafficLockingData> trafficLockingRules) {
        for (TrafficLockingData trafficLockingRule : trafficLockingRules) {
            if (!checkJMRIObject.validClass(trafficLockingRule)) return false; // Error
        }
        return true;    // All valid
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        _mEditLeftTrafficLockingRules = new javax.swing.JButton();
        _mEditRightTrafficLockingRules = new javax.swing.JButton();
        _mLeftNumberOfRulesPrompt = new javax.swing.JLabel();
        _mRightNumberOfRulesPrompt = new javax.swing.JLabel();
        _mOK = new javax.swing.JButton();
        _mLeftNumberOfRules = new javax.swing.JLabel();
        _mRightNumberOfRules = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        _mAutoGenerate = new javax.swing.JButton();
        _mReverseLeftRight = new javax.swing.JButton();
        _mAutoGenerateWarning = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(Bundle.getMessage("TitleDlgTRL"));
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        _mEditLeftTrafficLockingRules.setText(Bundle.getMessage("LabelDlgTRLEdit"));
        _mEditLeftTrafficLockingRules.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mEditLeftTrafficLockingRulesActionPerformed(evt);
            }
        });

        _mEditRightTrafficLockingRules.setText(Bundle.getMessage("LabelDlgTRLEdit"));
        _mEditRightTrafficLockingRules.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mEditRightTrafficLockingRulesActionPerformed(evt);
            }
        });

        _mLeftNumberOfRulesPrompt.setText(Bundle.getMessage("LabelDlgTRLLeft"));

        _mRightNumberOfRulesPrompt.setText(Bundle.getMessage("LabelDlgTRLRight"));

        _mOK.setText(Bundle.getMessage("ButtonOK"));
        _mOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mOKActionPerformed(evt);
            }
        });

        _mLeftNumberOfRules.setText(Bundle.getMessage("InfoDlgTRLRulesQuestion"));

        _mRightNumberOfRules.setText(Bundle.getMessage("InfoDlgTRLRulesQuestion"));

        jLabel10.setText(Bundle.getMessage("InfoDlgTRLNote2"));

        jLabel4.setText(Bundle.getMessage("InfoDlgTRLNote1"));

        jLabel11.setText(Bundle.getMessage("InfoDlgTRLNote3"));

        _mAutoGenerate.setText(Bundle.getMessage("LabelDlgTRLAutoGenerate"));
        _mAutoGenerate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mAutoGenerateActionPerformed(evt);
            }
        });

        _mReverseLeftRight.setText(Bundle.getMessage("LabelDlgTRLReverseLeftRight"));
        _mReverseLeftRight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mReverseLeftRightActionPerformed(evt);
            }
        });

        _mAutoGenerateWarning.setText(Bundle.getMessage("LabelDlgTRLAutoGenerateWarning"));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(49, 49, 49)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(_mLeftNumberOfRulesPrompt)
                            .addComponent(_mRightNumberOfRulesPrompt))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(_mEditRightTrafficLockingRules)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(_mRightNumberOfRules, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(_mReverseLeftRight))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(_mEditLeftTrafficLockingRules)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(_mLeftNumberOfRules, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(_mAutoGenerateWarning)
                                    .addComponent(_mAutoGenerate, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel4))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel10))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel11))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(164, 164, 164)
                        .addComponent(_mOK)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(_mAutoGenerateWarning)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mLeftNumberOfRulesPrompt)
                    .addComponent(_mEditLeftTrafficLockingRules)
                    .addComponent(_mLeftNumberOfRules)
                    .addComponent(_mAutoGenerate))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mEditRightTrafficLockingRules)
                    .addComponent(_mRightNumberOfRulesPrompt)
                    .addComponent(_mRightNumberOfRules)
                    .addComponent(_mReverseLeftRight))
                .addGap(13, 13, 13)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel11)
                .addGap(18, 18, 18)
                .addComponent(_mOK)
                .addContainerGap(31, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        _mAwtWindowProperties.saveWindowState(this, FORM_PROPERTIES);
        if (CommonSubs.allowClose(this, dataChanged())) dispose();
    }//GEN-LAST:event_formWindowClosing

    private void _mEditLeftTrafficLockingRulesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mEditLeftTrafficLockingRulesActionPerformed
        if (_mMainForm._mTRL_RulesFormOpen) return;
        _mMainForm._mTRL_RulesFormOpen = true;
        FrmTRL_Rules dialog = new FrmTRL_Rules( _mAwtWindowProperties, _mCodeButtonHandlerData,
                                                true, _mCTCSerialData, _mCheckJMRIObject);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (dialog.closedNormally()) {
                    _mClosedNormally = true;
                    updateRuleCounts();
                }
                _mMainForm._mTRL_RulesFormOpen = false;
            }
        });
        dialog.setVisible(true);  // MUST BE AFTER "addWindowListener"!  BUG IN AWT/SWING!
    }//GEN-LAST:event__mEditLeftTrafficLockingRulesActionPerformed

    private void _mEditRightTrafficLockingRulesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mEditRightTrafficLockingRulesActionPerformed
        if (_mMainForm._mTRL_RulesFormOpen) return;
        _mMainForm._mTRL_RulesFormOpen = true;
        FrmTRL_Rules dialog = new FrmTRL_Rules( _mAwtWindowProperties, _mCodeButtonHandlerData,
                                                false, _mCTCSerialData, _mCheckJMRIObject);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (dialog.closedNormally()) {
                    _mClosedNormally = true;
                    updateRuleCounts();
                }
                _mMainForm._mTRL_RulesFormOpen = false;
            }
        });
        dialog.setVisible(true);  // MUST BE AFTER "addWindowListener"!  BUG IN AWT/SWING!
    }//GEN-LAST:event__mEditRightTrafficLockingRulesActionPerformed

    private void _mOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mOKActionPerformed
        dispose();
    }//GEN-LAST:event__mOKActionPerformed

    private void _mReverseLeftRightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mReverseLeftRightActionPerformed
        ArrayList<TrafficLockingData> blah = _mCodeButtonHandlerData._mTRL_LeftTrafficLockingRules;
        _mCodeButtonHandlerData._mTRL_LeftTrafficLockingRules = _mCodeButtonHandlerData._mTRL_RightTrafficLockingRules;
        _mCodeButtonHandlerData._mTRL_RightTrafficLockingRules = blah;
        updateRuleCounts();
    }//GEN-LAST:event__mReverseLeftRightActionPerformed

    private void _mAutoGenerateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mAutoGenerateActionPerformed

        ArrayList<TopologyInfo> topologyInfosArrayList = _mTopology.getTrafficLockingRules(true);        // Left traffic.
        _mCodeButtonHandlerData._mTRL_LeftTrafficLockingRules.clear();
        for (int index = 0; index < topologyInfosArrayList.size(); index++) {
            TopologyInfo topologyInfo = topologyInfosArrayList.get(index);
            TrafficLockingData trafficLockingData = new TrafficLockingData(index + 1, topologyInfo.getDestinationSignalMast(), topologyInfo);
            _mCodeButtonHandlerData._mTRL_LeftTrafficLockingRules.add(trafficLockingData);
        }

        topologyInfosArrayList = _mTopology.getTrafficLockingRules(false);        // Right traffic.
        _mCodeButtonHandlerData._mTRL_RightTrafficLockingRules.clear();
        for (int index = 0; index < topologyInfosArrayList.size(); index++) {
            TopologyInfo topologyInfo = topologyInfosArrayList.get(index);
            TrafficLockingData trafficLockingData = new TrafficLockingData(index + 1, topologyInfo.getDestinationSignalMast(), topologyInfo);
            _mCodeButtonHandlerData._mTRL_RightTrafficLockingRules.add(trafficLockingData);
        }

        updateRuleCounts();
    }//GEN-LAST:event__mAutoGenerateActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton _mAutoGenerate;
    private javax.swing.JLabel _mAutoGenerateWarning;
    private javax.swing.JButton _mEditLeftTrafficLockingRules;
    private javax.swing.JButton _mEditRightTrafficLockingRules;
    private javax.swing.JLabel _mLeftNumberOfRules;
    private javax.swing.JLabel _mLeftNumberOfRulesPrompt;
    private javax.swing.JButton _mOK;
    private javax.swing.JButton _mReverseLeftRight;
    private javax.swing.JLabel _mRightNumberOfRules;
    private javax.swing.JLabel _mRightNumberOfRulesPrompt;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel4;
    // End of variables declaration//GEN-END:variables
}
