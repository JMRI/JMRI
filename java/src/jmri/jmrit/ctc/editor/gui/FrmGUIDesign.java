package jmri.jmrit.ctc.editor.gui;

import jmri.jmrit.ctc.editor.code.AwtWindowProperties;
import jmri.jmrit.ctc.editor.code.CommonSubs;
import jmri.jmrit.ctc.ctcserialdata.OtherData;
import jmri.jmrit.ctc.ctcserialdata.ProjectsCommonSubs;

/**
 *
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 */
public class FrmGUIDesign extends javax.swing.JFrame {

    /*
     * Creates new form dlgProperties
     */
    private static final String FORM_PROPERTIES = "DlgGUIDesign";   // NOI18N
    private final AwtWindowProperties _mAwtWindowProperties;
    private final OtherData _mOtherData;
    private boolean _mClosedNormally = false;
    public boolean closedNormally() { return _mClosedNormally; }

    private int _mGUIDesign_NumberOfEmptyColumnsAtEndOrig;
    private OtherData.CTC_PANEL_TYPE   _mGUIDesign_CTCPanelTypeOrig;
    private boolean  _mGUIDesign_BuilderPlateOrig;
    private OtherData.SIGNALS_ON_PANEL _mGUIDesign_SignalsOnPanelOrig;
    private boolean  _mGUIDesign_FleetingToggleSwitchOrig;
    private boolean  _mGUIDesign_AnalogClockEtcOrig;
    private boolean  _mGUIDesign_ReloadCTCSystemButtonOrig;
    private boolean  _mGUIDesign_CTCDebugOnToggleOrig;
    private boolean  _mGUIDesign_CreateTrackPiecesOrig;
    private OtherData.VERTICAL_SIZE _mGUIDesign_VerticalSizeOrig;
    private boolean  _mGUIDesign_OSSectionUnknownInconsistentRedBlinkOrig;
    private boolean  _mGUIDesign_TurnoutsOnPanelOrig;

    private void initOrig(OtherData otherData) {
        _mGUIDesign_NumberOfEmptyColumnsAtEndOrig = otherData._mGUIDesign_NumberOfEmptyColumnsAtEnd;
        _mGUIDesign_CTCPanelTypeOrig = otherData._mGUIDesign_CTCPanelType;
        _mGUIDesign_BuilderPlateOrig = otherData._mGUIDesign_BuilderPlate;
        _mGUIDesign_SignalsOnPanelOrig = otherData._mGUIDesign_SignalsOnPanel;
        _mGUIDesign_FleetingToggleSwitchOrig = otherData._mGUIDesign_FleetingToggleSwitch;
        _mGUIDesign_AnalogClockEtcOrig = otherData._mGUIDesign_AnalogClockEtc;
        _mGUIDesign_ReloadCTCSystemButtonOrig = otherData._mGUIDesign_ReloadCTCSystemButton;
        _mGUIDesign_CTCDebugOnToggleOrig = otherData._mGUIDesign_CTCDebugOnToggle;
        _mGUIDesign_CreateTrackPiecesOrig = otherData._mGUIDesign_CreateTrackPieces;
        _mGUIDesign_VerticalSizeOrig = otherData._mGUIDesign_VerticalSize;
        _mGUIDesign_OSSectionUnknownInconsistentRedBlinkOrig = otherData._mGUIDesign_OSSectionUnknownInconsistentRedBlink;
        _mGUIDesign_TurnoutsOnPanelOrig = otherData._mGUIDesign_TurnoutsOnPanel;
    }

    private boolean dataChanged() {
        if (CommonSubs.getIntFromJTextFieldNoThrow(_mGUIDesign_NumberOfEmptyColumnsAtEnd) != _mGUIDesign_NumberOfEmptyColumnsAtEndOrig) return true;
        if (_mGUIDesign_CTCPanelTypeOrig != OtherData.CTC_PANEL_TYPE.getRadioGroupValue(ProjectsCommonSubs.getButtonSelectedInt(_mGUIDesign_CTCPanelType))) return true;
        if (_mGUIDesign_BuilderPlateOrig != _mGUIDesign_BuilderPlate.isSelected()) return true;
        if (_mGUIDesign_SignalsOnPanelOrig != OtherData.SIGNALS_ON_PANEL.getRadioGroupValue(ProjectsCommonSubs.getButtonSelectedInt(_mGUIDesign_SignalsOnPanel))) return true;
        if (_mGUIDesign_FleetingToggleSwitchOrig != _mGUIDesign_FleetingToggleSwitch.isSelected()) return true;
        if (_mGUIDesign_AnalogClockEtcOrig != _mGUIDesign_AnalogClockEtc.isSelected()) return true;
        if (_mGUIDesign_ReloadCTCSystemButtonOrig != _mGUIDesign_ReloadCTCSystemButton.isSelected()) return true;
        if (_mGUIDesign_CTCDebugOnToggleOrig != _mGUIDesign_CTCDebugOnToggle.isSelected()) return true;
        if (_mGUIDesign_CreateTrackPiecesOrig != _mGUIDesign_CreateTrackPieces.isSelected()) return true;
        if (_mGUIDesign_VerticalSizeOrig != OtherData.VERTICAL_SIZE.getRadioGroupValue(ProjectsCommonSubs.getButtonSelectedInt(_mGUIDesign_VerticalSize))) return true;
        if (_mGUIDesign_OSSectionUnknownInconsistentRedBlinkOrig != _mGUIDesign_OSSectionUnknownInconsistentRedBlink.isSelected()) return true;
        if (_mGUIDesign_TurnoutsOnPanelOrig != _mGUIDesign_TurnoutsOnPanel.isSelected()) return true;
        return false;
    }

    public FrmGUIDesign(AwtWindowProperties awtWindowProperties, OtherData otherData) {
        super();
        initComponents();
        CommonSubs.addHelpMenu(this, "package.jmri.jmrit.ctc.CTC_menuCfgGui", true);  // NOI18N
        _mAwtWindowProperties = awtWindowProperties;
        _mOtherData = otherData;

        _mGUIDesign_NumberOfEmptyColumnsAtEnd.setText(Integer.toString(_mOtherData._mGUIDesign_NumberOfEmptyColumnsAtEnd));
        CommonSubs.numberButtonGroup(_mGUIDesign_CTCPanelType);
        CommonSubs.setButtonSelected(_mGUIDesign_CTCPanelType, _mOtherData._mGUIDesign_CTCPanelType.getRadioGroupValue());
        _mGUIDesign_BuilderPlate.setSelected(otherData._mGUIDesign_BuilderPlate);
        CommonSubs.numberButtonGroup(_mGUIDesign_SignalsOnPanel);
        CommonSubs.setButtonSelected(_mGUIDesign_SignalsOnPanel, _mOtherData._mGUIDesign_SignalsOnPanel.getRadioGroupValue());
        _mGUIDesign_FleetingToggleSwitch.setSelected(otherData._mGUIDesign_FleetingToggleSwitch);
        _mGUIDesign_AnalogClockEtc.setSelected(otherData._mGUIDesign_AnalogClockEtc);
        _mGUIDesign_ReloadCTCSystemButton.setSelected(otherData._mGUIDesign_ReloadCTCSystemButton);
        _mGUIDesign_CTCDebugOnToggle.setSelected(otherData._mGUIDesign_CTCDebugOnToggle);
        _mGUIDesign_CreateTrackPieces.setSelected(otherData._mGUIDesign_CreateTrackPieces);
        CommonSubs.numberButtonGroup(_mGUIDesign_VerticalSize);
        CommonSubs.setButtonSelected(_mGUIDesign_VerticalSize, _mOtherData._mGUIDesign_VerticalSize.getRadioGroupValue());
        _mGUIDesign_OSSectionUnknownInconsistentRedBlink.setSelected(otherData._mGUIDesign_OSSectionUnknownInconsistentRedBlink);
        _mGUIDesign_TurnoutsOnPanel.setSelected(otherData._mGUIDesign_TurnoutsOnPanel);
        initOrig(otherData);
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

        _mGUIDesign_CTCPanelType = new javax.swing.ButtonGroup();
        _mGUIDesign_SignalsOnPanel = new javax.swing.ButtonGroup();
        _mGUIDesign_VerticalSize = new javax.swing.ButtonGroup();
        _mGUIDesign_NumberOfEmptyColumnsAtEnd = new javax.swing.JFormattedTextField();
        jLabel19 = new javax.swing.JLabel();
        _mSaveAndClose = new javax.swing.JButton();
        _mAllSignals = new javax.swing.JRadioButton();
        _mGreenOffOnly = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        _mNone = new javax.swing.JRadioButton();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        _mGUIDesign_BuilderPlate = new javax.swing.JCheckBox();
        _mGUIDesign_ReloadCTCSystemButton = new javax.swing.JCheckBox();
        _mGUIDesign_CTCDebugOnToggle = new javax.swing.JCheckBox();
        _mGUIDesign_FleetingToggleSwitch = new javax.swing.JCheckBox();
        _mGUIDesign_AnalogClockEtc = new javax.swing.JCheckBox();
        _mGUIDesign_CreateTrackPieces = new javax.swing.JCheckBox();
        jRadioButton3 = new javax.swing.JRadioButton();
        jRadioButton4 = new javax.swing.JRadioButton();
        jRadioButton5 = new javax.swing.JRadioButton();
        jLabel4 = new javax.swing.JLabel();
        _mGUIDesign_OSSectionUnknownInconsistentRedBlink = new javax.swing.JCheckBox();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        _mGUIDesign_TurnoutsOnPanel = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle(Bundle.getMessage("TitleDlgGUI"));
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        _mGUIDesign_NumberOfEmptyColumnsAtEnd.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        jLabel19.setText(Bundle.getMessage("LabelDlgGUIBlanks"));

        _mSaveAndClose.setText(Bundle.getMessage("ButtonSaveClose"));
        _mSaveAndClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mSaveAndCloseActionPerformed(evt);
            }
        });

        _mGUIDesign_SignalsOnPanel.add(_mAllSignals);
        _mAllSignals.setText(Bundle.getMessage("LableDlgGUIAllOS"));

        _mGUIDesign_SignalsOnPanel.add(_mGreenOffOnly);
        _mGreenOffOnly.setText(Bundle.getMessage("LableDlgGUIGreen"));
        _mGreenOffOnly.setEnabled(false);

        jLabel1.setText(Bundle.getMessage("InfoDlgGUIPrototype"));

        _mGUIDesign_SignalsOnPanel.add(_mNone);
        _mNone.setText(Bundle.getMessage("LableDlgGUINone"));

        _mGUIDesign_CTCPanelType.add(jRadioButton1);
        jRadioButton1.setText(Bundle.getMessage("LabelDlgUSS"));

        _mGUIDesign_CTCPanelType.add(jRadioButton2);
        jRadioButton2.setText(Bundle.getMessage("LableDlgGUIOther"));
        jRadioButton2.setEnabled(false);

        jLabel2.setText(Bundle.getMessage("InfoDlgGUITYpe"));

        jLabel3.setText(Bundle.getMessage("InfoDlgGUISignals"));

        _mGUIDesign_BuilderPlate.setText(Bundle.getMessage("LableDlgGUIBuilder"));

        _mGUIDesign_ReloadCTCSystemButton.setText(Bundle.getMessage("LableDlgGUIReload"));

        _mGUIDesign_CTCDebugOnToggle.setText(Bundle.getMessage("LableDlgGUIDebug"));

        _mGUIDesign_FleetingToggleSwitch.setText(Bundle.getMessage("LableDlgGUIFleeting"));

        _mGUIDesign_AnalogClockEtc.setText(Bundle.getMessage("LableDlgGUIClock"));
        _mGUIDesign_AnalogClockEtc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mGUIDesign_AnalogClockEtcActionPerformed(evt);
            }
        });

        _mGUIDesign_CreateTrackPieces.setText(Bundle.getMessage("LableDlgGUITrack")
        );
        _mGUIDesign_CreateTrackPieces.setEnabled(false);

        _mGUIDesign_VerticalSize.add(jRadioButton3);
        jRadioButton3.setText(Bundle.getMessage("LableDlgGUI718"));

        _mGUIDesign_VerticalSize.add(jRadioButton4);
        jRadioButton4.setText(Bundle.getMessage("LableDlgGUI850"));

        _mGUIDesign_VerticalSize.add(jRadioButton5);
        jRadioButton5.setText(Bundle.getMessage("LableDlgGUI900"));

        jLabel4.setText(Bundle.getMessage("InfoDlgGUISize"));

        _mGUIDesign_OSSectionUnknownInconsistentRedBlink.setText(Bundle.getMessage("LableDlgGUIBlinkRed"));

        jLabel5.setText(Bundle.getMessage("InfoDlgGUIWhen"));

        jLabel6.setText(Bundle.getMessage("InfoDlgGUITurnouts"));

        _mGUIDesign_TurnoutsOnPanel.setText(Bundle.getMessage("LableDlgGUIGenerate"));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(138, 138, 138)
                .addComponent(_mSaveAndClose)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(_mGUIDesign_FleetingToggleSwitch)
                            .addComponent(_mGUIDesign_AnalogClockEtc)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel2)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jRadioButton2)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(jRadioButton1)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(jLabel4))))
                                    .addComponent(jLabel19))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(_mGUIDesign_NumberOfEmptyColumnsAtEnd, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jRadioButton3)
                                    .addComponent(jRadioButton4)
                                    .addComponent(jRadioButton5)
                                    .addComponent(_mGUIDesign_BuilderPlate)))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(_mGUIDesign_ReloadCTCSystemButton)
                                    .addComponent(_mGUIDesign_CreateTrackPieces))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(_mGUIDesign_CTCDebugOnToggle)
                                    .addComponent(_mGUIDesign_OSSectionUnknownInconsistentRedBlink)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(21, 21, 21)
                                        .addComponent(jLabel5)))))
                        .addGap(0, 99, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(jLabel3))
                            .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(_mGreenOffOnly)
                            .addComponent(_mNone)
                            .addComponent(_mAllSignals)
                            .addComponent(jLabel1)
                            .addComponent(_mGUIDesign_TurnoutsOnPanel))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mGUIDesign_NumberOfEmptyColumnsAtEnd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel19))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButton1)
                    .addComponent(jLabel2)
                    .addComponent(jRadioButton3)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButton2)
                    .addComponent(jRadioButton4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButton5)
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mAllSignals)
                    .addComponent(jLabel3)
                    .addComponent(_mGUIDesign_BuilderPlate))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(_mGreenOffOnly)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(_mNone)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mGUIDesign_TurnoutsOnPanel)
                    .addComponent(jLabel6))
                .addGap(34, 34, 34)
                .addComponent(_mGUIDesign_FleetingToggleSwitch)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(_mGUIDesign_AnalogClockEtc)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mGUIDesign_ReloadCTCSystemButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(_mGUIDesign_CTCDebugOnToggle))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mGUIDesign_CreateTrackPieces)
                    .addComponent(_mGUIDesign_OSSectionUnknownInconsistentRedBlink))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 14, Short.MAX_VALUE)
                .addComponent(_mSaveAndClose)
                .addGap(19, 19, 19))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        _mAwtWindowProperties.saveWindowState(this, FORM_PROPERTIES);
        if (CommonSubs.allowClose(this, dataChanged())) dispose();
    }//GEN-LAST:event_formWindowClosing

    private void _mSaveAndCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mSaveAndCloseActionPerformed
        _mOtherData._mGUIDesign_NumberOfEmptyColumnsAtEnd = Integer.parseInt(_mGUIDesign_NumberOfEmptyColumnsAtEnd.getText());
        _mOtherData._mGUIDesign_CTCPanelType = OtherData.CTC_PANEL_TYPE.getRadioGroupValue(ProjectsCommonSubs.getButtonSelectedInt(_mGUIDesign_CTCPanelType));
        _mOtherData._mGUIDesign_BuilderPlate = _mGUIDesign_BuilderPlate.isSelected();
        _mOtherData._mGUIDesign_SignalsOnPanel = OtherData.SIGNALS_ON_PANEL.getRadioGroupValue(ProjectsCommonSubs.getButtonSelectedInt(_mGUIDesign_SignalsOnPanel));
        _mOtherData._mGUIDesign_FleetingToggleSwitch = _mGUIDesign_FleetingToggleSwitch.isSelected();
        _mOtherData._mGUIDesign_AnalogClockEtc = _mGUIDesign_AnalogClockEtc.isSelected();
        _mOtherData._mGUIDesign_ReloadCTCSystemButton = _mGUIDesign_ReloadCTCSystemButton.isSelected();
        _mOtherData._mGUIDesign_CTCDebugOnToggle = _mGUIDesign_CTCDebugOnToggle.isSelected();
        _mOtherData._mGUIDesign_CreateTrackPieces = _mGUIDesign_CreateTrackPieces.isSelected();
        _mOtherData._mGUIDesign_VerticalSize = OtherData.VERTICAL_SIZE.getRadioGroupValue(ProjectsCommonSubs.getButtonSelectedInt(_mGUIDesign_VerticalSize));
        _mOtherData._mGUIDesign_OSSectionUnknownInconsistentRedBlink = _mGUIDesign_OSSectionUnknownInconsistentRedBlink.isSelected();
        _mOtherData._mGUIDesign_TurnoutsOnPanel = _mGUIDesign_TurnoutsOnPanel.isSelected();
        _mClosedNormally = true;
        _mAwtWindowProperties.saveWindowState(this, FORM_PROPERTIES);
        dispose();
    }//GEN-LAST:event__mSaveAndCloseActionPerformed

    private void _mGUIDesign_AnalogClockEtcActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mGUIDesign_AnalogClockEtcActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event__mGUIDesign_AnalogClockEtcActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton _mAllSignals;
    private javax.swing.JCheckBox _mGUIDesign_AnalogClockEtc;
    private javax.swing.JCheckBox _mGUIDesign_BuilderPlate;
    private javax.swing.JCheckBox _mGUIDesign_CTCDebugOnToggle;
    private javax.swing.ButtonGroup _mGUIDesign_CTCPanelType;
    private javax.swing.JCheckBox _mGUIDesign_CreateTrackPieces;
    private javax.swing.JCheckBox _mGUIDesign_FleetingToggleSwitch;
    private javax.swing.JFormattedTextField _mGUIDesign_NumberOfEmptyColumnsAtEnd;
    private javax.swing.JCheckBox _mGUIDesign_OSSectionUnknownInconsistentRedBlink;
    private javax.swing.JCheckBox _mGUIDesign_ReloadCTCSystemButton;
    private javax.swing.ButtonGroup _mGUIDesign_SignalsOnPanel;
    private javax.swing.JCheckBox _mGUIDesign_TurnoutsOnPanel;
    private javax.swing.ButtonGroup _mGUIDesign_VerticalSize;
    private javax.swing.JRadioButton _mGreenOffOnly;
    private javax.swing.JRadioButton _mNone;
    private javax.swing.JButton _mSaveAndClose;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JRadioButton jRadioButton4;
    private javax.swing.JRadioButton jRadioButton5;
    // End of variables declaration//GEN-END:variables
}
