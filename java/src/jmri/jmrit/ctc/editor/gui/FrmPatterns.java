package jmri.jmrit.ctc.editor.gui;

import jmri.jmrit.ctc.editor.code.AwtWindowProperties;
import jmri.jmrit.ctc.editor.code.CommonSubs;
import jmri.jmrit.ctc.editor.code.ProgramProperties;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import jmri.jmrit.ctc.ctcserialdata.ProjectsCommonSubs;

/**
 *
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 */
public class FrmPatterns extends javax.swing.JFrame {

    /**
     * Creates new form dlgProperties
     */
    private static final String FORM_PROPERTIES = "DlgPatterns";    // NOI18N
    private final AwtWindowProperties _mAwtWindowProperties;
    private final ProgramProperties _mProgramProperties;
    private boolean _mClosedNormally = false;
    public boolean closedNormally() { return _mClosedNormally; }

    private String _mCodeButtonInternalSensorPatternOrig;
    private String _mSIDI_LeftInternalSensorPatternOrig;
    private String _mSIDI_NormalInternalSensorPatternOrig;
    private String _mSIDI_RightInternalSensorPatternOrig;
    private String _mSIDL_LeftInternalSensorPatternOrig;
    private String _mSIDL_NormalInternalSensorPatternOrig;
    private String _mSIDL_RightInternalSensorPatternOrig;
    private String _mSWDI_NormalInternalSensorPatternOrig;
    private String _mSWDI_ReversedInternalSensorPatternOrig;
    private String _mSWDL_InternalSensorPatternOrig;
    private String _mCO_CallOnToggleInternalSensorPatternOrig;
    private String _mTUL_DispatcherInternalSensorLockTogglePatternOrig;
    private String _mTUL_DispatcherInternalSensorUnlockedIndicatorPatternOrig;

    private void initOrig(ProgramProperties programProperties) {
        _mCodeButtonInternalSensorPatternOrig = programProperties._mCodeButtonInternalSensorPattern;
        _mSIDI_LeftInternalSensorPatternOrig = programProperties._mSIDI_LeftInternalSensorPattern;
        _mSIDI_NormalInternalSensorPatternOrig = programProperties._mSIDI_NormalInternalSensorPattern;
        _mSIDI_RightInternalSensorPatternOrig = programProperties._mSIDI_RightInternalSensorPattern;
        _mSIDL_LeftInternalSensorPatternOrig = programProperties._mSIDL_LeftInternalSensorPattern;
        _mSIDL_NormalInternalSensorPatternOrig = programProperties._mSIDL_NormalInternalSensorPattern;
        _mSIDL_RightInternalSensorPatternOrig = programProperties._mSIDL_RightInternalSensorPattern;
        _mSWDI_NormalInternalSensorPatternOrig = programProperties._mSWDI_NormalInternalSensorPattern;
        _mSWDI_ReversedInternalSensorPatternOrig = programProperties._mSWDI_ReversedInternalSensorPattern;
        _mSWDL_InternalSensorPatternOrig = programProperties._mSWDL_InternalSensorPattern;
        _mCO_CallOnToggleInternalSensorPatternOrig = programProperties._mCO_CallOnToggleInternalSensorPattern;
        _mTUL_DispatcherInternalSensorLockTogglePatternOrig = programProperties._mTUL_DispatcherInternalSensorLockTogglePattern;
        _mTUL_DispatcherInternalSensorUnlockedIndicatorPatternOrig = programProperties._mTUL_DispatcherInternalSensorUnlockedIndicatorPattern;
    }
    private boolean dataChanged() {
        if (!_mCodeButtonInternalSensorPatternOrig.equals(_mCodeButtonInternalSensorPattern.getText())) return true;
        if (!_mSIDI_LeftInternalSensorPatternOrig.equals(_mSIDI_LeftInternalSensorPattern.getText())) return true;
        if (!_mSIDI_NormalInternalSensorPatternOrig.equals(_mSIDI_NormalInternalSensorPattern.getText())) return true;
        if (!_mSIDI_RightInternalSensorPatternOrig.equals(_mSIDI_RightInternalSensorPattern.getText())) return true;
        if (!_mSIDL_LeftInternalSensorPatternOrig.equals(_mSIDL_LeftInternalSensorPattern.getText())) return true;
        if (!_mSIDL_NormalInternalSensorPatternOrig.equals(_mSIDL_NormalInternalSensorPattern.getText())) return true;
        if (!_mSIDL_RightInternalSensorPatternOrig.equals(_mSIDL_RightInternalSensorPattern.getText())) return true;
        if (!_mSWDI_NormalInternalSensorPatternOrig.equals(_mSWDI_NormalInternalSensorPattern.getText())) return true;
        if (!_mSWDI_ReversedInternalSensorPatternOrig.equals(_mSWDI_ReversedInternalSensorPattern.getText())) return true;
        if (!_mSWDL_InternalSensorPatternOrig.equals(_mSWDL_InternalSensorPattern.getText())) return true;
        if (!_mCO_CallOnToggleInternalSensorPatternOrig.equals(_mCO_CallOnToggleInternalSensorPattern.getText())) return true;
        if (!_mTUL_DispatcherInternalSensorLockTogglePatternOrig.equals(_mTUL_DispatcherInternalSensorLockTogglePattern.getText())) return true;
        if (!_mTUL_DispatcherInternalSensorUnlockedIndicatorPatternOrig.equals(_mTUL_DispatcherInternalSensorUnlockedIndicatorPattern.getText())) return true;
        return false;
    }

    public FrmPatterns(AwtWindowProperties awtWindowProperties, ProgramProperties programProperties) {
        super();
        initComponents();
        CommonSubs.addHelpMenu(this, "package.jmri.jmrit.ctc.CTC_menuCfgPat", true);  // NOI18N
        _mAwtWindowProperties = awtWindowProperties;
        _mProgramProperties = programProperties;

        _mCodeButtonInternalSensorPattern.setText(programProperties._mCodeButtonInternalSensorPattern);
        _mSIDI_LeftInternalSensorPattern.setText(programProperties._mSIDI_LeftInternalSensorPattern);
        _mSIDI_NormalInternalSensorPattern.setText(programProperties._mSIDI_NormalInternalSensorPattern);
        _mSIDI_RightInternalSensorPattern.setText(programProperties._mSIDI_RightInternalSensorPattern);
        _mSIDL_LeftInternalSensorPattern.setText(programProperties._mSIDL_LeftInternalSensorPattern);
        _mSIDL_NormalInternalSensorPattern.setText(programProperties._mSIDL_NormalInternalSensorPattern);
        _mSIDL_RightInternalSensorPattern.setText(programProperties._mSIDL_RightInternalSensorPattern);
        _mSWDI_NormalInternalSensorPattern.setText(programProperties._mSWDI_NormalInternalSensorPattern);
        _mSWDI_ReversedInternalSensorPattern.setText(programProperties._mSWDI_ReversedInternalSensorPattern);
        _mSWDL_InternalSensorPattern.setText(programProperties._mSWDL_InternalSensorPattern);
        _mCO_CallOnToggleInternalSensorPattern.setText(programProperties._mCO_CallOnToggleInternalSensorPattern);
        _mTUL_DispatcherInternalSensorLockTogglePattern.setText(programProperties._mTUL_DispatcherInternalSensorLockTogglePattern);
        _mTUL_DispatcherInternalSensorUnlockedIndicatorPattern.setText(programProperties._mTUL_DispatcherInternalSensorUnlockedIndicatorPattern);
        initOrig(programProperties);
        _mAwtWindowProperties.setWindowState(this, FORM_PROPERTIES);
        this.getRootPane().setDefaultButton(_mSaveAndClose);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")  // NOI18N
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        _mCodeButtonInternalSensorPattern = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        _mSIDI_LeftInternalSensorPattern = new javax.swing.JTextField();
        _mSIDI_NormalInternalSensorPattern = new javax.swing.JTextField();
        _mSIDI_RightInternalSensorPattern = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        _mSWDI_NormalInternalSensorPattern = new javax.swing.JTextField();
        _mSWDI_ReversedInternalSensorPattern = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        _mSIDL_LeftInternalSensorPattern = new javax.swing.JTextField();
        _mSIDL_NormalInternalSensorPattern = new javax.swing.JTextField();
        _mSIDL_RightInternalSensorPattern = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        _mSWDL_InternalSensorPattern = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        _mCO_CallOnToggleInternalSensorPattern = new javax.swing.JTextField();
        jLabel28 = new javax.swing.JLabel();
        _mSaveAndClose = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        _mTUL_DispatcherInternalSensorLockTogglePattern = new javax.swing.JTextField();
        jLabel32 = new javax.swing.JLabel();
        _mTUL_DispatcherInternalSensorUnlockedIndicatorPattern = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle(Bundle.getMessage("TItleDlgPat"));
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setText(Bundle.getMessage("LabelDlgPatCode"));
        jLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel5.setText(Bundle.getMessage("LabelDlgPatSigLeftInd"));

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel6.setText(Bundle.getMessage("LabelDlgPatSigNormalInd"));

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel7.setText(Bundle.getMessage("LabelDlgPatSigRightInd"));

        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel13.setText(Bundle.getMessage("LabelDlgPatToNormalInd"));

        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel14.setText(Bundle.getMessage("LabelDlgPatToReverseInd"));

        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel9.setText(Bundle.getMessage("LabelDlgPatSigLeftLever"));

        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel10.setText(Bundle.getMessage("LabelDlgPatSigNormalLever"));

        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel11.setText(Bundle.getMessage("LabelDlgPatSigRightLever"));

        jLabel16.setText(Bundle.getMessage("LabelDlgPatToLever"));

        jLabel28.setText(Bundle.getMessage("LabelDlgPatCallOn"));

        _mSaveAndClose.setText(Bundle.getMessage("ButtonSaveClose"));
        _mSaveAndClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mSaveAndCloseActionPerformed(evt);
            }
        });

        jLabel3.setText(Bundle.getMessage("InfoDlgPatGeneral"));

        jLabel4.setText(Bundle.getMessage("InfoDlgPatSigInds"));

        jLabel12.setText(Bundle.getMessage("InfoDlgPatToInds"));

        jLabel8.setText(Bundle.getMessage("InfoDlgPatSigLevers"));

        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel15.setText(Bundle.getMessage("InfoDlgPatToLever"));

        jLabel20.setText(Bundle.getMessage("InfoDlgPatCallOn"));

        jLabel30.setText(Bundle.getMessage("InfoDlgPatToLock"));

        jLabel31.setText(Bundle.getMessage("LabelDlgPatLockToggle"));

        jLabel32.setText(Bundle.getMessage("LabelDlgPatUnlockInd"));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel5)
                            .addComponent(jLabel1)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(jLabel10)
                            .addComponent(jLabel11)
                            .addComponent(jLabel8)
                            .addComponent(jLabel9)
                            .addComponent(jLabel4)))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel32)
                            .addComponent(jLabel31)
                            .addComponent(jLabel30))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(_mSIDI_NormalInternalSensorPattern, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel16)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(_mSWDL_InternalSensorPattern, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel14)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(_mSWDI_ReversedInternalSensorPattern, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jLabel20)
                                            .addComponent(jLabel28))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(_mCO_CallOnToggleInternalSensorPattern, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(0, 0, Short.MAX_VALUE)
                                        .addComponent(jLabel12))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(_mSIDI_LeftInternalSensorPattern, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel13)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(_mSWDI_NormalInternalSensorPattern, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(109, 109, 109))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(_mSIDL_RightInternalSensorPattern, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(_mSIDI_RightInternalSensorPattern, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(_mSIDL_NormalInternalSensorPattern, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(_mCodeButtonInternalSensorPattern, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addGap(65, 65, 65)
                                        .addComponent(_mSaveAndClose)))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(_mSIDL_LeftInternalSensorPattern, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(_mTUL_DispatcherInternalSensorLockTogglePattern, javax.swing.GroupLayout.DEFAULT_SIZE, 184, Short.MAX_VALUE)
                            .addComponent(_mTUL_DispatcherInternalSensorUnlockedIndicatorPattern))
                        .addGap(0, 0, Short.MAX_VALUE))))
            .addGroup(layout.createSequentialGroup()
                .addGap(260, 260, 260)
                .addComponent(jLabel3)
                .addGap(0, 334, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mCodeButtonInternalSensorPattern, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel12))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mSIDI_LeftInternalSensorPattern, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(_mSWDI_NormalInternalSensorPattern, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mSIDI_NormalInternalSensorPattern, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(_mSWDI_ReversedInternalSensorPattern, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mSIDI_RightInternalSensorPattern, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mSIDL_LeftInternalSensorPattern, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9)
                    .addComponent(_mSWDL_InternalSensorPattern, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mSIDL_NormalInternalSensorPattern, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(_mSIDL_RightInternalSensorPattern, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel11))
                        .addGap(28, 28, 28)
                        .addComponent(jLabel30))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel28)
                            .addComponent(_mCO_CallOnToggleInternalSensorPattern, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel31)
                    .addComponent(_mTUL_DispatcherInternalSensorLockTogglePattern, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mTUL_DispatcherInternalSensorUnlockedIndicatorPattern, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel32))
                .addGap(18, 18, 18)
                .addComponent(_mSaveAndClose)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        _mAwtWindowProperties.saveWindowState(this, FORM_PROPERTIES);
        if (CommonSubs.allowClose(this, dataChanged())) dispose();
    }//GEN-LAST:event_formWindowClosing

    private void _mSaveAndCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mSaveAndCloseActionPerformed
        _mProgramProperties._mCodeButtonInternalSensorPattern = _mCodeButtonInternalSensorPattern.getText();
        _mProgramProperties._mSIDI_LeftInternalSensorPattern = _mSIDI_LeftInternalSensorPattern.getText();
        _mProgramProperties._mSIDI_NormalInternalSensorPattern = _mSIDI_NormalInternalSensorPattern.getText();
        _mProgramProperties._mSIDI_RightInternalSensorPattern = _mSIDI_RightInternalSensorPattern.getText();
        _mProgramProperties._mSIDL_LeftInternalSensorPattern = _mSIDL_LeftInternalSensorPattern.getText();
        _mProgramProperties._mSIDL_NormalInternalSensorPattern = _mSIDL_NormalInternalSensorPattern.getText();
        _mProgramProperties._mSIDL_RightInternalSensorPattern = _mSIDL_RightInternalSensorPattern.getText();
        _mProgramProperties._mSWDI_NormalInternalSensorPattern = _mSWDI_NormalInternalSensorPattern.getText();
        _mProgramProperties._mSWDI_ReversedInternalSensorPattern = _mSWDI_ReversedInternalSensorPattern.getText();
        _mProgramProperties._mSWDL_InternalSensorPattern = _mSWDL_InternalSensorPattern.getText();
        _mProgramProperties._mCO_CallOnToggleInternalSensorPattern = _mCO_CallOnToggleInternalSensorPattern.getText();
        _mProgramProperties._mTUL_DispatcherInternalSensorLockTogglePattern = _mTUL_DispatcherInternalSensorLockTogglePattern.getText();
        _mProgramProperties._mTUL_DispatcherInternalSensorUnlockedIndicatorPattern = _mTUL_DispatcherInternalSensorUnlockedIndicatorPattern.getText();
        _mClosedNormally = true;
        _mAwtWindowProperties.saveWindowState(this, FORM_PROPERTIES);
        dispose();
    }//GEN-LAST:event__mSaveAndCloseActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField _mCO_CallOnToggleInternalSensorPattern;
    private javax.swing.JTextField _mCodeButtonInternalSensorPattern;
    private javax.swing.JTextField _mSIDI_LeftInternalSensorPattern;
    private javax.swing.JTextField _mSIDI_NormalInternalSensorPattern;
    private javax.swing.JTextField _mSIDI_RightInternalSensorPattern;
    private javax.swing.JTextField _mSIDL_LeftInternalSensorPattern;
    private javax.swing.JTextField _mSIDL_NormalInternalSensorPattern;
    private javax.swing.JTextField _mSIDL_RightInternalSensorPattern;
    private javax.swing.JTextField _mSWDI_NormalInternalSensorPattern;
    private javax.swing.JTextField _mSWDI_ReversedInternalSensorPattern;
    private javax.swing.JTextField _mSWDL_InternalSensorPattern;
    private javax.swing.JButton _mSaveAndClose;
    private javax.swing.JTextField _mTUL_DispatcherInternalSensorLockTogglePattern;
    private javax.swing.JTextField _mTUL_DispatcherInternalSensorUnlockedIndicatorPattern;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    // End of variables declaration//GEN-END:variables
}
