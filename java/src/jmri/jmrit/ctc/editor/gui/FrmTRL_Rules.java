package jmri.jmrit.ctc.editor.gui;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.jmrit.ctc.editor.code.AwtWindowProperties;
import jmri.jmrit.ctc.editor.code.CheckJMRIObject;
import jmri.jmrit.ctc.editor.code.CommonSubs;
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import jmri.jmrit.ctc.ctcserialdata.CTCSerialData;
import jmri.jmrit.ctc.ctcserialdata.CodeButtonHandlerData;
import jmri.jmrit.ctc.ctcserialdata.ProjectsCommonSubs;
import jmri.jmrit.ctc.ctcserialdata.TrafficLockingEntry;

/**
 *
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 *
 */
public class FrmTRL_Rules extends javax.swing.JFrame {

    /**
     * Creates new form DlgTRL_Rules
     */
    private static final String FORM_PROPERTIES = "DlgTRL_Rules";   // NOI18N
//  private static final String PREFIX = "_mTRL_";                  // NOI18N
    private final AwtWindowProperties _mAwtWindowProperties;
    private boolean _mClosedNormally = false;
    public boolean closedNormally() { return _mClosedNormally; }
    private final CodeButtonHandlerData _mCodeButtonHandlerData;
    private final boolean _mIsLeftTraffic;
    private final CTCSerialData _mCTCSerialData;
    private final CheckJMRIObject _mCheckJMRIObject;
    private final ArrayList<Integer> _mArrayListOfSelectableOSSectionUniqueIDs;
    private final DefaultListModel<String> _mDefaultListModel;
    private boolean _mAddNewPressed;

    private final ArrayList<String> _mDefaultListModelOrig = new ArrayList<> ();
    private void initOrig() {
        int defaultListModelSize = _mDefaultListModel.getSize();
        for (int index = 0; index < defaultListModelSize; index++) {
            _mDefaultListModelOrig.add(_mDefaultListModel.get(index));
        }
    }
    private boolean dataChanged() {
        int defaultListModelSize = _mDefaultListModel.getSize();
        if (defaultListModelSize != _mDefaultListModelOrig.size()) return true;
        for (int index = 0; index < defaultListModelSize; index++) {
            if (!_mDefaultListModel.get(index).equals(_mDefaultListModelOrig.get(index))) return true;
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
        _mDefaultListModel = new DefaultListModel<>();
        _mTRL_TrafficLockingRulesSSVList.setModel(_mDefaultListModel);

        String trafficLockingRulesSSVList;
        String identifier = codeButtonHandlerData.myShortStringNoComma();
        if (isLeftTraffic) {
            this.setTitle(Bundle.getMessage("TitleDlgTRLRulesLeft") + " " + identifier);    // NOI18N
            _mRulesInfo.setText(Bundle.getMessage("LabelDlgTRLRulesLeftInfo"));             // NOI18N
            trafficLockingRulesSSVList = _mCodeButtonHandlerData._mTRL_LeftTrafficLockingRulesSSVList;
        } else {
            this.setTitle(Bundle.getMessage("TitleDlgTRLRulesRight") + " " + identifier);   // NOI18N
            _mRulesInfo.setText(Bundle.getMessage("LabelDlgTRLRulesRightInfo"));            // NOI18N
            trafficLockingRulesSSVList = _mCodeButtonHandlerData._mTRL_RightTrafficLockingRulesSSVList;
        }
//  Once you specify a model, then functions like JList.setListData may update the screen, but the model
//  DOES NOT SEE ANY OF THE DATA!  Therefore, I have to load the data via the model instead of directly:
        _mDefaultListModel.clear(); // Superflous but doesn't hurt in case GUI designer put something in there.....
        int ruleNumber = 1;
        for (String aString : ProjectsCommonSubs.getArrayListFromSSV(trafficLockingRulesSSVList)) {
            aString = renumberCSVString(aString, ruleNumber++);
            _mDefaultListModel.addElement(aString);
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
    @SuppressWarnings("unchecked")  // NOI18N
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        _mSaveAndClose = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        _mTRL_TrafficLockingRulesSSVList = new javax.swing.JList<>();
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
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mSaveAndCloseActionPerformed(evt);
            }
        });

        jLabel2.setText(Bundle.getMessage("LabelDlgTRLRulesRules"));

        _mTRL_TrafficLockingRulesSSVList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                _mTRL_TrafficLockingRulesSSVListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(_mTRL_TrafficLockingRulesSSVList);

        _mAddNew.setText(Bundle.getMessage("ButtonAddNew"));
        _mAddNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mAddNewActionPerformed(evt);
            }
        });

        _mEditBelow.setText(Bundle.getMessage("ButtonEditBelow"));
        _mEditBelow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mEditBelowActionPerformed(evt);
            }
        });

        _mDelete.setText(Bundle.getMessage("ButtonDelete"));
        _mDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mDeleteActionPerformed(evt);
            }
        });

        _mGroupingListAddReplace.setText(Bundle.getMessage("ButtonDlgTRLRulesUpdate"));
        _mGroupingListAddReplace.setEnabled(false);
        _mGroupingListAddReplace.addActionListener(new java.awt.event.ActionListener() {
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
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mCancelActionPerformed(evt);
            }
        });

        jLabel4.setText(Bundle.getMessage("InfoDlgTRLRulesNote2"));

        jLabel10.setText(Bundle.getMessage("InfoDlgTRLRulesNote3"));

        _mRuleEnabled.setText(Bundle.getMessage("LabelDlgTRLRulesEnabled"));

        _mEnableALLRules.setText(Bundle.getMessage("ButtonDlgTRLRulesEnable"));
        _mEnableALLRules.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mEnableALLRulesActionPerformed(evt);
            }
        });

        _mDisableALLRules.setText(Bundle.getMessage("ButtonDlgTRLRulesDisable"));
        _mDisableALLRules.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mDisableALLRulesActionPerformed(evt);
            }
        });

        jLabel5.setText(Bundle.getMessage("InfoDlgTRLRulesSensor"));

        _mDupToEnd.setText(Bundle.getMessage("ButtonDlgTRLRules"));
        _mDupToEnd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mDupToEndActionPerformed(evt);
            }
        });

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
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(_mRuleEnabled)
                                .addGap(18, 18, 18)
                                .addComponent(_mEnableALLRules, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(_mDisableALLRules, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(_mRulesInfo)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel4)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 786, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(_mEditBelow, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(_mAddNew, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(_mDelete, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(_mDupToEnd)))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(3, 3, 3)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(_mOS_NumberEntry1, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(_mOS_NumberEntry2, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(_mOS_NumberEntry3, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(_mOS_NumberEntry4, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(_mOS_NumberEntry5, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                                                .addComponent(_mCancel))))))
                            .addComponent(jLabel10))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
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
                    .addComponent(_mDisableALLRules))
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

    @SuppressFBWarnings(value = "SBSC_USE_STRINGBUFFER_CONCATENATION", justification = "I don't want to introduce bugs, CPU no big deal here.")
    private void _mSaveAndCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mSaveAndCloseActionPerformed
        int size = _mDefaultListModel.getSize();

        String resultString = "";
        for (int index = 0; index < size; index++) {
            String thisEntry = _mDefaultListModel.getElementAt(index);
            resultString = (0 == index) ? thisEntry : resultString + ProjectsCommonSubs.SSV_SEPARATOR + thisEntry;
        }

        if (_mIsLeftTraffic) { _mCodeButtonHandlerData._mTRL_LeftTrafficLockingRulesSSVList = resultString; }
        else { _mCodeButtonHandlerData._mTRL_RightTrafficLockingRulesSSVList = resultString; }
        _mClosedNormally = true;
        _mAwtWindowProperties.saveWindowState(this, FORM_PROPERTIES);
        dispose();
    }//GEN-LAST:event__mSaveAndCloseActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        _mAwtWindowProperties.saveWindowState(this, FORM_PROPERTIES);
        if (CommonSubs.allowClose(this, dataChanged())) dispose();
    }//GEN-LAST:event_formWindowClosing

    private void _mTRL_TrafficLockingRulesSSVListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event__mTRL_TrafficLockingRulesSSVListValueChanged
        if (_mTRL_TrafficLockingRulesSSVList.isSelectionEmpty()) {
            _mEditBelow.setEnabled(false);
            _mDelete.setEnabled(false);
            _mDupToEnd.setEnabled(false);
        } else {
            _mEditBelow.setEnabled(true);
            _mDelete.setEnabled(true);
            _mDupToEnd.setEnabled(true);
        }
    }//GEN-LAST:event__mTRL_TrafficLockingRulesSSVListValueChanged

    private void _mAddNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mAddNewActionPerformed
        _mAddNewPressed = true;
        enableTopPart(false);
        _mTRL_TrafficLockingRulesSSVList.setEnabled(false);
        _mTRL_TrafficLockingRulesSSVList.clearSelection();
//        _mOptionalExternalSensor1.setText("");
//        _mOptionalExternalSensor2.setText("");
//        _mOccupancyExternalSensor1.setText("");
//        _mOccupancyExternalSensor2.setText("");
//        _mOccupancyExternalSensor3.setText("");
//        _mOccupancyExternalSensor4.setText("");
//        _mOccupancyExternalSensor5.setText("");
//        _mOccupancyExternalSensor6.setText("");
//        _mOccupancyExternalSensor7.setText("");
//        _mOccupancyExternalSensor8.setText("");
//        _mOccupancyExternalSensor9.setText("");
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
        _mOS_NumberEntry1.requestFocusInWindow();
    }//GEN-LAST:event__mAddNewActionPerformed

    private void _mEditBelowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mEditBelowActionPerformed
        _mAddNewPressed = false;
        int selectedIndex = _mTRL_TrafficLockingRulesSSVList.getSelectedIndex();
        enableTopPart(false);
        _mTRL_TrafficLockingRulesSSVList.setEnabled(false);

        TrafficLockingEntry trafficLockingEntry = new TrafficLockingEntry(_mDefaultListModel.get(selectedIndex));
//        _mOccupancyExternalSensor1.setText(trafficLockingEntry._mOccupancyExternalSensor1);
//        _mOccupancyExternalSensor2.setText(trafficLockingEntry._mOccupancyExternalSensor2);
//        _mOccupancyExternalSensor3.setText(trafficLockingEntry._mOccupancyExternalSensor3);
//        _mOccupancyExternalSensor4.setText(trafficLockingEntry._mOccupancyExternalSensor4);
//        _mOccupancyExternalSensor5.setText(trafficLockingEntry._mOccupancyExternalSensor5);
//        _mOccupancyExternalSensor6.setText(trafficLockingEntry._mOccupancyExternalSensor6);
//        _mOccupancyExternalSensor7.setText(trafficLockingEntry._mOccupancyExternalSensor7);
//        _mOccupancyExternalSensor8.setText(trafficLockingEntry._mOccupancyExternalSensor8);
//        _mOccupancyExternalSensor9.setText(trafficLockingEntry._mOccupancyExternalSensor9);
//        _mOptionalExternalSensor1.setText(trafficLockingEntry._mOptionalExternalSensor1);
//        _mOptionalExternalSensor2.setText(trafficLockingEntry._mOptionalExternalSensor2);
        CommonSubs.populateJComboBoxWithBeans(_mOccupancyExternalSensor1, "Sensor", trafficLockingEntry._mOccupancyExternalSensor1, true);
        CommonSubs.populateJComboBoxWithBeans(_mOccupancyExternalSensor2, "Sensor", trafficLockingEntry._mOccupancyExternalSensor2, true);
        CommonSubs.populateJComboBoxWithBeans(_mOccupancyExternalSensor3, "Sensor", trafficLockingEntry._mOccupancyExternalSensor3, true);
        CommonSubs.populateJComboBoxWithBeans(_mOccupancyExternalSensor4, "Sensor", trafficLockingEntry._mOccupancyExternalSensor4, true);
        CommonSubs.populateJComboBoxWithBeans(_mOccupancyExternalSensor5, "Sensor", trafficLockingEntry._mOccupancyExternalSensor5, true);
        CommonSubs.populateJComboBoxWithBeans(_mOccupancyExternalSensor6, "Sensor", trafficLockingEntry._mOccupancyExternalSensor6, true);
        CommonSubs.populateJComboBoxWithBeans(_mOccupancyExternalSensor7, "Sensor", trafficLockingEntry._mOccupancyExternalSensor7, true);
        CommonSubs.populateJComboBoxWithBeans(_mOccupancyExternalSensor8, "Sensor", trafficLockingEntry._mOccupancyExternalSensor8, true);
        CommonSubs.populateJComboBoxWithBeans(_mOccupancyExternalSensor9, "Sensor", trafficLockingEntry._mOccupancyExternalSensor9, true);
        CommonSubs.populateJComboBoxWithBeans(_mOptionalExternalSensor1, "Sensor", trafficLockingEntry._mOptionalExternalSensor1, true);
        CommonSubs.populateJComboBoxWithBeans(_mOptionalExternalSensor2, "Sensor", trafficLockingEntry._mOptionalExternalSensor2, true);
        int uniqueID;
        uniqueID = ProjectsCommonSubs.getIntFromStringNoThrow(trafficLockingEntry._mUniqueID1, -1);   // Technically should NEVER throw or return default, but for safety.  Default will NEVER be found!
        CommonSubs.setSelectedIndexOfJComboBoxViaUniqueID(_mOS_NumberEntry1, _mCTCSerialData, uniqueID);
        uniqueID = ProjectsCommonSubs.getIntFromStringNoThrow(trafficLockingEntry._mUniqueID2, -1);   // Technically should NEVER throw or return default, but for safety.  Default will NEVER be found!
        CommonSubs.setSelectedIndexOfJComboBoxViaUniqueID(_mOS_NumberEntry2, _mCTCSerialData, uniqueID);
        uniqueID = ProjectsCommonSubs.getIntFromStringNoThrow(trafficLockingEntry._mUniqueID3, -1);   // Technically should NEVER throw or return default, but for safety.  Default will NEVER be found!
        CommonSubs.setSelectedIndexOfJComboBoxViaUniqueID(_mOS_NumberEntry3, _mCTCSerialData, uniqueID);
        uniqueID = ProjectsCommonSubs.getIntFromStringNoThrow(trafficLockingEntry._mUniqueID4, -1);   // Technically should NEVER throw or return default, but for safety.  Default will NEVER be found!
        CommonSubs.setSelectedIndexOfJComboBoxViaUniqueID(_mOS_NumberEntry4, _mCTCSerialData, uniqueID);
        uniqueID = ProjectsCommonSubs.getIntFromStringNoThrow(trafficLockingEntry._mUniqueID5, -1);   // Technically should NEVER throw or return default, but for safety.  Default will NEVER be found!
        CommonSubs.setSelectedIndexOfJComboBoxViaUniqueID(_mOS_NumberEntry5, _mCTCSerialData, uniqueID);
        _mSwitchAlignment1.setSelectedItem(trafficLockingEntry._mSwitchAlignment1);
        _mSwitchAlignment2.setSelectedItem(trafficLockingEntry._mSwitchAlignment2);
        _mSwitchAlignment3.setSelectedItem(trafficLockingEntry._mSwitchAlignment3);
        _mSwitchAlignment4.setSelectedItem(trafficLockingEntry._mSwitchAlignment4);
        _mSwitchAlignment5.setSelectedItem(trafficLockingEntry._mSwitchAlignment5);
        _mGroupingListAddReplace.setText(Bundle.getMessage("TextDlgTRLRulesUpdateThis"));       // NOI18N
        _mGroupingListAddReplace.setEnabled(true);
        _mRuleEnabled.setSelected(!trafficLockingEntry._mRuleEnabled.equals(Bundle.getMessage("TLE_RuleDisabled")));  // NOI18N  Default if invalid is ENABLED
        _mOS_NumberEntry1.requestFocusInWindow();
    }//GEN-LAST:event__mEditBelowActionPerformed

    private void _mDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mDeleteActionPerformed
        _mDefaultListModel.remove(_mTRL_TrafficLockingRulesSSVList.getSelectedIndex());
        for (int index = 0; index < _mDefaultListModel.size(); index++) {
            _mDefaultListModel.set(index, renumberCSVString(_mDefaultListModel.get(index), index + 1));
        }
        enableTopPart(true);
    }//GEN-LAST:event__mDeleteActionPerformed

    private void _mGroupingListAddReplaceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mGroupingListAddReplaceActionPerformed
        TrafficLockingEntry trafficLockingEntry = new TrafficLockingEntry(  _mRuleEnabled.isSelected() ? Bundle.getMessage("TLE_RuleEnabled") : Bundle.getMessage("TLE_RuleDisabled"),  // NOI18N
                                                                            (String)_mSwitchAlignment1.getSelectedItem(),
                                                                            (String)_mSwitchAlignment2.getSelectedItem(),
                                                                            (String)_mSwitchAlignment3.getSelectedItem(),
                                                                            (String)_mSwitchAlignment4.getSelectedItem(),
                                                                            (String)_mSwitchAlignment5.getSelectedItem(),
                                                                            (String)_mOccupancyExternalSensor1.getSelectedItem(),
                                                                            (String)_mOccupancyExternalSensor2.getSelectedItem(),
                                                                            (String)_mOccupancyExternalSensor3.getSelectedItem(),
                                                                            (String)_mOccupancyExternalSensor4.getSelectedItem(),
                                                                            (String)_mOccupancyExternalSensor5.getSelectedItem(),
                                                                            (String)_mOccupancyExternalSensor6.getSelectedItem(),
                                                                            (String)_mOccupancyExternalSensor7.getSelectedItem(),
                                                                            (String)_mOccupancyExternalSensor8.getSelectedItem(),
                                                                            (String)_mOccupancyExternalSensor9.getSelectedItem(),
                                                                            (String)_mOptionalExternalSensor1.getSelectedItem(),
                                                                            (String)_mOptionalExternalSensor2.getSelectedItem());

        CheckJMRIObject.VerifyClassReturnValue verifyClassReturnValue = _mCheckJMRIObject.verifyClass(trafficLockingEntry);
        if (verifyClassReturnValue != null) { // Error:
            JOptionPane.showMessageDialog(this, verifyClassReturnValue.toString(),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);  // NOI18N
            return;
        }

// Any uninitialized are null, and thats OK for "constructCSVStringFromArrayList":

        int osNumberSelectedIndex;
        osNumberSelectedIndex = _mOS_NumberEntry1.getSelectedIndex();
        if (osNumberSelectedIndex > 0) { // Something selected (-1 = none, 0 = blank i.e. none also).
            trafficLockingEntry._mUserText1 = (String)_mOS_NumberEntry1.getSelectedItem();
            trafficLockingEntry._mUniqueID1 = _mArrayListOfSelectableOSSectionUniqueIDs.get(osNumberSelectedIndex - 1).toString();
        }
        osNumberSelectedIndex = _mOS_NumberEntry2.getSelectedIndex();
        if (osNumberSelectedIndex > 0) { // Something selected (-1 = none, 0 = blank i.e. none also).
            trafficLockingEntry._mUserText2 = (String)_mOS_NumberEntry2.getSelectedItem();
            trafficLockingEntry._mUniqueID2 = _mArrayListOfSelectableOSSectionUniqueIDs.get(osNumberSelectedIndex - 1).toString();
        }
        osNumberSelectedIndex = _mOS_NumberEntry3.getSelectedIndex();
        if (osNumberSelectedIndex > 0) { // Something selected (-1 = none, 0 = blank i.e. none also).
            trafficLockingEntry._mUserText3 = (String)_mOS_NumberEntry3.getSelectedItem();
            trafficLockingEntry._mUniqueID3 = _mArrayListOfSelectableOSSectionUniqueIDs.get(osNumberSelectedIndex - 1).toString();
        }
        osNumberSelectedIndex = _mOS_NumberEntry4.getSelectedIndex();
        if (osNumberSelectedIndex > 0) { // Something selected (-1 = none, 0 = blank i.e. none also).
            trafficLockingEntry._mUserText4 = (String)_mOS_NumberEntry4.getSelectedItem();
            trafficLockingEntry._mUniqueID4 = _mArrayListOfSelectableOSSectionUniqueIDs.get(osNumberSelectedIndex - 1).toString();
        }
        osNumberSelectedIndex = _mOS_NumberEntry5.getSelectedIndex();
        if (osNumberSelectedIndex > 0) { // Something selected (-1 = none, 0 = blank i.e. none also).
            trafficLockingEntry._mUserText5 = (String)_mOS_NumberEntry5.getSelectedItem();
            trafficLockingEntry._mUniqueID5 = _mArrayListOfSelectableOSSectionUniqueIDs.get(osNumberSelectedIndex - 1).toString();
        }

        _mGroupingListAddReplace.setEnabled(false);
        enableTopPart(true);
        if (_mAddNewPressed) {
            trafficLockingEntry._mUserRuleNumber = getRuleNumberString(_mDefaultListModel.size() + 1);
            String newValue = trafficLockingEntry.toCSVString();
            _mDefaultListModel.addElement(newValue);
        }
        else {
            int selectedIndex = _mTRL_TrafficLockingRulesSSVList.getSelectedIndex();
            trafficLockingEntry._mUserRuleNumber = getRuleNumberString(selectedIndex + 1);
            String newValue = trafficLockingEntry.toCSVString();
            _mDefaultListModel.set(selectedIndex, newValue);
        }
        _mTRL_TrafficLockingRulesSSVList.setEnabled(true);
    }//GEN-LAST:event__mGroupingListAddReplaceActionPerformed

    private void _mCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mCancelActionPerformed
        enableTopPart(true);
        _mTRL_TrafficLockingRulesSSVList.setEnabled(true);
    }//GEN-LAST:event__mCancelActionPerformed

    private void _mEnableALLRulesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mEnableALLRulesActionPerformed
        for (int index = 0; index < _mDefaultListModel.getSize(); index++) {
            TrafficLockingEntry trafficLockingEntry = new TrafficLockingEntry(_mDefaultListModel.get(index));
            trafficLockingEntry._mRuleEnabled = Bundle.getMessage("TLE_RuleEnabled");   // NOI18N
            _mDefaultListModel.set(index, trafficLockingEntry.toCSVString());
        }
    }//GEN-LAST:event__mEnableALLRulesActionPerformed

    private void _mDisableALLRulesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mDisableALLRulesActionPerformed
        for (int index = 0; index < _mDefaultListModel.getSize(); index++) {
            TrafficLockingEntry trafficLockingEntry = new TrafficLockingEntry(_mDefaultListModel.get(index));
            trafficLockingEntry._mRuleEnabled = Bundle.getMessage("TLE_RuleDisabled");  // NOI18N
            _mDefaultListModel.set(index, trafficLockingEntry.toCSVString());
        }
    }//GEN-LAST:event__mDisableALLRulesActionPerformed

    private void _mDupToEndActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mDupToEndActionPerformed
        TrafficLockingEntry trafficLockingEntry = new TrafficLockingEntry(_mDefaultListModel.get(_mTRL_TrafficLockingRulesSSVList.getSelectedIndex()));
        trafficLockingEntry._mUserRuleNumber = getRuleNumberString(_mDefaultListModel.size() + 1);
        String newValue = trafficLockingEntry.toCSVString();
        _mDefaultListModel.addElement(newValue);
    }//GEN-LAST:event__mDupToEndActionPerformed

    private String renumberCSVString(String aString, int ruleNumber) {
            TrafficLockingEntry trafficLockingEntry = new TrafficLockingEntry(aString);
            trafficLockingEntry._mUserRuleNumber = getRuleNumberString(ruleNumber);
            return trafficLockingEntry.toCSVString();
    }
    private String getRuleNumberString(int ruleNumber) { return " " + Bundle.getMessage("InfoDlgTRLRuleNumber") + Integer.toString(ruleNumber); }   // NOI18N

    private void enableTopPart(boolean enabled) {
        _mAddNew.setEnabled(enabled);
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

//  private static ArrayList<String> getArrayListOfSelectableSwitchDirectionIndicators(CodeButtonHandlerData codeButtonHandlerData) {
//      ArrayList<String> returnValue = new ArrayList<>();
//      if (!codeButtonHandlerData._mSWDI_NormalInternalSensor.isEmpty()) {
//          returnValue.add(codeButtonHandlerData._mSWDI_NormalInternalSensor);
//      }
//      if (!codeButtonHandlerData._mSWDI_ReversedInternalSensor.isEmpty()) {
//          returnValue.add(codeButtonHandlerData._mSWDI_ReversedInternalSensor);
//      }
//      return returnValue;
//  }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton _mAddNew;
    private javax.swing.JButton _mCancel;
    private javax.swing.JButton _mDelete;
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
    private javax.swing.JList<String> _mTRL_TrafficLockingRulesSSVList;
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
