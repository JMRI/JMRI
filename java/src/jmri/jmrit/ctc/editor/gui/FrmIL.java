package jmri.jmrit.ctc.editor.gui;

import jmri.jmrit.ctc.editor.code.AwtWindowProperties;
import jmri.jmrit.ctc.editor.code.CheckJMRIObject;
import jmri.jmrit.ctc.editor.code.CommonSubs;
import java.util.ArrayList;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import jmri.jmrit.ctc.ctcserialdata.CTCSerialData;
import jmri.jmrit.ctc.ctcserialdata.CodeButtonHandlerData;
import jmri.jmrit.ctc.ctcserialdata.ProjectsCommonSubs;

/**
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 */
public class FrmIL extends javax.swing.JFrame {

    /**
     * Creates new form DlgIL
     */
    private static final String FORM_PROPERTIES = "DlgIL";  // NOI18N
    private static final String PREFIX = "_mIL_";           // NOI18N
    private final AwtWindowProperties _mAwtWindowProperties;
    private boolean _mClosedNormally = false;
    public boolean closedNormally() { return _mClosedNormally; }
    private final CodeButtonHandlerData _mCodeButtonHandlerData;
    private final CheckJMRIObject _mCheckJMRIObject;
    private final boolean _mSignalHeadSelected;
    private final CTCSerialData _mCTCSerialData;
    private final ArrayList<Integer> _mUniqueIDS = new ArrayList<>();

    private ArrayList<String> _mSignalsArrayListOrig;
    private void initOrig(ArrayList<String> signalsArrayList) {
        _mSignalsArrayListOrig = new ArrayList<>();
        for (int index = 0; index < signalsArrayList.size(); index++) {
            _mSignalsArrayListOrig.add(signalsArrayList.get(index));
        }
    }
    private boolean dataChanged() {
        int tableLength = CommonSubs.compactDefaultTableModel(_mIL_TableOfExternalSignalNamesDefaultTableModel);
        if (tableLength != _mSignalsArrayListOrig.size()) return true;
        for (int index = 0; index < tableLength; index++) {
            if (!_mSignalsArrayListOrig.get(index).equals(_mIL_TableOfExternalSignalNamesDefaultTableModel.getValueAt(index, 0))) return true;
        }
        return false;
    }

    private final DefaultTableModel _mIL_TableOfExternalSignalNamesDefaultTableModel;

    public FrmIL(   AwtWindowProperties awtWindowProperties,
                    CodeButtonHandlerData codeButtonHandlerData,
                    CheckJMRIObject checkJMRIObject,
                    boolean signalHeadSelected,
                    CTCSerialData ctcSerialData) {
        super();
        initComponents();
        CommonSubs.addHelpMenu(this, "package.jmri.jmrit.ctc.CTC_frmIL", true);  // NOI18N
        _mAwtWindowProperties = awtWindowProperties;
        _mCodeButtonHandlerData = codeButtonHandlerData;
        _mCheckJMRIObject = checkJMRIObject;
        _mSignalHeadSelected = signalHeadSelected;
        _mCTCSerialData = ctcSerialData;
        _mIL_TableOfExternalSignalNamesDefaultTableModel = (DefaultTableModel)_mIL_TableOfExternalSignalNames.getModel();
        ArrayList<String> signalsArrayList = ProjectsCommonSubs.getArrayListFromCSV(_mCodeButtonHandlerData._mIL_ListOfCSVSignalNames);
        loadUpSignalTable(signalsArrayList);
        initOrig(signalsArrayList);
//  This is TYPICAL of the poor quality of Java coding by supposed advanced programmers.
//  I searched the entire Oracle Web sites that publishes documentation on Java, and NOWHERE
//  is this mentioned.  HOW IN THE HELL is anyone supposed to find out about this?
//  And WHY would the default be the other way?  Why don't they just admit they are poor programmers!
//  Where is a list of properties available and their corresponding functions?
        _mIL_TableOfExternalSignalNames.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);    // NOI18N
        _mAwtWindowProperties.setWindowState(this, FORM_PROPERTIES);
        this.getRootPane().setDefaultButton(_mSaveAndClose);

        enableSignalListComboBox(_mIL_TableOfExternalSignalNames);
        boolean anyAvailable = CommonSubs.populateJComboBoxWithColumnDescriptionsExceptOurs(_mOS_NumberEntry, _mCTCSerialData, _mCodeButtonHandlerData._mUniqueID, _mUniqueIDS);
        BT_Replace.setEnabled(anyAvailable);
    }

    public static boolean dialogCodeButtonHandlerDataValid(CheckJMRIObject checkJMRIObject, CodeButtonHandlerData codeButtonHandlerData) {
        if (!codeButtonHandlerData._mIL_Enabled) return true; // Not enabled, can be no error!
//  Checks:
        if (ProjectsCommonSubs.isNullOrEmptyString(codeButtonHandlerData._mIL_ListOfCSVSignalNames)) return false;
        for (String signalName : ProjectsCommonSubs.getArrayListFromCSV(codeButtonHandlerData._mIL_ListOfCSVSignalNames)) {
            if (checkJMRIObject.checkSignal(signalName) == false) return false;
        }
        return checkJMRIObject.validClassWithPrefix(PREFIX, codeButtonHandlerData);
    }

//  Validate all internal fields as much as possible:
    private ArrayList<String> formFieldsValid() {
        ArrayList<String> errors = new ArrayList<>();
//  Checks:
        if (CommonSubs.getCSVStringFromDefaultTableModel(_mIL_TableOfExternalSignalNamesDefaultTableModel).isEmpty()) {
            errors.add(Bundle.getMessage("InfoDlgILNoEntriesIn") + " \"" + _mTableOfSignalNamesPrompt.getText() + "\"");    // NOI18N
        }
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
        // Since IL does not have a signal type field, use the SIDI signal type
        if (_mSignalHeadSelected) {
            CommonSubs.populateJComboBoxWithBeans(comboBox, "SignalHead", null, true);
        } else {
            CommonSubs.populateJComboBoxWithBeans(comboBox, "SignalMast", null, true);
        }

        // Update the signal list cell editor
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setCellEditor(new javax.swing.DefaultCellEditor(comboBox));
    }

    private void loadUpSignalTable(ArrayList<String> signalsArrayList) {
        int signalsArrayLength = signalsArrayList.size();
        if (signalsArrayLength > _mIL_TableOfExternalSignalNames.getRowCount()) { // Has more than default (100 as of this writing) rows:
            _mIL_TableOfExternalSignalNamesDefaultTableModel.setRowCount(signalsArrayLength);
        }
        for (int index = 0; index < signalsArrayLength; index++) {
            _mIL_TableOfExternalSignalNamesDefaultTableModel.setValueAt(signalsArrayList.get(index), index, 0);
        }
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
        jScrollPane1 = new javax.swing.JScrollPane();
        _mIL_TableOfExternalSignalNames = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        _mTableOfSignalNamesPrompt = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        _mOS_NumberEntry = new javax.swing.JComboBox<>();
        BT_Replace = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle(Bundle.getMessage("TitleDlgIL"));
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

        _mIL_TableOfExternalSignalNames.setModel(new javax.swing.table.DefaultTableModel(
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
        _mIL_TableOfExternalSignalNames.setRowHeight(18);
        jScrollPane1.setViewportView(_mIL_TableOfExternalSignalNames);

        jButton1.setText(Bundle.getMessage("ButtonDlgILCompact"));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel4.setText(Bundle.getMessage("InfoDlgILRemove"));

        jLabel1.setText(Bundle.getMessage("InfoDlgILNonRed"));

        jLabel5.setText(Bundle.getMessage("InfoDlgILLocked"));

        jLabel6.setText(Bundle.getMessage("InfoDlgILAutomatic"));

        _mTableOfSignalNamesPrompt.setText(Bundle.getMessage("LabelDlgILNames"));

        jLabel2.setText(Bundle.getMessage("LabelDlgILReplaceSet"));

        BT_Replace.setText(Bundle.getMessage("ButtonDlgILReplace"));
        BT_Replace.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BT_ReplaceActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(_mTableOfSignalNamesPrompt))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel5)
                    .addComponent(jButton1)
                    .addComponent(jLabel4)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(_mSaveAndClose)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel6))
                    .addComponent(jLabel2)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(_mOS_NumberEntry, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(BT_Replace)))
                .addContainerGap(169, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(_mTableOfSignalNamesPrompt, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addGap(47, 47, 47)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(_mOS_NumberEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(BT_Replace))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 155, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(_mSaveAndClose)
                            .addComponent(jLabel6)))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void _mSaveAndCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mSaveAndCloseActionPerformed
        if (CommonSubs.missingFieldsErrorDialogDisplayed(this, formFieldsValid(), false)) {
            return; // Do not allow exit or transfer of data.
        }
        _mCodeButtonHandlerData._mIL_ListOfCSVSignalNames = CommonSubs.getCSVStringFromDefaultTableModel(_mIL_TableOfExternalSignalNamesDefaultTableModel);
        _mClosedNormally = true;
        _mAwtWindowProperties.saveWindowState(this, FORM_PROPERTIES);
        dispose();
    }//GEN-LAST:event__mSaveAndCloseActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        _mAwtWindowProperties.saveWindowState(this, FORM_PROPERTIES);
        if (CommonSubs.allowClose(this, dataChanged())) dispose();
    }//GEN-LAST:event_formWindowClosing

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        CommonSubs.compactDefaultTableModel(_mIL_TableOfExternalSignalNamesDefaultTableModel);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void BT_ReplaceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BT_ReplaceActionPerformed
        int index = _mOS_NumberEntry.getSelectedIndex();
        if (index != -1) { // Safety:
            CodeButtonHandlerData otherCodeButtonHandlerData = _mCTCSerialData.getCodeButtonHandlerDataViaUniqueID(_mUniqueIDS.get(index));
            loadUpSignalTable(ProjectsCommonSubs.getArrayListFromCSV(otherCodeButtonHandlerData._mIL_ListOfCSVSignalNames));
        }
    }//GEN-LAST:event_BT_ReplaceActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton BT_Replace;
    private javax.swing.JTable _mIL_TableOfExternalSignalNames;
    private javax.swing.JComboBox<String> _mOS_NumberEntry;
    private javax.swing.JButton _mSaveAndClose;
    private javax.swing.JLabel _mTableOfSignalNamesPrompt;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
