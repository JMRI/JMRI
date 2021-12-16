package jmri.jmrit.ctc.editor.gui;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;

import jmri.jmrit.ctc.NBHSensor;
import jmri.jmrit.ctc.editor.code.AwtWindowProperties;
import jmri.jmrit.ctc.editor.code.CheckJMRIObject;
import jmri.jmrit.ctc.editor.code.CommonSubs;
import jmri.jmrit.ctc.ctcserialdata.CTCSerialData;
import jmri.jmrit.ctc.ctcserialdata.CodeButtonHandlerData;
import jmri.jmrit.ctc.ctcserialdata.ProjectsCommonSubs;
import jmri.jmrit.ctc.ctcserialdata.TrafficLockingData;

/**
 * Maintain the set of traffic locking rules.  There is an east set and a west set
 * in CodeButtonHandlerData.
 * <p>
 * When the form is invoked, trafficLockingRules points to either the east set or the west set.
 * The contents of the set is then loaded into _mTrafficLockingModel for display by JList.
 * When changes are made, the related entries in the model are updated.
 * <p>
 * When the save and close button is pressed, the model contents replace the contents of
 * the related traffic locking rules ArrayList in the current CodeButtonHandlerData object.
 *
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 */
public class FrmTRL_Rules extends javax.swing.JFrame {

    /**
     * Creates new form DlgTRL_Rules
     */
    private static final String FORM_PROPERTIES = "DlgTRL_Rules";   // NOI18N
    private final AwtWindowProperties _mAwtWindowProperties;
    private boolean _mClosedNormally = false;
    public boolean closedNormally() { return _mClosedNormally; }
    private final CodeButtonHandlerData _mCodeButtonHandlerData;
    private final boolean _mIsLeftTraffic;
    private final CTCSerialData _mCTCSerialData;
    private final CheckJMRIObject _mCheckJMRIObject;
    private final ArrayList<Integer> _mArrayListOfSelectableOSSectionUniqueIDs;
    private boolean _mAddNewPressed;

    private final DefaultListModel<TrafficLockingData> _mTrafficLockingModel;
    private final ArrayList<TrafficLockingData> _mTrafficLockingRulesOrig = new ArrayList<>();

    private void initOrig() {
        for (int index = 0; index < _mTrafficLockingModel.size(); index++) {
            _mTrafficLockingRulesOrig.add(_mTrafficLockingModel.get(index));
        }
    }

    private boolean dataChanged() {
        int ruleListSize = _mTrafficLockingModel.size();
        if (ruleListSize != _mTrafficLockingRulesOrig.size()) return true;
        for (int index = 0; index < ruleListSize; index++) {
            if (!_mTrafficLockingModel.get(index).equals(_mTrafficLockingRulesOrig.get(index))) return true;
        }
        return false;
    }

    public FrmTRL_Rules(AwtWindowProperties awtWindowProperties, CodeButtonHandlerData codeButtonHandlerData,
                        boolean isLeftTraffic,
                        CTCSerialData ctcSerialData, CheckJMRIObject checkJMRIObject) {
        super();
        initComponents();
        CommonSubs.addHelpMenu(this, "package.jmri.jmrit.ctc.CTC_frmTRL", true);  // NOI18N
        _mAwtWindowProperties = awtWindowProperties;
        _mCodeButtonHandlerData = codeButtonHandlerData;
        _mIsLeftTraffic = isLeftTraffic;
        _mCTCSerialData = ctcSerialData;
        _mCheckJMRIObject = checkJMRIObject;
        _mArrayListOfSelectableOSSectionUniqueIDs = CommonSubs.getArrayListOfSelectableOSSectionUniqueIDs(_mCTCSerialData.getCodeButtonHandlerDataArrayList());
        String identifier = codeButtonHandlerData.myShortStringNoComma();

        ArrayList<TrafficLockingData> trafficLockingRules;
        if (isLeftTraffic) {
            this.setTitle(Bundle.getMessage("TitleDlgTRLRulesLeft") + " " + identifier);    // NOI18N
            _mRulesInfo.setText(Bundle.getMessage("LabelDlgTRLRulesLeftInfo"));             // NOI18N
            trafficLockingRules = _mCodeButtonHandlerData._mTRL_LeftTrafficLockingRules;
        } else {
            this.setTitle(Bundle.getMessage("TitleDlgTRLRulesRight") + " " + identifier);   // NOI18N
            _mRulesInfo.setText(Bundle.getMessage("LabelDlgTRLRulesRightInfo"));            // NOI18N
            trafficLockingRules = _mCodeButtonHandlerData._mTRL_RightTrafficLockingRules;
        }
        _mTrafficLockingModel = new DefaultListModel<>();
        _mTRL_TrafficLockingRules.setModel(_mTrafficLockingModel);
        trafficLockingRules.forEach(rule -> {
            _mTrafficLockingModel.addElement(rule);
        });
        int ruleNumber = 1;
        for (int index = 0; index < _mTrafficLockingModel.size(); index++) {
            _mTrafficLockingModel.set(index, renumberRule(_mTrafficLockingModel.get(index), ruleNumber++));
        }
        initOrig();
        _mAwtWindowProperties.setWindowState(this, FORM_PROPERTIES);
        CommonSubs.populateJComboBoxWithBeans(_mOccupancyExternalSensor1, "Sensor", null, true);
        CommonSubs.populateJComboBoxWithBeans(_mOccupancyExternalSensor2, "Sensor", null, true);
        CommonSubs.populateJComboBoxWithBeans(_mOccupancyExternalSensor3, "Sensor", null, true);
        CommonSubs.populateJComboBoxWithBeans(_mOccupancyExternalSensor4, "Sensor", null, true);
        CommonSubs.populateJComboBoxWithBeans(_mOccupancyExternalSensor5, "Sensor", null, true);
        CommonSubs.populateJComboBoxWithBeans(_mOccupancyExternalSensor6, "Sensor", null, true);
        CommonSubs.populateJComboBoxWithBeans(_mOccupancyExternalSensor7, "Sensor", null, true);
        CommonSubs.populateJComboBoxWithBeans(_mOccupancyExternalSensor8, "Sensor", null, true);
        CommonSubs.populateJComboBoxWithBeans(_mOccupancyExternalSensor9, "Sensor", null, true);
        CommonSubs.populateJComboBoxWithBeans(_mOptionalExternalSensor1, "Sensor", null, true);
        CommonSubs.populateJComboBoxWithBeans(_mOptionalExternalSensor2, "Sensor", null, true);
        CommonSubs.populateJComboBoxWithColumnDescriptions(_mOS_NumberEntry1, _mCTCSerialData);
        CommonSubs.populateJComboBoxWithColumnDescriptions(_mOS_NumberEntry2, _mCTCSerialData);
        CommonSubs.populateJComboBoxWithColumnDescriptions(_mOS_NumberEntry3, _mCTCSerialData);
        CommonSubs.populateJComboBoxWithColumnDescriptions(_mOS_NumberEntry4, _mCTCSerialData);
        CommonSubs.populateJComboBoxWithColumnDescriptions(_mOS_NumberEntry5, _mCTCSerialData);
        String[] normalAndReverse = new String[] { Bundle.getMessage("TLE_Normal"), Bundle.getMessage("TLE_Reverse") }; // NOI18N
        _mSwitchAlignment1.setModel(new javax.swing.DefaultComboBoxModel<>(normalAndReverse));
        _mSwitchAlignment2.setModel(new javax.swing.DefaultComboBoxModel<>(normalAndReverse));
        _mSwitchAlignment3.setModel(new javax.swing.DefaultComboBoxModel<>(normalAndReverse));
        _mSwitchAlignment4.setModel(new javax.swing.DefaultComboBoxModel<>(normalAndReverse));
        _mSwitchAlignment5.setModel(new javax.swing.DefaultComboBoxModel<>(normalAndReverse));
        enableTopPart(true);
        _mEditBelow.setEnabled(false);
        _mDelete.setEnabled(false);
        _mDupToEnd.setEnabled(false);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        _mSaveAndClose = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        _mTRL_TrafficLockingRules = new javax.swing.JList<>();
        _mAddNew = new javax.swing.JButton();
        _mEditBelow = new javax.swing.JButton();
        _mDelete = new javax.swing.JButton();
        _mGroupingListAddReplace = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        _mRulesInfo = new javax.swing.JLabel();
        _mCancel = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        _mRuleEnabled = new javax.swing.JCheckBox();
        _mEnableALLRules = new javax.swing.JButton();
        _mDisableALLRules = new javax.swing.JButton();
        _mOS_NumberEntry1 = new javax.swing.JComboBox<>();
        _mOS_NumberEntry2 = new javax.swing.JComboBox<>();
        _mOS_NumberEntry3 = new javax.swing.JComboBox<>();
        _mOS_NumberEntry4 = new javax.swing.JComboBox<>();
        _mOS_NumberEntry5 = new javax.swing.JComboBox<>();
        _mSwitchAlignment1 = new javax.swing.JComboBox<>();
        _mSwitchAlignment2 = new javax.swing.JComboBox<>();
        _mSwitchAlignment3 = new javax.swing.JComboBox<>();
        _mSwitchAlignment4 = new javax.swing.JComboBox<>();
        _mSwitchAlignment5 = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        _mOccupancyExternalSensor1 = new javax.swing.JComboBox<>();
        _mOccupancyExternalSensor2 = new javax.swing.JComboBox<>();
        _mOccupancyExternalSensor3 = new javax.swing.JComboBox<>();
        _mOccupancyExternalSensor4 = new javax.swing.JComboBox<>();
        _mOccupancyExternalSensor5 = new javax.swing.JComboBox<>();
        _mOccupancyExternalSensor6 = new javax.swing.JComboBox<>();
        _mOccupancyExternalSensor7 = new javax.swing.JComboBox<>();
        _mOccupancyExternalSensor8 = new javax.swing.JComboBox<>();
        _mOccupancyExternalSensor9 = new javax.swing.JComboBox<>();
        _mOptionalExternalSensor1 = new javax.swing.JComboBox<>();
        _mOptionalExternalSensor2 = new javax.swing.JComboBox<>();
        _mDupToEnd = new javax.swing.JButton();
        _mDestinationSignalOrCommentPrompt = new javax.swing.JLabel();
        _mDestinationSignalOrComment = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle(Bundle.getMessage("TitleDlgTRLRules"));
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        _mSaveAndClose.setText(Bundle.getMessage("ButtonSaveClose"));
        _mSaveAndClose.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mSaveAndCloseActionPerformed(evt);
            }
        });

        jLabel2.setText(Bundle.getMessage("LabelDlgTRLRulesRules"));

        _mTRL_TrafficLockingRules.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            @Override
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                _mTRL_TrafficLockingRulesValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(_mTRL_TrafficLockingRules);

        _mAddNew.setText(Bundle.getMessage("ButtonAddNew"));
        _mAddNew.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mAddNewActionPerformed(evt);
            }
        });

        _mEditBelow.setText(Bundle.getMessage("ButtonEditBelow"));
        _mEditBelow.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mEditBelowActionPerformed(evt);
            }
        });

        _mDelete.setText(Bundle.getMessage("ButtonDelete"));
        _mDelete.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mDeleteActionPerformed(evt);
            }
        });

        _mGroupingListAddReplace.setText(Bundle.getMessage("ButtonDlgTRLRulesUpdate"));
        _mGroupingListAddReplace.setEnabled(false);
        _mGroupingListAddReplace.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mGroupingListAddReplaceActionPerformed(evt);
            }
        });

        jLabel1.setText(Bundle.getMessage("InfoDlgTRLRulesSep1"));

        jLabel8.setText(Bundle.getMessage("InfoDlgTRLRulesSection"));

        jLabel7.setText(Bundle.getMessage("InfoDlgTRLRulesSep2"));

        _mRulesInfo.setText(Bundle.getMessage("InfoDlgTRLRulesNote1"));

        _mCancel.setText(Bundle.getMessage("ButtonCancel"));
        _mCancel.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mCancelActionPerformed(evt);
            }
        });

        jLabel4.setText(Bundle.getMessage("InfoDlgTRLRulesNote2"));

        jLabel10.setText(Bundle.getMessage("InfoDlgTRLRulesNote3"));

        _mRuleEnabled.setText(Bundle.getMessage("LabelDlgTRLRulesEnabled"));

        _mEnableALLRules.setText(Bundle.getMessage("ButtonDlgTRLRulesEnable"));
        _mEnableALLRules.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mEnableALLRulesActionPerformed(evt);
            }
        });

        _mDisableALLRules.setText(Bundle.getMessage("ButtonDlgTRLRulesDisable"));
        _mDisableALLRules.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mDisableALLRulesActionPerformed(evt);
            }
        });

        jLabel5.setText(Bundle.getMessage("InfoDlgTRLRulesSensor"));

        _mDupToEnd.setText(Bundle.getMessage("ButtonDlgTRLRules"));
        _mDupToEnd.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mDupToEndActionPerformed(evt);
            }
        });

        _mDestinationSignalOrCommentPrompt.setText(Bundle.getMessage("InfoDlgTRLRulesDestinationSignalOrCommentPrompt")
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel8)
                            .addComponent(jLabel7)
                            .addComponent(jLabel1)
                            .addComponent(jLabel5)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(_mSwitchAlignment1, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(_mSwitchAlignment2, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(_mSwitchAlignment3, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(_mSwitchAlignment4, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(_mSwitchAlignment5, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(_mOccupancyExternalSensor1, 0, 280, Short.MAX_VALUE)
                                    .addComponent(_mOccupancyExternalSensor4, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(_mOccupancyExternalSensor7, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(_mOccupancyExternalSensor2, 0, 280, Short.MAX_VALUE)
                                    .addComponent(_mOccupancyExternalSensor5, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(_mOccupancyExternalSensor8, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(_mOccupancyExternalSensor3, 0, 280, Short.MAX_VALUE)
                                    .addComponent(_mOccupancyExternalSensor6, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(_mOccupancyExternalSensor9, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(_mOptionalExternalSensor1, javax.swing.GroupLayout.PREFERRED_SIZE, 224, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(_mOptionalExternalSensor2, javax.swing.GroupLayout.PREFERRED_SIZE, 219, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(36, 36, 36)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(_mSaveAndClose)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(_mGroupingListAddReplace, javax.swing.GroupLayout.PREFERRED_SIZE, 312, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(_mCancel))))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(_mOS_NumberEntry1, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(_mOS_NumberEntry2, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(_mOS_NumberEntry3, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(_mOS_NumberEntry4, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(_mOS_NumberEntry5, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(jLabel10)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 786, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(_mRuleEnabled)
                                .addGap(18, 18, 18)
                                .addComponent(_mEnableALLRules, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(_mDisableALLRules, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(_mDestinationSignalOrCommentPrompt)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(_mDestinationSignalOrComment, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(_mRulesInfo)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel4)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(_mEditBelow, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(_mAddNew, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(_mDelete, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(_mDupToEnd))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(_mAddNew)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(_mEditBelow)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(_mDelete)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(_mDupToEnd))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mRulesInfo)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mRuleEnabled)
                    .addComponent(_mEnableALLRules)
                    .addComponent(_mDisableALLRules)
                    .addComponent(_mDestinationSignalOrCommentPrompt)
                    .addComponent(_mDestinationSignalOrComment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mOS_NumberEntry1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(_mOS_NumberEntry2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(_mOS_NumberEntry3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(_mOS_NumberEntry4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(_mOS_NumberEntry5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mSwitchAlignment1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(_mSwitchAlignment2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(_mSwitchAlignment3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(_mSwitchAlignment4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(_mSwitchAlignment5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, Short.MAX_VALUE)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mOccupancyExternalSensor1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(_mOccupancyExternalSensor2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(_mOccupancyExternalSensor3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mOccupancyExternalSensor4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(_mOccupancyExternalSensor5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(_mOccupancyExternalSensor6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mOccupancyExternalSensor7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(_mOccupancyExternalSensor8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(_mOccupancyExternalSensor9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mGroupingListAddReplace)
                    .addComponent(_mCancel)
                    .addComponent(_mOptionalExternalSensor1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(_mOptionalExternalSensor2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(_mSaveAndClose)
                .addGap(13, 13, 13))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void _mSaveAndCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mSaveAndCloseActionPerformed
        ArrayList<TrafficLockingData> trafficLockingRules;
        if (_mIsLeftTraffic) {
            trafficLockingRules = _mCodeButtonHandlerData._mTRL_LeftTrafficLockingRules;
        } else {
            trafficLockingRules = _mCodeButtonHandlerData._mTRL_RightTrafficLockingRules;
        }

        int size = _mTrafficLockingModel.getSize();
        trafficLockingRules.clear();
        for (int index = 0; index < size; index++) {
            trafficLockingRules.add(_mTrafficLockingModel.getElementAt(index));
        }
        _mClosedNormally = true;
        _mAwtWindowProperties.saveWindowState(this, FORM_PROPERTIES);
        dispose();
    }//GEN-LAST:event__mSaveAndCloseActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        _mAwtWindowProperties.saveWindowState(this, FORM_PROPERTIES);
        if (CommonSubs.allowClose(this, dataChanged())) dispose();
    }//GEN-LAST:event_formWindowClosing

    private void _mTRL_TrafficLockingRulesValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event__mTRL_TrafficLockingRulesValueChanged
        if (_mTRL_TrafficLockingRules.isSelectionEmpty()) {
            _mEditBelow.setEnabled(false);
            _mDelete.setEnabled(false);
            _mDupToEnd.setEnabled(false);
        } else {
            _mEditBelow.setEnabled(true);
            _mDelete.setEnabled(true);
            _mDupToEnd.setEnabled(true);
        }
    }//GEN-LAST:event__mTRL_TrafficLockingRulesValueChanged

    private void _mAddNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mAddNewActionPerformed
        _mAddNewPressed = true;
        enableTopPart(false);
        _mTRL_TrafficLockingRules.setEnabled(false);
        _mTRL_TrafficLockingRules.clearSelection();
        CommonSubs.populateJComboBoxWithBeans(_mOccupancyExternalSensor1, "Sensor", null, true);
        CommonSubs.populateJComboBoxWithBeans(_mOccupancyExternalSensor2, "Sensor", null, true);
        CommonSubs.populateJComboBoxWithBeans(_mOccupancyExternalSensor3, "Sensor", null, true);
        CommonSubs.populateJComboBoxWithBeans(_mOccupancyExternalSensor4, "Sensor", null, true);
        CommonSubs.populateJComboBoxWithBeans(_mOccupancyExternalSensor5, "Sensor", null, true);
        CommonSubs.populateJComboBoxWithBeans(_mOccupancyExternalSensor6, "Sensor", null, true);
        CommonSubs.populateJComboBoxWithBeans(_mOccupancyExternalSensor7, "Sensor", null, true);
        CommonSubs.populateJComboBoxWithBeans(_mOccupancyExternalSensor8, "Sensor", null, true);
        CommonSubs.populateJComboBoxWithBeans(_mOccupancyExternalSensor9, "Sensor", null, true);
        CommonSubs.populateJComboBoxWithBeans(_mOptionalExternalSensor1, "Sensor", null, true);
        CommonSubs.populateJComboBoxWithBeans(_mOptionalExternalSensor2, "Sensor", null, true);
        _mOS_NumberEntry1.setSelectedIndex(0);
        _mOS_NumberEntry2.setSelectedIndex(0);
        _mOS_NumberEntry3.setSelectedIndex(0);
        _mOS_NumberEntry4.setSelectedIndex(0);
        _mOS_NumberEntry5.setSelectedIndex(0);
        _mSwitchAlignment1.setSelectedIndex(0);
        _mSwitchAlignment2.setSelectedIndex(0);
        _mSwitchAlignment3.setSelectedIndex(0);
        _mSwitchAlignment4.setSelectedIndex(0);
        _mSwitchAlignment5.setSelectedIndex(0);
        _mGroupingListAddReplace.setText(Bundle.getMessage("TextDlgTRLRulesAddThis"));  // NOI18N
        _mGroupingListAddReplace.setEnabled(true);
        _mRuleEnabled.setSelected(true);
        _mDestinationSignalOrComment.setText("");
        _mOS_NumberEntry1.requestFocusInWindow();
    }//GEN-LAST:event__mAddNewActionPerformed

    private void _mEditBelowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mEditBelowActionPerformed
        _mAddNewPressed = false;
        int selectedIndex = _mTRL_TrafficLockingRules.getSelectedIndex();
        enableTopPart(false);
        _mTRL_TrafficLockingRules.setEnabled(false);

        TrafficLockingData trafficLockingData = _mTrafficLockingModel.get(selectedIndex);

        ArrayList<NBHSensor> occupancySensors = trafficLockingData.getOccupancySensors();
        ArrayList<NBHSensor> optionalSensors = trafficLockingData.getOptionalSensors();
        ArrayList<Integer> ids = trafficLockingData.getUniqueIDs();
        ArrayList<String> alignments = trafficLockingData.getAlignments();

        CommonSubs.populateJComboBoxWithBeans(_mOccupancyExternalSensor1, "Sensor", occupancySensors.get(0).getHandleName(), true);
        CommonSubs.populateJComboBoxWithBeans(_mOccupancyExternalSensor2, "Sensor", occupancySensors.get(1).getHandleName(), true);
        CommonSubs.populateJComboBoxWithBeans(_mOccupancyExternalSensor3, "Sensor", occupancySensors.get(2).getHandleName(), true);
        CommonSubs.populateJComboBoxWithBeans(_mOccupancyExternalSensor4, "Sensor", occupancySensors.get(3).getHandleName(), true);
        CommonSubs.populateJComboBoxWithBeans(_mOccupancyExternalSensor5, "Sensor", occupancySensors.get(4).getHandleName(), true);
        CommonSubs.populateJComboBoxWithBeans(_mOccupancyExternalSensor6, "Sensor", occupancySensors.get(5).getHandleName(), true);
        CommonSubs.populateJComboBoxWithBeans(_mOccupancyExternalSensor7, "Sensor", occupancySensors.get(6).getHandleName(), true);
        CommonSubs.populateJComboBoxWithBeans(_mOccupancyExternalSensor8, "Sensor", occupancySensors.get(7).getHandleName(), true);
        CommonSubs.populateJComboBoxWithBeans(_mOccupancyExternalSensor9, "Sensor", occupancySensors.get(8).getHandleName(), true);

        CommonSubs.populateJComboBoxWithBeans(_mOptionalExternalSensor1, "Sensor", optionalSensors.get(0).getHandleName(), true);
        CommonSubs.populateJComboBoxWithBeans(_mOptionalExternalSensor2, "Sensor", optionalSensors.get(1).getHandleName(), true);

        CommonSubs.setSelectedIndexOfJComboBoxViaUniqueID(_mOS_NumberEntry1, _mCTCSerialData, ids.get(0));
        CommonSubs.setSelectedIndexOfJComboBoxViaUniqueID(_mOS_NumberEntry2, _mCTCSerialData, ids.get(1));
        CommonSubs.setSelectedIndexOfJComboBoxViaUniqueID(_mOS_NumberEntry3, _mCTCSerialData, ids.get(2));
        CommonSubs.setSelectedIndexOfJComboBoxViaUniqueID(_mOS_NumberEntry4, _mCTCSerialData, ids.get(3));
        CommonSubs.setSelectedIndexOfJComboBoxViaUniqueID(_mOS_NumberEntry5, _mCTCSerialData, ids.get(4));

        _mSwitchAlignment1.setSelectedItem(alignments.get(0));
        _mSwitchAlignment2.setSelectedItem(alignments.get(1));
        _mSwitchAlignment3.setSelectedItem(alignments.get(2));
        _mSwitchAlignment4.setSelectedItem(alignments.get(3));
        _mSwitchAlignment5.setSelectedItem(alignments.get(4));
        _mGroupingListAddReplace.setText(Bundle.getMessage("TextDlgTRLRulesUpdateThis"));       // NOI18N
        _mGroupingListAddReplace.setEnabled(true);
        _mRuleEnabled.setSelected(!trafficLockingData._mRuleEnabled.equals(Bundle.getMessage("TLE_RuleDisabled")));  // NOI18N  Default if invalid is ENABLED
        _mDestinationSignalOrComment.setText(trafficLockingData._mDestinationSignalOrComment);
        _mOS_NumberEntry1.requestFocusInWindow();
    }//GEN-LAST:event__mEditBelowActionPerformed

    private void _mDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mDeleteActionPerformed
        _mTrafficLockingModel.remove(_mTRL_TrafficLockingRules.getSelectedIndex());
        for (int index = 0; index < _mTrafficLockingModel.size(); index++) {
            _mTrafficLockingModel.set(index, renumberRule(_mTrafficLockingModel.get(index), index + 1));
        }
        enableTopPart(true);
    }//GEN-LAST:event__mDeleteActionPerformed

    private void _mGroupingListAddReplaceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mGroupingListAddReplaceActionPerformed
        TrafficLockingData trafficLockingData = new TrafficLockingData();
        trafficLockingData._mRuleEnabled = _mRuleEnabled.isSelected() ? Bundle.getMessage("TLE_RuleEnabled") : Bundle.getMessage("TLE_RuleDisabled");  // NOI18N
        trafficLockingData._mDestinationSignalOrComment = _mDestinationSignalOrComment.getText();

        ArrayList<NBHSensor> occupancySensors = new ArrayList<>();
        CommonSubs.addSensorToSensorList(occupancySensors, (String)_mOccupancyExternalSensor1.getSelectedItem());
        CommonSubs.addSensorToSensorList(occupancySensors, (String)_mOccupancyExternalSensor2.getSelectedItem());
        CommonSubs.addSensorToSensorList(occupancySensors, (String)_mOccupancyExternalSensor3.getSelectedItem());
        CommonSubs.addSensorToSensorList(occupancySensors, (String)_mOccupancyExternalSensor4.getSelectedItem());
        CommonSubs.addSensorToSensorList(occupancySensors, (String)_mOccupancyExternalSensor5.getSelectedItem());
        CommonSubs.addSensorToSensorList(occupancySensors, (String)_mOccupancyExternalSensor6.getSelectedItem());
        CommonSubs.addSensorToSensorList(occupancySensors, (String)_mOccupancyExternalSensor7.getSelectedItem());
        CommonSubs.addSensorToSensorList(occupancySensors, (String)_mOccupancyExternalSensor8.getSelectedItem());
        CommonSubs.addSensorToSensorList(occupancySensors, (String)_mOccupancyExternalSensor9.getSelectedItem());
        trafficLockingData._mOccupancyExternalSensors = occupancySensors;

        ArrayList<NBHSensor> optionalSensors = new ArrayList<>();
        CommonSubs.addSensorToSensorList(optionalSensors, (String)_mOptionalExternalSensor1.getSelectedItem());
        CommonSubs.addSensorToSensorList(optionalSensors, (String)_mOptionalExternalSensor2.getSelectedItem());
        trafficLockingData._mOptionalExternalSensors = optionalSensors;

        TrafficLockingData.TRLSwitch trlSwitch;
        ArrayList<TrafficLockingData.TRLSwitch> switchAlignments = new ArrayList<>();
        trlSwitch = getSwitchAllignment(_mOS_NumberEntry1, _mSwitchAlignment1);
        if (trlSwitch != null) switchAlignments.add(trlSwitch);
        trlSwitch = getSwitchAllignment(_mOS_NumberEntry2, _mSwitchAlignment2);
        if (trlSwitch != null) switchAlignments.add(trlSwitch);
        trlSwitch = getSwitchAllignment(_mOS_NumberEntry3, _mSwitchAlignment3);
        if (trlSwitch != null) switchAlignments.add(trlSwitch);
        trlSwitch = getSwitchAllignment(_mOS_NumberEntry4, _mSwitchAlignment4);
        if (trlSwitch != null) switchAlignments.add(trlSwitch);
        trlSwitch = getSwitchAllignment(_mOS_NumberEntry5, _mSwitchAlignment5);
        if (trlSwitch != null) switchAlignments.add(trlSwitch);
        trafficLockingData._mSwitchAlignments = switchAlignments;

        CheckJMRIObject.VerifyClassReturnValue verifyClassReturnValue = _mCheckJMRIObject.verifyClass(trafficLockingData);
        if (verifyClassReturnValue != null) { // Error:
            JOptionPane.showMessageDialog(this, verifyClassReturnValue.toString(),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);  // NOI18N
            return;
        }

        _mGroupingListAddReplace.setEnabled(false);
        enableTopPart(true);
        if (_mAddNewPressed) {
            trafficLockingData._mUserRuleNumber = getRuleNumberString(_mTrafficLockingModel.size() + 1);
            _mTrafficLockingModel.addElement(trafficLockingData);
        }
        else {
            int selectedIndex = _mTRL_TrafficLockingRules.getSelectedIndex();
            trafficLockingData._mUserRuleNumber = getRuleNumberString(selectedIndex + 1);
            _mTrafficLockingModel.set(selectedIndex, trafficLockingData);
        }
        _mTRL_TrafficLockingRules.setEnabled(true);
    }//GEN-LAST:event__mGroupingListAddReplaceActionPerformed

    private void _mCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mCancelActionPerformed
        enableTopPart(true);
        _mTRL_TrafficLockingRules.setEnabled(true);
    }//GEN-LAST:event__mCancelActionPerformed

    private void _mEnableALLRulesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mEnableALLRulesActionPerformed
        for (int index = 0; index < _mTrafficLockingModel.getSize(); index++) {
            TrafficLockingData trafficLockingData = _mTrafficLockingModel.get(index);
            trafficLockingData._mRuleEnabled = Bundle.getMessage("TLE_RuleEnabled");   // NOI18N
            _mTrafficLockingModel.set(index, trafficLockingData);
        }
    }//GEN-LAST:event__mEnableALLRulesActionPerformed

    private void _mDisableALLRulesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mDisableALLRulesActionPerformed
        for (int index = 0; index < _mTrafficLockingModel.getSize(); index++) {
            TrafficLockingData trafficLockingData = _mTrafficLockingModel.get(index);
            trafficLockingData._mRuleEnabled = Bundle.getMessage("TLE_RuleDisabled");  // NOI18N
            _mTrafficLockingModel.set(index, trafficLockingData);
        }
    }//GEN-LAST:event__mDisableALLRulesActionPerformed

    private void _mDupToEndActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mDupToEndActionPerformed
        TrafficLockingData trafficLockingData = _mTrafficLockingModel.get(_mTRL_TrafficLockingRules.getSelectedIndex());
        trafficLockingData._mUserRuleNumber = getRuleNumberString(_mTrafficLockingModel.size() + 1);
        _mTrafficLockingModel.addElement(trafficLockingData);
    }//GEN-LAST:event__mDupToEndActionPerformed

    private TrafficLockingData renumberRule(TrafficLockingData rule, int number) {
        rule._mUserRuleNumber = getRuleNumberString(number);
        return rule;
    }

    public static String getRuleNumberString(int ruleNumber) { return " " + Bundle.getMessage("InfoDlgTRLRuleNumber") + Integer.toString(ruleNumber); }   // NOI18N

    public static String getRuleEnabledString() { return Bundle.getMessage("TLE_RuleEnabled"); }

    public TrafficLockingData.TRLSwitch getSwitchAllignment(javax.swing.JComboBox<String> userText, javax.swing.JComboBox<String> alignment) {
        TrafficLockingData.TRLSwitch trlSwitch = null;
        int osNumberSelectedIndex = userText.getSelectedIndex();
        if (osNumberSelectedIndex > 0) {
            trlSwitch = new TrafficLockingData.TRLSwitch(
                    (String)userText.getSelectedItem(),
                    (String)alignment.getSelectedItem(),
                    _mArrayListOfSelectableOSSectionUniqueIDs.get(osNumberSelectedIndex - 1));
        }
        return trlSwitch;
    }

    private void enableTopPart(boolean enabled) {
        _mAddNew.setEnabled(enabled);
        _mDestinationSignalOrComment.setEnabled(!enabled);
        _mOccupancyExternalSensor1.setEnabled(!enabled);
        _mOccupancyExternalSensor2.setEnabled(!enabled);
        _mOccupancyExternalSensor3.setEnabled(!enabled);
        _mOccupancyExternalSensor4.setEnabled(!enabled);
        _mOccupancyExternalSensor5.setEnabled(!enabled);
        _mOccupancyExternalSensor6.setEnabled(!enabled);
        _mOccupancyExternalSensor7.setEnabled(!enabled);
        _mOccupancyExternalSensor8.setEnabled(!enabled);
        _mOccupancyExternalSensor9.setEnabled(!enabled);
        _mOptionalExternalSensor1.setEnabled(!enabled);
        _mOptionalExternalSensor2.setEnabled(!enabled);
        _mOS_NumberEntry1.setEnabled(!enabled);
        _mOS_NumberEntry2.setEnabled(!enabled);
        _mOS_NumberEntry3.setEnabled(!enabled);
        _mOS_NumberEntry4.setEnabled(!enabled);
        _mOS_NumberEntry5.setEnabled(!enabled);
        _mRuleEnabled.setEnabled(!enabled);
        _mSwitchAlignment1.setEnabled(!enabled);
        _mSwitchAlignment2.setEnabled(!enabled);
        _mSwitchAlignment3.setEnabled(!enabled);
        _mSwitchAlignment4.setEnabled(!enabled);
        _mSwitchAlignment5.setEnabled(!enabled);
        _mGroupingListAddReplace.setEnabled(!enabled);
        _mCancel.setEnabled(!enabled);
        _mSaveAndClose.setEnabled(enabled);

        if (enabled) this.getRootPane().setDefaultButton(_mSaveAndClose);
        else this.getRootPane().setDefaultButton(_mGroupingListAddReplace);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton _mAddNew;
    private javax.swing.JButton _mCancel;
    private javax.swing.JButton _mDelete;
    private javax.swing.JTextField _mDestinationSignalOrComment;
    private javax.swing.JLabel _mDestinationSignalOrCommentPrompt;
    private javax.swing.JButton _mDisableALLRules;
    private javax.swing.JButton _mDupToEnd;
    private javax.swing.JButton _mEditBelow;
    private javax.swing.JButton _mEnableALLRules;
    private javax.swing.JButton _mGroupingListAddReplace;
    private javax.swing.JComboBox<String> _mOS_NumberEntry1;
    private javax.swing.JComboBox<String> _mOS_NumberEntry2;
    private javax.swing.JComboBox<String> _mOS_NumberEntry3;
    private javax.swing.JComboBox<String> _mOS_NumberEntry4;
    private javax.swing.JComboBox<String> _mOS_NumberEntry5;
    private javax.swing.JComboBox<String> _mOccupancyExternalSensor1;
    private javax.swing.JComboBox<String> _mOccupancyExternalSensor2;
    private javax.swing.JComboBox<String> _mOccupancyExternalSensor3;
    private javax.swing.JComboBox<String> _mOccupancyExternalSensor4;
    private javax.swing.JComboBox<String> _mOccupancyExternalSensor5;
    private javax.swing.JComboBox<String> _mOccupancyExternalSensor6;
    private javax.swing.JComboBox<String> _mOccupancyExternalSensor7;
    private javax.swing.JComboBox<String> _mOccupancyExternalSensor8;
    private javax.swing.JComboBox<String> _mOccupancyExternalSensor9;
    private javax.swing.JComboBox<String> _mOptionalExternalSensor1;
    private javax.swing.JComboBox<String> _mOptionalExternalSensor2;
    private javax.swing.JCheckBox _mRuleEnabled;
    private javax.swing.JLabel _mRulesInfo;
    private javax.swing.JButton _mSaveAndClose;
    private javax.swing.JComboBox<String> _mSwitchAlignment1;
    private javax.swing.JComboBox<String> _mSwitchAlignment2;
    private javax.swing.JComboBox<String> _mSwitchAlignment3;
    private javax.swing.JComboBox<String> _mSwitchAlignment4;
    private javax.swing.JComboBox<String> _mSwitchAlignment5;
    private javax.swing.JList<TrafficLockingData> _mTRL_TrafficLockingRules;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
