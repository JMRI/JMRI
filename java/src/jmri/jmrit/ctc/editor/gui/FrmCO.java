/*_mResultSoFar getCaretPosition
http://www.coderslexicon.com/tracking-the-caret-in-a-jtextarea-with-java/
*/
package jmri.jmrit.ctc.editor.gui;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.jmrit.ctc.editor.code.AwtWindowProperties;
import jmri.jmrit.ctc.editor.code.CheckJMRIObject;
import jmri.jmrit.ctc.editor.code.CodeButtonHandlerDataRoutines;
import jmri.jmrit.ctc.editor.code.CommonSubs;
import jmri.jmrit.ctc.editor.code.ProgramProperties;
import java.util.ArrayList;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import jmri.jmrit.ctc.ctcserialdata.CTCSerialData;
import jmri.jmrit.ctc.ctcserialdata.CallOnEntry;
import jmri.jmrit.ctc.ctcserialdata.CodeButtonHandlerData;
import jmri.jmrit.ctc.ctcserialdata.OtherData;
import jmri.jmrit.ctc.ctcserialdata.ProjectsCommonSubs;

/**
 *
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 */
public class FrmCO extends javax.swing.JFrame {
    /**
     * Creates new form DlgCO
     */
    private static final String FORM_PROPERTIES = "DlgCO";  // NOI18N
    private static final String PREFIX = "_mCO_";           // NOI18N
    private final AwtWindowProperties _mAwtWindowProperties;
    private boolean _mClosedNormally = false;
    public boolean closedNormally() { return _mClosedNormally; }
    private final CodeButtonHandlerData _mCodeButtonHandlerData;
    private final ProgramProperties _mProgramProperties;
    private final CTCSerialData _mCTCSerialData;
    private final CheckJMRIObject _mCheckJMRIObject;
    private final boolean _mSignalHeadSelected;
    private final DefaultListModel<String> _mDefaultListModel;
    private boolean _mAddNewPressed;

    private String _mCO_CallOnToggleInternalSensorOrig;

    private ArrayList<String> _mDefaultListModelOrig = new ArrayList<> ();
    private void initOrig() {
        _mCO_CallOnToggleInternalSensorOrig = _mCodeButtonHandlerData._mCO_CallOnToggleInternalSensor;
        int defaultListModelSize = _mDefaultListModel.getSize();
        for (int index = 0; index < defaultListModelSize; index++) {
            _mDefaultListModelOrig.add(_mDefaultListModel.get(index));
        }
    }
    private boolean dataChanged() {
        if (!_mCO_CallOnToggleInternalSensorOrig.equals(_mCO_CallOnToggleInternalSensor.getText())) return true;
        int defaultListModelSize = _mDefaultListModel.getSize();
        if (defaultListModelSize != _mDefaultListModelOrig.size()) return true;
        for (int index = 0; index < defaultListModelSize; index++) {
            if (!_mDefaultListModel.get(index).equals(_mDefaultListModelOrig.get(index))) return true;
        }
        return false;
    }

    public FrmCO(   AwtWindowProperties awtWindowProperties, CodeButtonHandlerData codeButtonHandlerData, ProgramProperties programProperties,
                    CTCSerialData ctcSerialData, CheckJMRIObject checkJMRIObject, boolean signalHeadSelected) {
        super();
        initComponents();
        CommonSubs.addHelpMenu(this, "package.jmri.jmrit.ctc.CTC_frmCO", true);  // NOI18N
        _mSignalFacingDirection.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {  Bundle.getMessage("InfoDlgCOLeftTraffic"),
                                                                                                Bundle.getMessage("InfoDlgCORightTraffic") }));
        _mSignalAspectToDisplay.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {  Bundle.getMessage("SignalHeadStateDark"),
                                                                                                Bundle.getMessage("SignalHeadStateRed"),
                                                                                                Bundle.getMessage("SignalHeadStateYellow"),
                                                                                                Bundle.getMessage("SignalHeadStateGreen"),
                                                                                                Bundle.getMessage("SignalHeadStateFlashingRed"),
                                                                                                Bundle.getMessage("SignalHeadStateFlashingYellow"),
                                                                                                Bundle.getMessage("SignalHeadStateFlashingGreen"),
                                                                                                Bundle.getMessage("SignalHeadStateLunar"),
                                                                                                Bundle.getMessage("SignalHeadStateFlashingLunar") }));
        _mAwtWindowProperties = awtWindowProperties;
        _mCodeButtonHandlerData = codeButtonHandlerData;
        _mProgramProperties = programProperties;
        _mCTCSerialData = ctcSerialData;
        _mCheckJMRIObject = checkJMRIObject;
        _mSignalHeadSelected = signalHeadSelected;
        _mCO_CallOnToggleInternalSensor.setText(_mCodeButtonHandlerData._mCO_CallOnToggleInternalSensor);
        _mDefaultListModel = new DefaultListModel<>();
        _mGroupingsList.setModel(_mDefaultListModel);
//  Once you specify a model, then functions like JList.setListData may update the screen, but the model
//  DOES NOT SEE ANY OF THE DATA!  Therefore, I have to load the data via the model instead of directly:
        _mDefaultListModel.clear(); // Superflous but doesn't hurt in case GUI designer put something in there.....
        for (String aString : ProjectsCommonSubs.getArrayListFromSSV(_mCodeButtonHandlerData._mCO_GroupingsListString)) {
            _mDefaultListModel.addElement(aString);
        }
        initOrig();
        ArrayList<String> arrayListOfSelectableSwitchDirectionIndicators = CommonSubs.getArrayListOfSelectableSwitchDirectionIndicators(_mCTCSerialData.getCodeButtonHandlerDataArrayList());
        arrayListOfSelectableSwitchDirectionIndicators.add(0, "");     // None is always available.
        String[] arrayOfSelectableSwitchDirectionIndicators = new String[arrayListOfSelectableSwitchDirectionIndicators.size()];
        arrayOfSelectableSwitchDirectionIndicators = arrayListOfSelectableSwitchDirectionIndicators.toArray(arrayOfSelectableSwitchDirectionIndicators);
//  If I used "defaultComboBoxModel" in the calls below, that would cause ALL dropdowns to be tied together!  Changing one would change all!
//      DefaultComboBoxModel<String> defaultComboBoxModel = new DefaultComboBoxModel<>(arrayOfSelectableSwitchDirectionIndicators);
//  And there is NO copy constructor for "defaultComboBoxModel", ergo the hard way:
        _mSwitchIndicator1.setModel(new DefaultComboBoxModel<>(arrayOfSelectableSwitchDirectionIndicators));
        _mSwitchIndicator2.setModel(new DefaultComboBoxModel<>(arrayOfSelectableSwitchDirectionIndicators));
        _mSwitchIndicator3.setModel(new DefaultComboBoxModel<>(arrayOfSelectableSwitchDirectionIndicators));
        _mSwitchIndicator4.setModel(new DefaultComboBoxModel<>(arrayOfSelectableSwitchDirectionIndicators));
        _mSwitchIndicator5.setModel(new DefaultComboBoxModel<>(arrayOfSelectableSwitchDirectionIndicators));
        _mSwitchIndicator6.setModel(new DefaultComboBoxModel<>(arrayOfSelectableSwitchDirectionIndicators));
        _mAwtWindowProperties.setWindowState(this, FORM_PROPERTIES);
        enableTopPart(true);
        _mEditBelow.setEnabled(false);
        _mDelete.setEnabled(false);
    }

    public static boolean dialogCodeButtonHandlerDataValid(CheckJMRIObject checkJMRIObject, CodeButtonHandlerData codeButtonHandlerData) {
        if (!codeButtonHandlerData._mCO_Enabled) return true;  // Not enabled, can be no error!
//  Checks:
        if (ProjectsCommonSubs.isNullOrEmptyString(codeButtonHandlerData._mCO_CallOnToggleInternalSensor)) return false;
        for (String groupingListString : ProjectsCommonSubs.getArrayListFromSSV(codeButtonHandlerData._mCO_GroupingsListString)) {
            if (!checkJMRIObject.validClass(new CallOnEntry(groupingListString))) return false; // Error
        }
        return checkJMRIObject.validClassWithPrefix(PREFIX, codeButtonHandlerData);
    }

//  Validate all internal fields as much as possible:
    private ArrayList<String> formFieldsValid() {
        ArrayList<String> errors = new ArrayList<>();
//  Checks:
        CommonSubs.checkJTextFieldNotEmpty(_mCO_CallOnToggleInternalSensor, _mCO_CallOnToggleInternalSensorPrompt, errors);
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

        _mSaveAndClose = new javax.swing.JButton();
        _mCO_CallOnToggleInternalSensorPrompt = new javax.swing.JLabel();
        _mCO_CallOnToggleInternalSensor = new javax.swing.JTextField();
        _mGroupingsListPrompt = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        _mSignalFacingDirection = new javax.swing.JComboBox<>();
        _mSignalAspectToDisplay = new javax.swing.JComboBox<>();
        jScrollPane1 = new javax.swing.JScrollPane();
        _mGroupingsList = new javax.swing.JList<>();
        _mDelete = new javax.swing.JButton();
        _mEditBelow = new javax.swing.JButton();
        _mAddNew = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        _mGroupingListAddReplace = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        _mCancel = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        _mSwitchIndicator1 = new javax.swing.JComboBox<>();
        _mSwitchIndicator2 = new javax.swing.JComboBox<>();
        _mSwitchIndicator3 = new javax.swing.JComboBox<>();
        _mSwitchIndicator4 = new javax.swing.JComboBox<>();
        _mSwitchIndicator6 = new javax.swing.JComboBox<>();
        _mSwitchIndicator5 = new javax.swing.JComboBox<>();
        _mExternalSignal = new javax.swing.JComboBox<>();
        _mCalledOnExternalSensor = new javax.swing.JComboBox<>();
        _mExternalBlock = new javax.swing.JComboBox<>();
        jLabel18 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle(Bundle.getMessage("TitleDlgCO"));
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

        _mCO_CallOnToggleInternalSensorPrompt.setText(Bundle.getMessage("LabelDlgCOToggleSensor"));

        _mGroupingsListPrompt.setText(Bundle.getMessage("LabelDlgCOGroupingList"));

        jLabel5.setText(Bundle.getMessage("InfoDlgCORow1"));

        jLabel6.setText(Bundle.getMessage("InfoDlgCORow2A"));

        jLabel7.setText(Bundle.getMessage("InfoDlgCORow3A"));

        jLabel8.setText(Bundle.getMessage("InfoDlgCORow4A"));

        jLabel10.setText(Bundle.getMessage("InfoDlgCORow5A"));

        jLabel11.setText(Bundle.getMessage("InfoDlgCORow2B"));

        jLabel12.setText(Bundle.getMessage("InfoDlgCORow3B"));

        jLabel13.setText(Bundle.getMessage("InfoDlgCORow4B"));

        jLabel15.setText(Bundle.getMessage("InfoDlgCORow5B"));

        _mGroupingsList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                _mGroupingsListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(_mGroupingsList);

        _mDelete.setText(Bundle.getMessage("ButtonDelete"));
        _mDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mDeleteActionPerformed(evt);
            }
        });

        _mEditBelow.setText(Bundle.getMessage("ButtonEditBelow"));
        _mEditBelow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mEditBelowActionPerformed(evt);
            }
        });

        _mAddNew.setText(Bundle.getMessage("ButtonAddNew")
        );
        _mAddNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mAddNewActionPerformed(evt);
            }
        });

        jLabel1.setText(Bundle.getMessage("InfoDlgCORow2A"));

        jLabel3.setText(Bundle.getMessage("InfoDlgCORow5A"));

        _mGroupingListAddReplace.setText("      ");
        _mGroupingListAddReplace.setEnabled(false);
        _mGroupingListAddReplace.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mGroupingListAddReplaceActionPerformed(evt);
            }
        });

        jButton2.setText(Bundle.getMessage("ButtonReapply"));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        _mCancel.setText(Bundle.getMessage("ButtonCancel"));
        _mCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mCancelActionPerformed(evt);
            }
        });

        jLabel4.setText(Bundle.getMessage("InfoDlgCORow6A"));

        jLabel16.setText(Bundle.getMessage("InfoDlgCORow6A"));

        jLabel17.setText(Bundle.getMessage("InfoDlgCORow6B"));

        jLabel9.setText(Bundle.getMessage("InfoDlgCOSep"));

        jLabel14.setText(Bundle.getMessage("InfoDlgCOSelect"));

        _mSwitchIndicator1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mSwitchIndicator1ActionPerformed(evt);
            }
        });

        jLabel18.setText(Bundle.getMessage("InfoDlgCORow4A"));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(34, 34, 34)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel7)
                            .addComponent(jLabel8)
                            .addComponent(jLabel6)
                            .addComponent(jLabel10)
                            .addComponent(jLabel16))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel11)
                            .addComponent(jLabel12)
                            .addComponent(jLabel13)
                            .addComponent(jLabel15)
                            .addComponent(jLabel17)))
                    .addComponent(jLabel5)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton2)
                        .addGap(252, 252, 252)
                        .addComponent(_mSaveAndClose))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(_mCO_CallOnToggleInternalSensorPrompt)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(_mCO_CallOnToggleInternalSensor, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(_mGroupingsListPrompt, javax.swing.GroupLayout.Alignment.TRAILING))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jScrollPane1)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(_mEditBelow, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(_mAddNew, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(_mDelete, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                            .addGap(9, 9, 9)
                            .addComponent(jLabel1)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(_mExternalSignal, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(_mSignalFacingDirection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(14, 14, 14)
                            .addComponent(jLabel18)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(_mSignalAspectToDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(jLabel3)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(_mCalledOnExternalSensor, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(12, 12, 12))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel9)
                                .addComponent(jLabel14)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(_mSwitchIndicator1, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(_mSwitchIndicator2, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(_mSwitchIndicator3, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(_mSwitchIndicator4, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(_mSwitchIndicator5, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(_mSwitchIndicator6, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGap(18, 18, 18)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(_mGroupingListAddReplace, javax.swing.GroupLayout.PREFERRED_SIZE, 340, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(0, 66, Short.MAX_VALUE))
                                .addGroup(layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                            .addComponent(_mCancel)
                                            .addGap(0, 0, Short.MAX_VALUE))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                            .addGap(0, 0, Short.MAX_VALUE)
                                            .addComponent(jLabel4)))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(_mExternalBlock, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(_mGroupingsListPrompt)
                        .addGap(44, 44, 44)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(_mCO_CallOnToggleInternalSensorPrompt)
                            .addComponent(_mCO_CallOnToggleInternalSensor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(_mAddNew)
                        .addGap(18, 18, 18)
                        .addComponent(_mEditBelow)
                        .addGap(18, 18, 18)
                        .addComponent(_mDelete)))
                .addGap(18, 18, 18)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jLabel11))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jLabel12))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jLabel13))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(jLabel15))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(jLabel17))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mSignalFacingDirection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(_mSignalAspectToDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4)
                    .addComponent(_mExternalSignal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(_mCalledOnExternalSensor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(_mExternalBlock, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel18))
                .addGap(18, 18, 18)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(_mCancel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mSwitchIndicator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(_mSwitchIndicator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(_mSwitchIndicator3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(_mSwitchIndicator4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(_mSwitchIndicator5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(_mSwitchIndicator6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(_mGroupingListAddReplace))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mSaveAndClose)
                    .addComponent(jButton2))
                .addGap(18, 18, 18))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    @SuppressFBWarnings(value = "SBSC_USE_STRINGBUFFER_CONCATENATION", justification = "I don't want to introduce bugs, CPU no big deal here.")
    private void _mSaveAndCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mSaveAndCloseActionPerformed
        if (CommonSubs.missingFieldsErrorDialogDisplayed(this, formFieldsValid(), false)) {
            return; // Do not allow exit or transfer of data.
        }
        _mCodeButtonHandlerData._mCO_CallOnToggleInternalSensor = _mCO_CallOnToggleInternalSensor.getText();
        int size = _mDefaultListModel.getSize();
        String resultString = "";
        for (int index = 0; index < size; index++) {
            String thisEntry = _mDefaultListModel.getElementAt(index);
            resultString = 0 == index ? thisEntry : resultString + ProjectsCommonSubs.SSV_SEPARATOR + thisEntry;
        }
        _mCodeButtonHandlerData._mCO_GroupingsListString = resultString;
        _mClosedNormally = true;
        _mAwtWindowProperties.saveWindowState(this, FORM_PROPERTIES);
        dispose();
    }//GEN-LAST:event__mSaveAndCloseActionPerformed

    private void _mAddNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mAddNewActionPerformed
        _mAddNewPressed = true;
        enableTopPart(false);
        _mGroupingsList.setEnabled(false);
        _mGroupingsList.clearSelection();
        _mSignalFacingDirection.setSelectedIndex(0);
        _mSignalAspectToDisplay.setSelectedIndex(4);    // Flashing Red.
        _mGroupingListAddReplace.setText(Bundle.getMessage("TextDlgCOAddInstructions"));    // NOI18N
        _mGroupingListAddReplace.setEnabled(true);
        _mSwitchIndicator1.setSelectedIndex(0);
        _mSwitchIndicator2.setSelectedIndex(0);
        _mSwitchIndicator3.setSelectedIndex(0);
        _mSwitchIndicator4.setSelectedIndex(0);
        _mSwitchIndicator5.setSelectedIndex(0);
        _mSwitchIndicator6.setSelectedIndex(0);
        _mExternalSignal.requestFocusInWindow();
        if (_mSignalHeadSelected) {
            CommonSubs.populateJComboBoxWithBeans(_mExternalSignal, "SignalHead", null, false);
            CommonSubs.populateJComboBoxWithBeans(_mCalledOnExternalSensor, "Sensor", null, false);
            CommonSubs.populateJComboBoxWithBeans(_mExternalBlock, "Block", null, true);
        } else {
            CommonSubs.populateJComboBoxWithBeans(_mExternalSignal, "SignalMast", null, false);
            CommonSubs.populateJComboBoxWithBeans(_mCalledOnExternalSensor, "Sensor", null, true);
            CommonSubs.populateJComboBoxWithBeans(_mExternalBlock, "Block", null, false);
        }
    }//GEN-LAST:event__mAddNewActionPerformed

    private void _mEditBelowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mEditBelowActionPerformed
        _mAddNewPressed = false;
        int selectedIndex = _mGroupingsList.getSelectedIndex();
        enableTopPart(false);
        _mGroupingsList.setEnabled(false);
        CallOnEntry callOnEntry = new CallOnEntry(_mDefaultListModel.get(selectedIndex));
        _mSignalFacingDirection.setSelectedItem(callOnEntry._mSignalFacingDirection);
        _mSignalAspectToDisplay.setSelectedItem(callOnEntry._mSignalAspectToDisplay);
        _mGroupingListAddReplace.setText(Bundle.getMessage("TextDlgCOUpdateInstructions")); // NOI18N
        _mGroupingListAddReplace.setEnabled(true);
        _mSwitchIndicator1.setSelectedItem(callOnEntry._mSwitchIndicator1);
        _mSwitchIndicator2.setSelectedItem(callOnEntry._mSwitchIndicator2);
        _mSwitchIndicator3.setSelectedItem(callOnEntry._mSwitchIndicator3);
        _mSwitchIndicator4.setSelectedItem(callOnEntry._mSwitchIndicator4);
        _mSwitchIndicator5.setSelectedItem(callOnEntry._mSwitchIndicator5);
        _mSwitchIndicator6.setSelectedItem(callOnEntry._mSwitchIndicator6);
        _mExternalSignal.requestFocusInWindow();
        if (_mSignalHeadSelected) {
            CommonSubs.populateJComboBoxWithBeans(_mExternalSignal, "SignalHead", callOnEntry._mExternalSignal, false);
            CommonSubs.populateJComboBoxWithBeans(_mCalledOnExternalSensor, "Sensor", callOnEntry._mCalledOnExternalSensor, false);
            CommonSubs.populateJComboBoxWithBeans(_mExternalBlock, "Block", null, true);
        } else {
            CommonSubs.populateJComboBoxWithBeans(_mExternalSignal, "SignalMast", callOnEntry._mExternalSignal, false);
            CommonSubs.populateJComboBoxWithBeans(_mCalledOnExternalSensor, "Sensor", null, true);
            CommonSubs.populateJComboBoxWithBeans(_mExternalBlock, "Block", callOnEntry._mExternalBlock, false);
        }
    }//GEN-LAST:event__mEditBelowActionPerformed

    private void _mDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mDeleteActionPerformed
        _mDefaultListModel.remove(_mGroupingsList.getSelectedIndex());
        enableTopPart(true);
    }//GEN-LAST:event__mDeleteActionPerformed

    private void _mGroupingListAddReplaceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mGroupingListAddReplaceActionPerformed
        if (ProjectsCommonSubs.isNullOrEmptyString((String) _mExternalSignal.getSelectedItem())) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("ErrorDlgCOSignalInvalid"),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE); // NOI18N
            return;
        }
        if (_mSignalHeadSelected) {
            if (ProjectsCommonSubs.isNullOrEmptyString((String) _mCalledOnExternalSensor.getSelectedItem())) {
                JOptionPane.showMessageDialog(this, Bundle.getMessage("ErrorDlgCOCalledOnSensorInvalid"),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE); // NOI18N
                return;
            }
        } else {
            if (ProjectsCommonSubs.isNullOrEmptyString((String) _mExternalBlock.getSelectedItem())) {
                JOptionPane.showMessageDialog(this, Bundle.getMessage("ErrorDlgCOBlockInvalid"),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE); // NOI18N
                return;
            }
        }
        CallOnEntry callOnEntry = new CallOnEntry(  (String) _mExternalSignal.getSelectedItem(),
                (_mSignalFacingDirection.getSelectedItem() == null ? null : _mSignalFacingDirection.getSelectedItem().toString()),
                (_mSignalAspectToDisplay.getSelectedItem() == null ? null : _mSignalAspectToDisplay.getSelectedItem().toString()),
                (String) _mCalledOnExternalSensor.getSelectedItem(),
                (String) _mExternalBlock.getSelectedItem(),
                (String)_mSwitchIndicator1.getSelectedItem(),
                (String)_mSwitchIndicator2.getSelectedItem(),
                (String)_mSwitchIndicator3.getSelectedItem(),
                (String)_mSwitchIndicator4.getSelectedItem(),
                (String)_mSwitchIndicator5.getSelectedItem(),
                (String)_mSwitchIndicator6.getSelectedItem());
        CheckJMRIObject.VerifyClassReturnValue verifyClassReturnValue = _mCheckJMRIObject.verifyClass(callOnEntry);
        if (verifyClassReturnValue != null) { // Error:
            JOptionPane.showMessageDialog(this, verifyClassReturnValue.toString(),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);    // NOI18N
            return;
        }

        String newValue = callOnEntry.toCSVString();
        _mGroupingListAddReplace.setEnabled(false);
        enableTopPart(true);
        if (_mAddNewPressed) {
            _mDefaultListModel.addElement(newValue);
        }
        else {
            _mDefaultListModel.set(_mGroupingsList.getSelectedIndex(), newValue);
        }
        _mGroupingsList.setEnabled(true);
    }//GEN-LAST:event__mGroupingListAddReplaceActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        _mAwtWindowProperties.saveWindowState(this, FORM_PROPERTIES);
        if (CommonSubs.allowClose(this, dataChanged())) dispose();
    }//GEN-LAST:event_formWindowClosing

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        CodeButtonHandlerData temp = _mCodeButtonHandlerData.deepCopy();
        temp = CodeButtonHandlerDataRoutines.uECBHDWSD_CallOn(_mProgramProperties, temp);
        _mCO_CallOnToggleInternalSensor.setText(temp._mCO_CallOnToggleInternalSensor);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void _mGroupingsListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event__mGroupingsListValueChanged
        if (_mGroupingsList.isSelectionEmpty()) {
            _mEditBelow.setEnabled(false);
            _mDelete.setEnabled(false);
        } else {
            _mEditBelow.setEnabled(true);
            _mDelete.setEnabled(true);
        }
    }//GEN-LAST:event__mGroupingsListValueChanged

    private void _mCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mCancelActionPerformed
        enableTopPart(true);
        _mGroupingsList.setEnabled(true);
    }//GEN-LAST:event__mCancelActionPerformed

    private void _mSwitchIndicator1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mSwitchIndicator1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event__mSwitchIndicator1ActionPerformed

    private void enableTopPart(boolean enabled) {
        _mAddNew.setEnabled(enabled);
        _mSwitchIndicator1.setEnabled(!enabled);
        _mSwitchIndicator2.setEnabled(!enabled);
        _mSwitchIndicator3.setEnabled(!enabled);
        _mSwitchIndicator4.setEnabled(!enabled);
        _mSwitchIndicator5.setEnabled(!enabled);
        _mSwitchIndicator6.setEnabled(!enabled);

        _mExternalSignal.setEnabled(!enabled);
        _mSignalFacingDirection.setEnabled(!enabled);
        _mSignalAspectToDisplay.setEnabled(!enabled && _mSignalHeadSelected);
        _mCalledOnExternalSensor.setEnabled(!enabled && _mSignalHeadSelected);
        _mExternalBlock.setEnabled(!enabled && !_mSignalHeadSelected);

        _mGroupingListAddReplace.setEnabled(!enabled);
        _mCancel.setEnabled(!enabled);
        _mSaveAndClose.setEnabled(enabled);

        if (enabled) this.getRootPane().setDefaultButton(_mSaveAndClose);
        else this.getRootPane().setDefaultButton(_mGroupingListAddReplace);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton _mAddNew;
    private javax.swing.JTextField _mCO_CallOnToggleInternalSensor;
    private javax.swing.JLabel _mCO_CallOnToggleInternalSensorPrompt;
    private javax.swing.JComboBox<String> _mCalledOnExternalSensor;
    private javax.swing.JButton _mCancel;
    private javax.swing.JButton _mDelete;
    private javax.swing.JButton _mEditBelow;
    private javax.swing.JComboBox<String> _mExternalBlock;
    private javax.swing.JComboBox<String> _mExternalSignal;
    private javax.swing.JButton _mGroupingListAddReplace;
    private javax.swing.JList<String> _mGroupingsList;
    private javax.swing.JLabel _mGroupingsListPrompt;
    private javax.swing.JButton _mSaveAndClose;
    private javax.swing.JComboBox<String> _mSignalAspectToDisplay;
    private javax.swing.JComboBox<String> _mSignalFacingDirection;
    private javax.swing.JComboBox<String> _mSwitchIndicator1;
    private javax.swing.JComboBox<String> _mSwitchIndicator2;
    private javax.swing.JComboBox<String> _mSwitchIndicator3;
    private javax.swing.JComboBox<String> _mSwitchIndicator4;
    private javax.swing.JComboBox<String> _mSwitchIndicator5;
    private javax.swing.JComboBox<String> _mSwitchIndicator6;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
