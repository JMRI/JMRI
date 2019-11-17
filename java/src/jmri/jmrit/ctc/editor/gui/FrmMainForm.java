/*
For this warning:
Note: C:\Users\NetBeansJMRI\Documents\NetBeansProjects\CTCTest\src\packageTest\FrmMainForm.java uses unchecked or unsafe operations.
Note: Recompile with -Xlint:unchecked for details.
Do this:
    Project Properties -> Build -> Compiling in "Additional compiler options" at the bottom:
    put in "-Xlint:unchecked"

https://stackoverflow.com/questions/494869/file-changed-listener-in-java
https://blogs.oracle.com/thejavatutorials/watching-a-directory-for-changes-mdash-file-change-notification-in-nio2

Set look and feel at design:
https://docs.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
At runtime (dynamic):
https://docs.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html#dynamic
*/
package jmri.jmrit.ctc.editor.gui;

import jmri.jmrit.ctc.editor.code.Columns;
import jmri.jmrit.ctc.editor.code.CodeButtonHandlerDataRoutines;
import jmri.jmrit.ctc.editor.code.AwtWindowProperties;
import jmri.jmrit.ctc.editor.code.CheckJMRIObject;
import jmri.jmrit.ctc.editor.code.CommonSubs;
import jmri.jmrit.ctc.editor.code.CreateXMLFiles;
import jmri.jmrit.ctc.editor.code.InternalSensorManager;
import jmri.jmrit.ctc.editor.code.OriginalCopy;
import jmri.jmrit.ctc.editor.code.ProgramProperties;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrit.ctc.ctcserialdata.CTCSerialData;
import jmri.jmrit.ctc.ctcserialdata.CodeButtonHandlerData;
import jmri.jmrit.ctc.ctcserialdata.OtherData;

/**
 *
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 */
// public class FrmMainForm extends jmri.util.JmriJFrame {
public class FrmMainForm extends JFrame {

    private static final String FORM_PROPERTIES = "FrmMainForm";    // NOI18N
    private CTCSerialData _mCTCSerialData;
    private OriginalCopy _mOriginalCopy;
    private Columns _mColumns;
    private DefaultListModel<String> _mDefaultListModel;
    private ProgramProperties _mProgramProperties;
    private AwtWindowProperties _mAwtWindowProperties;
    private CheckJMRIObject _mCheckJMRIObject;

    public boolean _mPanelLoaded = false;
    private boolean _mAnySubFormOpen = false;   // For any BUT FrmTRL_Rules
    boolean _mTRL_RulesFormOpen = false; // for ONLY FrmTRL_Rules

    @SuppressWarnings("LeakingThisInConstructor")   // NOI18N   Lazy, since this is NOT a multi-threaded program.
    public FrmMainForm() {
        super();
        InstanceManager.setDefault(jmri.jmrit.ctc.editor.gui.FrmMainForm.class, this);
        initComponents();
        CommonSubs.addHelpMenu(this, "package.jmri.jmrit.ctc.CTC", true);  // NOI18N
        _mAwtWindowProperties = new AwtWindowProperties(this, "AwtWindowProperties.txt", FORM_PROPERTIES); // NOI18N
        _mProgramProperties = new ProgramProperties();
        _mCheckJMRIObject = new CheckJMRIObject();
        newOrOpenFile(true);
        checkPanelStatus.actionPerformed(null);
        new javax.swing.Timer(5000, checkPanelStatus).start();
    }

    /**
     * Create a platform specific accelerator key stroke
     * @param keycode The integer value for the key from KeyEvent
     * @return The key stroke with the platform's accelerator character
     */
    private KeyStroke getAccelerator(int keycode) {
        int modifier = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        return KeyStroke.getKeyStroke(keycode, modifier);
    }

    /**
     * Set the panel status:  true: CTC enabled panel loaded, false = no CTC enabled panel loaded.
     * The presence of the debug and reload sensors is used to test for a potential CTC panel xml file.
     * Runs as a swing timer event and checks every 5 seconds.
     */
    java.awt.event.ActionListener checkPanelStatus = new java.awt.event.ActionListener() {
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        @Override
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            _mPanelLoaded = false;

            // Are two specific sensors present
            Sensor chkReload = sm.getSensor( _mCTCSerialData.getOtherData()._mCTCDebugSystemReloadInternalSensor);
            Sensor chkDebug = sm.getSensor( _mCTCSerialData.getOtherData()._mCTCDebug_TrafficLockingRuleTriggeredDisplayInternalSensor);

            if (chkReload == null || chkDebug == null) {
                // No CTC panel loaded
                _mJMRIValidationStatus.setText("<html><font color='red'>" + Bundle.getMessage("LabelDisabled") + "</font></html>");     // NOI18N
                _mCheckEverythingWithJMRI.setEnabled(false);
            } else {
                _mPanelLoaded = true;
                _mJMRIValidationStatus.setText("<html><font color='green'>" + Bundle.getMessage("LabelEnabled") + "</font></html>");    // NOI18N
                _mCheckEverythingWithJMRI.setEnabled(true);
            }
        }
    };

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     * Note:  The form editor will set the method private.  It must be public.
     */
    @SuppressWarnings("unchecked")  // NOI18N
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenuItem1 = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        _mPresentlyDefinedColumns = new javax.swing.JList<>();
        addButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        _mSIDI_Enabled = new javax.swing.JCheckBox();
        _mEdit_SIDI = new javax.swing.JButton();
        _mEdit_SIDI_Prompt = new javax.swing.JLabel();
        _mEdit_SIDL_Prompt = new javax.swing.JLabel();
        _mSIDL_Enabled = new javax.swing.JCheckBox();
        _mEdit_SIDL = new javax.swing.JButton();
        _mEdit_SWDI_Prompt = new javax.swing.JLabel();
        _mSWDI_Enabled = new javax.swing.JCheckBox();
        _mEdit_SWDI = new javax.swing.JButton();
        _mEdit_SWDL_Prompt = new javax.swing.JLabel();
        _mSWDL_Enabled = new javax.swing.JCheckBox();
        _mEdit_SWDL = new javax.swing.JButton();
        _mEdit_CO_Prompt = new javax.swing.JLabel();
        _mCO_Enabled = new javax.swing.JCheckBox();
        _mEdit_CO = new javax.swing.JButton();
        _mEdit_TUL_Prompt = new javax.swing.JLabel();
        _mTUL_Enabled = new javax.swing.JCheckBox();
        _mEdit_TUL = new javax.swing.JButton();
        _mEdit_IL_Prompt = new javax.swing.JLabel();
        _mIL_Enabled = new javax.swing.JCheckBox();
        _mEdit_IL = new javax.swing.JButton();
        reapplyPatternsButton = new javax.swing.JButton();
        _mEdit_TRL_Prompt = new javax.swing.JLabel();
        _mTRL_Enabled = new javax.swing.JCheckBox();
        _mEdit_TRL = new javax.swing.JButton();
        _mEdit_CB_Prompt = new javax.swing.JLabel();
        _mEdit_CB = new javax.swing.JButton();
        _mCB_EditAlwaysEnabled = new javax.swing.JLabel();
        changeNumbersButton = new javax.swing.JButton();
        _mButtonWriteXMLFiles = new javax.swing.JButton();
        _mMoveUp = new javax.swing.JButton();
        _mMoveDown = new javax.swing.JButton();
        _mCheckEverythingWithJMRI = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        _mJMRIValidationStatus = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        _mFile = new javax.swing.JMenu();
        _mNew = new javax.swing.JMenuItem();
        _mSave = new javax.swing.JMenuItem();
        _mQuitWithoutSaving = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        _mFindAndReplace = new javax.swing.JMenuItem();
        _mFixErrors = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        _mDebugging = new javax.swing.JMenuItem();
        _mDefaults = new javax.swing.JMenuItem();
        _mFleeting = new javax.swing.JMenuItem();
        _mPatterns = new javax.swing.JMenuItem();
        _mGUIDesign = new javax.swing.JMenuItem();
        _mHelp = new javax.swing.JMenu();
        _mHelpAbout = new javax.swing.JMenuItem();

        jMenuItem1.setText("jMenuItem1");

        jMenu3.setText("jMenu3");

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setLocation(new java.awt.Point(0, 0));
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jLabel1.setText(Bundle.getMessage("LabelDefined"));

        _mPresentlyDefinedColumns.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        _mPresentlyDefinedColumns.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                _mPresentlyDefinedColumnsValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(_mPresentlyDefinedColumns);

        addButton.setText(Bundle.getMessage("ButtonAdd"));
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        deleteButton.setText(Bundle.getMessage("ButtonDelete"));
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        _mSIDI_Enabled.setText(Bundle.getMessage("LabelEnabled"));
        _mSIDI_Enabled.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mSIDI_EnabledActionPerformed(evt);
            }
        });

        _mEdit_SIDI.setText(Bundle.getMessage("ButtonEdit"));
        _mEdit_SIDI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mEdit_SIDIActionPerformed(evt);
            }
        });

        _mEdit_SIDI_Prompt.setText(Bundle.getMessage("LabelSignalIndicators"));

        _mEdit_SIDL_Prompt.setText(Bundle.getMessage("LabelSignalLever"));

        _mSIDL_Enabled.setText(Bundle.getMessage("LabelEnabled"));
        _mSIDL_Enabled.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mSIDL_EnabledActionPerformed(evt);
            }
        });

        _mEdit_SIDL.setText(Bundle.getMessage("ButtonEdit"));
        _mEdit_SIDL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mEdit_SIDLActionPerformed(evt);
            }
        });

        _mEdit_SWDI_Prompt.setText(Bundle.getMessage("LabelSwitchIndicators"));

        _mSWDI_Enabled.setText(Bundle.getMessage("LabelEnabled"));
        _mSWDI_Enabled.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mSWDI_EnabledActionPerformed(evt);
            }
        });

        _mEdit_SWDI.setText(Bundle.getMessage("ButtonEdit"));
        _mEdit_SWDI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mEdit_SWDIActionPerformed(evt);
            }
        });

        _mEdit_SWDL_Prompt.setText(Bundle.getMessage("LabelSwitchLever"));

        _mSWDL_Enabled.setText(Bundle.getMessage("LabelEnabled"));
        _mSWDL_Enabled.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mSWDL_EnabledActionPerformed(evt);
            }
        });

        _mEdit_SWDL.setText(Bundle.getMessage("ButtonEdit"));
        _mEdit_SWDL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mEdit_SWDLActionPerformed(evt);
            }
        });

        _mEdit_CO_Prompt.setText(Bundle.getMessage("LabelCallOn"));

        _mCO_Enabled.setText(Bundle.getMessage("LabelEnabled"));
        _mCO_Enabled.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mCO_EnabledActionPerformed(evt);
            }
        });

        _mEdit_CO.setText(Bundle.getMessage("ButtonEdit"));
        _mEdit_CO.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mEdit_COActionPerformed(evt);
            }
        });

        _mEdit_TUL_Prompt.setText(Bundle.getMessage("LabelTurnoutLock"));

        _mTUL_Enabled.setText(Bundle.getMessage("LabelEnabled"));
        _mTUL_Enabled.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mTUL_EnabledActionPerformed(evt);
            }
        });

        _mEdit_TUL.setText(Bundle.getMessage("ButtonEdit"));
        _mEdit_TUL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mEdit_TULActionPerformed(evt);
            }
        });

        _mEdit_IL_Prompt.setText(Bundle.getMessage("LabelIndicatorLock"));

        _mIL_Enabled.setText(Bundle.getMessage("LabelEnabled"));
        _mIL_Enabled.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mIL_EnabledActionPerformed(evt);
            }
        });

        _mEdit_IL.setText(Bundle.getMessage("ButtonEdit"));
        _mEdit_IL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mEdit_ILActionPerformed(evt);
            }
        });

        reapplyPatternsButton.setText(Bundle.getMessage("ButtonReapplyItem"));
        reapplyPatternsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reapplyPatternsButtonActionPerformed(evt);
            }
        });

        _mEdit_TRL_Prompt.setText(Bundle.getMessage("LabelTrafficLock"));

        _mTRL_Enabled.setText(Bundle.getMessage("LabelEnabled"));
        _mTRL_Enabled.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mTRL_EnabledActionPerformed(evt);
            }
        });

        _mEdit_TRL.setText(Bundle.getMessage("ButtonEdit"));
        _mEdit_TRL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mEdit_TRLActionPerformed(evt);
            }
        });

        _mEdit_CB_Prompt.setText(Bundle.getMessage("LabelCode"));

        _mEdit_CB.setText(Bundle.getMessage("ButtonEdit"));
        _mEdit_CB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mEdit_CBActionPerformed(evt);
            }
        });

        _mCB_EditAlwaysEnabled.setText(Bundle.getMessage("InfoEnabled"));

        changeNumbersButton.setText(Bundle.getMessage("ButtonChange"));
        changeNumbersButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeNumbersButtonActionPerformed(evt);
            }
        });

        _mButtonWriteXMLFiles.setText(Bundle.getMessage("ButtonXmlFiles"));
        _mButtonWriteXMLFiles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mButtonWriteXMLFilesActionPerformed(evt);
            }
        });

        _mMoveUp.setText(Bundle.getMessage("ButtonMoveUp"));
        _mMoveUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mMoveUpActionPerformed(evt);
            }
        });

        _mMoveDown.setText(Bundle.getMessage("ButtonMoveDown"));
        _mMoveDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mMoveDownActionPerformed(evt);
            }
        });

        _mCheckEverythingWithJMRI.setText(Bundle.getMessage("ButtonCheck"));
        _mCheckEverythingWithJMRI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mCheckEverythingWithJMRIActionPerformed(evt);
            }
        });

        jLabel2.setText(Bundle.getMessage("LabelValidation"));

        _mJMRIValidationStatus.setText("Unknown");
        _mJMRIValidationStatus.setName("_mJMRIValidationStatus"); // NOI18N

        _mFile.setText(Bundle.getMessage("MenuFile"));

        _mNew.setAccelerator(getAccelerator(KeyEvent.VK_N));
        _mNew.setText(Bundle.getMessage("MenuNew"));
        _mNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mNewActionPerformed(evt);
            }
        });
        _mFile.add(_mNew);

        _mSave.setAccelerator(getAccelerator(KeyEvent.VK_S));
        _mSave.setText(Bundle.getMessage("MenuSave"));
        _mSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mSaveActionPerformed(evt);
            }
        });
        _mFile.add(_mSave);

        _mQuitWithoutSaving.setAccelerator(getAccelerator(KeyEvent.VK_E));
        _mQuitWithoutSaving.setText(Bundle.getMessage("MenuExit"));
        _mQuitWithoutSaving.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mQuitWithoutSavingActionPerformed(evt);
            }
        });
        _mFile.add(_mQuitWithoutSaving);

        jMenuBar1.add(_mFile);

        jMenu2.setText(Bundle.getMessage("MenuEdit"));

        _mFindAndReplace.setAccelerator(getAccelerator(KeyEvent.VK_F));
        _mFindAndReplace.setText(Bundle.getMessage("MenuFind"));
        _mFindAndReplace.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mFindAndReplaceActionPerformed(evt);
            }
        });
        jMenu2.add(_mFindAndReplace);

        _mFixErrors.setText(Bundle.getMessage("MenuFix"));
        _mFixErrors.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mFixErrorsActionPerformed(evt);
            }
        });
        jMenu2.add(_mFixErrors);

        jMenuBar1.add(jMenu2);

        jMenu1.setText(Bundle.getMessage("MenuConfigure"));

        _mDebugging.setText(Bundle.getMessage("MenuDebugging"));
        _mDebugging.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mDebuggingActionPerformed(evt);
            }
        });
        jMenu1.add(_mDebugging);

        _mDefaults.setText(Bundle.getMessage("MenuDefaults"));
        _mDefaults.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mDefaultsActionPerformed(evt);
            }
        });
        jMenu1.add(_mDefaults);

        _mFleeting.setText(Bundle.getMessage("MenuFleeting"));
        _mFleeting.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mFleetingActionPerformed(evt);
            }
        });
        jMenu1.add(_mFleeting);

        _mPatterns.setText(Bundle.getMessage("MenuPatterns"));
        _mPatterns.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mPatternsActionPerformed(evt);
            }
        });
        jMenu1.add(_mPatterns);

        _mGUIDesign.setText(Bundle.getMessage("MenuDesign"));
        _mGUIDesign.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mGUIDesignActionPerformed(evt);
            }
        });
        jMenu1.add(_mGUIDesign);

        jMenuBar1.add(jMenu1);

        _mHelp.setText(Bundle.getMessage("MenuAbout"));

        _mHelpAbout.setText(Bundle.getMessage("MenuAbout"));
        _mHelpAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _mHelpAboutActionPerformed(evt);
            }
        });
        _mHelp.add(_mHelpAbout);

        jMenuBar1.add(_mHelp);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(_mEdit_CB_Prompt)
                                    .addComponent(_mEdit_CB)
                                    .addComponent(_mCB_EditAlwaysEnabled))
                                .addGap(27, 27, 27)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(_mEdit_SIDI_Prompt)
                                    .addComponent(_mSIDI_Enabled)
                                    .addComponent(_mEdit_SIDI)))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(_mEdit_CO_Prompt)
                                    .addComponent(_mCO_Enabled)
                                    .addComponent(_mEdit_CO))
                                .addGap(43, 43, 43)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(_mEdit_TRL)
                                    .addComponent(_mTRL_Enabled)
                                    .addComponent(_mEdit_TRL_Prompt))))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(_mEdit_SIDL_Prompt)
                            .addComponent(_mSIDL_Enabled)
                            .addComponent(_mEdit_SIDL)
                            .addComponent(_mEdit_TUL_Prompt)
                            .addComponent(_mTUL_Enabled)
                            .addComponent(_mEdit_TUL))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(_mEdit_IL)
                            .addComponent(_mIL_Enabled)
                            .addComponent(_mEdit_IL_Prompt)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(_mEdit_SWDI_Prompt)
                                    .addComponent(_mSWDI_Enabled)
                                    .addComponent(_mEdit_SWDI))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(_mEdit_SWDL)
                                    .addComponent(_mSWDL_Enabled)
                                    .addComponent(_mEdit_SWDL_Prompt))))
                        .addGap(0, 174, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addGap(172, 172, 172)
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(_mJMRIValidationStatus)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jScrollPane1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(_mButtonWriteXMLFiles)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addGroup(layout.createSequentialGroup()
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(deleteButton)
                                                .addComponent(addButton, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(_mCheckEverythingWithJMRI))
                                        .addComponent(changeNumbersButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(reapplyPatternsButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(_mMoveUp, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(_mMoveDown, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(_mJMRIValidationStatus))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 334, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(addButton)
                                .addGap(18, 18, 18)
                                .addComponent(deleteButton))
                            .addComponent(_mCheckEverythingWithJMRI, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addComponent(reapplyPatternsButton)
                        .addGap(18, 18, 18)
                        .addComponent(changeNumbersButton)
                        .addGap(18, 18, 18)
                        .addComponent(_mMoveUp)
                        .addGap(18, 18, 18)
                        .addComponent(_mMoveDown)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(_mButtonWriteXMLFiles)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(_mEdit_CB_Prompt)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(_mEdit_SIDI_Prompt)
                        .addComponent(_mEdit_SIDL_Prompt)
                        .addComponent(_mEdit_SWDI_Prompt)
                        .addComponent(_mEdit_SWDL_Prompt)))
                .addGap(3, 3, 3)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mSIDI_Enabled)
                    .addComponent(_mSIDL_Enabled)
                    .addComponent(_mSWDI_Enabled)
                    .addComponent(_mSWDL_Enabled)
                    .addComponent(_mCB_EditAlwaysEnabled))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mEdit_SIDI)
                    .addComponent(_mEdit_SIDL)
                    .addComponent(_mEdit_SWDI)
                    .addComponent(_mEdit_SWDL)
                    .addComponent(_mEdit_CB))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mEdit_CO_Prompt)
                    .addComponent(_mEdit_TRL_Prompt)
                    .addComponent(_mEdit_TUL_Prompt)
                    .addComponent(_mEdit_IL_Prompt))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mCO_Enabled)
                    .addComponent(_mTRL_Enabled)
                    .addComponent(_mTUL_Enabled)
                    .addComponent(_mIL_Enabled))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_mEdit_CO)
                    .addComponent(_mEdit_TRL)
                    .addComponent(_mEdit_TUL)
                    .addComponent(_mEdit_IL))
                .addContainerGap(20, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public void shutdown() {
        jmri.InstanceManager.reset(jmri.jmrit.ctc.editor.gui.FrmMainForm.class);
        dispose();
    }

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        if (_mAnySubFormOpen) return;
        _mAnySubFormOpen = true;
//  Pre-scan and find the highest switch number used so far, and highest column number:
        int highestSwitchNumber = _mCTCSerialData.findHighestSwitchNumberUsedSoFar();
        int highestColumnNumber = _mCTCSerialData.findHighestColumnNumberUsedSoFar();
        InternalSensorManager internalSensorManager = new InternalSensorManager(_mCTCSerialData);
        FrmAddModifyCTCColumn dialog = new FrmAddModifyCTCColumn(_mAwtWindowProperties, _mColumns, false, highestSwitchNumber + 2, highestColumnNumber + 1, false);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (dialog.closedNormally()) {
                    _mCTCSerialData.addCodeButtonHandlerData(CodeButtonHandlerDataRoutines.createNewCodeButtonHandlerData(_mCTCSerialData.getUniqueNumber(), dialog._mNewSwitchNumber, dialog._mNewSignalEtcNumber, dialog._mNewGUIColumnNumber, _mProgramProperties));
                    internalSensorManager.checkForChanges(_mCTCSerialData);
                    _mColumns.updateFrame();
                }
                _mAnySubFormOpen = false;
            }
        });
        dialog.setVisible(true);  // MUST BE AFTER "addWindowListener"!  BUG IN AWT/SWING!
    }//GEN-LAST:event_addButtonActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        if ((_mPresentlyDefinedColumns.getSelectedValue()).contains(Columns.REFERENCES_PRESENT_INDICATOR)) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("FrmMainFormReferencesExist"));   // NOI18N
            return; // Do nothing!
        }
        int selectedIndex = _mPresentlyDefinedColumns.getSelectedIndex();
        if (selectedIndex != -1) { // Safety:
            _mCTCSerialData.removeCodeButtonHandlerData(selectedIndex);
            _mColumns.updateFrame();
        }
    }//GEN-LAST:event_deleteButtonActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        if (!validToSaveAtThisTime(Bundle.getMessage("FrmMainFormAutoSaveError1"), Bundle.getMessage("FrmMainFormAutoSaveError2"))) return; // NOI18N
        _mCTCSerialData.writeDataToXMLFile(_mProgramProperties._mFilename);
        _mProgramProperties.close();
        _mAwtWindowProperties.saveWindowStateAndClose(this, FORM_PROPERTIES);
        shutdown();
    }//GEN-LAST:event_formWindowClosing

    private void _mPresentlyDefinedColumnsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event__mPresentlyDefinedColumnsValueChanged
        if (!evt.getValueIsAdjusting()) { // returns false is FINAL in chain.
            int selectedIndex = _mPresentlyDefinedColumns.getSelectedIndex();
//  Who designed this steaming pile of XXXX called Swing?  I guess at some level this makes sense:
//  It seems that "_mDefaultListModel.clear();" and "_mDefaultListModel.addElement..." BOTH
//  causes this routine to be called repeatedly with a -1 each time for selectedIndex.  In fact, for EACH
//  "_mDefaultListModel.addElement..." it calls us with -1.  Go figure!  Why -1 in that case?
//  Isn't one being added that makes sense at a value >= 0?  Then why call us at all?  Sigh......
            _mColumns.setEntrySelected(selectedIndex);
        }
    }//GEN-LAST:event__mPresentlyDefinedColumnsValueChanged

    private void _mSIDI_EnabledActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mSIDI_EnabledActionPerformed
        _mColumns.sidi_EnabledClicked(_mSIDI_Enabled.isSelected());
    }//GEN-LAST:event__mSIDI_EnabledActionPerformed

    private void _mSIDL_EnabledActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mSIDL_EnabledActionPerformed
        _mColumns.sidl_EnabledClicked(_mSIDL_Enabled.isSelected());
    }//GEN-LAST:event__mSIDL_EnabledActionPerformed

    private void _mSWDI_EnabledActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mSWDI_EnabledActionPerformed
        _mColumns.swdi_EnabledClicked(_mSWDI_Enabled.isSelected());
    }//GEN-LAST:event__mSWDI_EnabledActionPerformed

    private void _mSWDL_EnabledActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mSWDL_EnabledActionPerformed
        _mColumns.swdl_EnabledClicked(_mSWDL_Enabled.isSelected());
    }//GEN-LAST:event__mSWDL_EnabledActionPerformed

    private void _mCO_EnabledActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mCO_EnabledActionPerformed
        _mColumns.co_EnabledClicked(_mCO_Enabled.isSelected());
    }//GEN-LAST:event__mCO_EnabledActionPerformed

    private void _mTUL_EnabledActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mTUL_EnabledActionPerformed
        _mColumns.tul_EnabledClicked(_mTUL_Enabled.isSelected());
    }//GEN-LAST:event__mTUL_EnabledActionPerformed

    private void _mIL_EnabledActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mIL_EnabledActionPerformed
        _mColumns.il_EnabledClicked(_mIL_Enabled.isSelected());
    }//GEN-LAST:event__mIL_EnabledActionPerformed

    private void _mEdit_SIDIActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mEdit_SIDIActionPerformed
        if (_mAnySubFormOpen) return;
        _mAnySubFormOpen = true;
        InternalSensorManager internalSensorManager = new InternalSensorManager(_mCTCSerialData);
        FrmSIDI dialog = new FrmSIDI(    _mAwtWindowProperties, _mColumns.getSelectedCodeButtonHandlerData(), _mProgramProperties, _mCheckJMRIObject,
                                         _mCTCSerialData.getOtherData()._mSignalSystemType == OtherData.SIGNAL_SYSTEM_TYPE.SIGNALHEAD);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (dialog.closedNormally()) {
                    internalSensorManager.checkForChanges(_mCTCSerialData);
                    _mColumns.updateFrame();
                }
                _mAnySubFormOpen = false;
            }
        });
        dialog.setVisible(true);
    }//GEN-LAST:event__mEdit_SIDIActionPerformed

    private void _mEdit_SIDLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mEdit_SIDLActionPerformed
        if (_mAnySubFormOpen) return;
        _mAnySubFormOpen = true;
        InternalSensorManager internalSensorManager = new InternalSensorManager(_mCTCSerialData);
        FrmSIDL dialog = new FrmSIDL(_mAwtWindowProperties, _mColumns.getSelectedCodeButtonHandlerData(), _mProgramProperties, _mCheckJMRIObject);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (dialog.closedNormally()) {
                    internalSensorManager.checkForChanges(_mCTCSerialData);
                    _mColumns.updateFrame();
                }
                _mAnySubFormOpen = false;
            }
        });
        dialog.setVisible(true);  // MUST BE AFTER "addWindowListener"!  BUG IN AWT/SWING!
    }//GEN-LAST:event__mEdit_SIDLActionPerformed

    private void _mEdit_SWDIActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mEdit_SWDIActionPerformed
        if (_mAnySubFormOpen) return;
        _mAnySubFormOpen = true;
        InternalSensorManager internalSensorManager = new InternalSensorManager(_mCTCSerialData);
        FrmSWDI dialog = new FrmSWDI(_mAwtWindowProperties, _mColumns.getSelectedCodeButtonHandlerData(), _mProgramProperties, _mCheckJMRIObject);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (dialog.closedNormally()) {
                    internalSensorManager.checkForChanges(_mCTCSerialData);
                    _mColumns.updateFrame();
                }
                _mAnySubFormOpen = false;
            }
        });
        dialog.setVisible(true);  // MUST BE AFTER "addWindowListener"!  BUG IN AWT/SWING!
    }//GEN-LAST:event__mEdit_SWDIActionPerformed

    private void _mEdit_SWDLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mEdit_SWDLActionPerformed
        if (_mAnySubFormOpen) return;
        _mAnySubFormOpen = true;
        InternalSensorManager internalSensorManager = new InternalSensorManager(_mCTCSerialData);
        FrmSWDL dialog = new FrmSWDL(_mAwtWindowProperties, _mColumns.getSelectedCodeButtonHandlerData(), _mProgramProperties, _mCheckJMRIObject);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (dialog.closedNormally()) {
                    internalSensorManager.checkForChanges(_mCTCSerialData);
                    _mColumns.updateFrame();
                }
                _mAnySubFormOpen = false;
            }
        });
        dialog.setVisible(true);  // MUST BE AFTER "addWindowListener"!  BUG IN AWT/SWING!
    }//GEN-LAST:event__mEdit_SWDLActionPerformed

    private void _mEdit_COActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mEdit_COActionPerformed
        if (_mAnySubFormOpen) return;
        _mAnySubFormOpen = true;
        InternalSensorManager internalSensorManager = new InternalSensorManager(_mCTCSerialData);
        FrmCO dialog = new FrmCO(   _mAwtWindowProperties, _mColumns.getSelectedCodeButtonHandlerData(),
                                    _mProgramProperties, _mCTCSerialData, _mCheckJMRIObject,
                                     _mCTCSerialData.getOtherData()._mSignalSystemType == OtherData.SIGNAL_SYSTEM_TYPE.SIGNALHEAD);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (dialog.closedNormally()) {
                    internalSensorManager.checkForChanges(_mCTCSerialData);
                    _mColumns.updateFrame();
                }
                _mAnySubFormOpen = false;
            }
        });
        dialog.setVisible(true);  // MUST BE AFTER "addWindowListener"!  BUG IN AWT/SWING!
    }//GEN-LAST:event__mEdit_COActionPerformed

    private void _mEdit_TULActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mEdit_TULActionPerformed
        if (_mAnySubFormOpen) return;
        _mAnySubFormOpen = true;
        InternalSensorManager internalSensorManager = new InternalSensorManager(_mCTCSerialData);
        FrmTUL dialog = new FrmTUL(_mAwtWindowProperties, _mColumns.getSelectedCodeButtonHandlerData(), _mProgramProperties, _mCheckJMRIObject);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (dialog.closedNormally()) {
                    internalSensorManager.checkForChanges(_mCTCSerialData);
                    _mColumns.updateFrame();
                }
                _mAnySubFormOpen = false;
            }
        });
        dialog.setVisible(true);  // MUST BE AFTER "addWindowListener"!  BUG IN AWT/SWING!
    }//GEN-LAST:event__mEdit_TULActionPerformed

    private void _mEdit_ILActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mEdit_ILActionPerformed
        if (_mAnySubFormOpen) return;
        _mAnySubFormOpen = true;
        InternalSensorManager internalSensorManager = new InternalSensorManager(_mCTCSerialData);
        FrmIL dialog = new FrmIL(   _mAwtWindowProperties, _mColumns.getSelectedCodeButtonHandlerData(), _mCheckJMRIObject,
                                    _mCTCSerialData.getOtherData()._mSignalSystemType == OtherData.SIGNAL_SYSTEM_TYPE.SIGNALHEAD,
                                    _mCTCSerialData);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (dialog.closedNormally()) {
                    internalSensorManager.checkForChanges(_mCTCSerialData);
                    _mColumns.updateFrame();
                }
                _mAnySubFormOpen = false;
            }
        });
        dialog.setVisible(true);  // MUST BE AFTER "addWindowListener"!  BUG IN AWT/SWING!
    }//GEN-LAST:event__mEdit_ILActionPerformed

    private void reapplyPatternsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reapplyPatternsButtonActionPerformed
        if (JOptionPane.showConfirmDialog(this, Bundle.getMessage("FrmMainFormConfirm"),
                Bundle.getMessage("WarningTitle"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {  // NOI18N
            int index = _mColumns.getEntrySelectedIndex();
            CodeButtonHandlerData codeButtonHandlerData = _mCTCSerialData.getCodeButtonHandlerData(index);
            codeButtonHandlerData = CodeButtonHandlerDataRoutines.updateExistingCodeButtonHandlerDataWithSubstitutedData(_mProgramProperties, codeButtonHandlerData);
            _mCTCSerialData.setCodeButtonHandlerData(index, codeButtonHandlerData);
        }
    }//GEN-LAST:event_reapplyPatternsButtonActionPerformed

    private void _mTRL_EnabledActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mTRL_EnabledActionPerformed
        _mColumns.trl_EnabledClicked(_mTRL_Enabled.isSelected());
    }//GEN-LAST:event__mTRL_EnabledActionPerformed

    private void _mEdit_TRLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mEdit_TRLActionPerformed
        if (_mAnySubFormOpen) return;
        _mAnySubFormOpen = true;
        CodeButtonHandlerData codeButtonHandlerDataSelected = _mColumns.getSelectedCodeButtonHandlerData();
        InternalSensorManager internalSensorManager = new InternalSensorManager(_mCTCSerialData);
        FrmTRL dialog = new FrmTRL( _mAwtWindowProperties, codeButtonHandlerDataSelected,
                                    _mCTCSerialData, _mCheckJMRIObject);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (dialog.closedNormally()) {
                    internalSensorManager.checkForChanges(_mCTCSerialData);
                    _mColumns.updateFrame();
                }
                _mAnySubFormOpen = false;
            }
        });
        dialog.setVisible(true);  // MUST BE AFTER "addWindowListener"!  BUG IN AWT/SWING!
    }//GEN-LAST:event__mEdit_TRLActionPerformed

    private void _mEdit_CBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mEdit_CBActionPerformed
        if (_mAnySubFormOpen) return;
        _mAnySubFormOpen = true;
        InternalSensorManager internalSensorManager = new InternalSensorManager(_mCTCSerialData);
        FrmCB dialog = new FrmCB(   _mAwtWindowProperties, _mColumns.getSelectedCodeButtonHandlerData(),
                                    _mProgramProperties, _mCTCSerialData, _mCheckJMRIObject);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (dialog.closedNormally()) {
                    internalSensorManager.checkForChanges(_mCTCSerialData);
                    _mColumns.updateFrame();
                }
                _mAnySubFormOpen = false;
            }
        });
        dialog.setVisible(true);  // MUST BE AFTER "addWindowListener"!  BUG IN AWT/SWING!
    }//GEN-LAST:event__mEdit_CBActionPerformed

    private void changeNumbersButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeNumbersButtonActionPerformed
        if (_mAnySubFormOpen) return;
        _mAnySubFormOpen = true;
        CodeButtonHandlerData temp = _mColumns.getSelectedCodeButtonHandlerData();
        InternalSensorManager internalSensorManager = new InternalSensorManager(_mCTCSerialData);
        FrmAddModifyCTCColumn dialog = new FrmAddModifyCTCColumn(_mAwtWindowProperties, _mColumns, true, temp._mSwitchNumber, temp._mGUIColumnNumber, temp._mGUIGeneratedAtLeastOnceAlready);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (dialog.closedNormally()) {
                    _mCTCSerialData.updateSwitchAndSignalEtcNumbersEverywhere(_mColumns.getEntrySelectedIndex(), dialog._mNewSwitchNumber, dialog._mNewSignalEtcNumber, dialog._mNewGUIColumnNumber, dialog._mNewGUIGeneratedAtLeastOnceAlready);
                    internalSensorManager.checkForChanges(_mCTCSerialData);
                    _mColumns.updateFrame();
                }
                _mAnySubFormOpen = false;
            }
        });
        dialog.setVisible(true);    // MUST BE AFTER "addWindowListener"!  BUG IN AWT/SWING!
    }//GEN-LAST:event_changeNumbersButtonActionPerformed

    private void _mButtonWriteXMLFilesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mButtonWriteXMLFilesActionPerformed
        CreateXMLFiles createTextFile = new CreateXMLFiles( _mCTCSerialData.getOtherData(), _mCTCSerialData.getCodeButtonHandlerDataArrayList());
        createTextFile.createTextFiles(CommonSubs.getDirectoryOnly(_mProgramProperties._mFilename));
        _mColumns.updateFrame();
    }//GEN-LAST:event__mButtonWriteXMLFilesActionPerformed

    private void newOrOpenFile(boolean openExisting) {
        _mCTCSerialData = new CTCSerialData();
        _mOriginalCopy = new OriginalCopy();
        if (openExisting) {
            _mCTCSerialData.readDataFromXMLFile(_mProgramProperties._mFilename);
            _mOriginalCopy.makeDeepCopy(_mCTCSerialData);
        } else _mProgramProperties._mFilename = ProgramProperties.FILENAME_DEFAULT;
        setTitle(Bundle.getMessage("TitleMainForm") + "   " + _mProgramProperties._mFilename );     // NOI18N
        _mDefaultListModel = new DefaultListModel<>();
        _mPresentlyDefinedColumns.setModel(_mDefaultListModel);
        _mColumns = new Columns(_mCTCSerialData, _mCheckJMRIObject, _mDefaultListModel,
                                deleteButton, reapplyPatternsButton, changeNumbersButton,
                                _mMoveUp, _mMoveDown,
                                _mEdit_CB_Prompt, _mCB_EditAlwaysEnabled, _mEdit_CB,
                                _mEdit_SIDI_Prompt, _mSIDI_Enabled, _mEdit_SIDI,
                                _mEdit_SIDL_Prompt, _mSIDL_Enabled, _mEdit_SIDL,
                                _mEdit_SWDI_Prompt, _mSWDI_Enabled, _mEdit_SWDI,
                                _mEdit_SWDL_Prompt, _mSWDL_Enabled, _mEdit_SWDL,
                                _mEdit_CO_Prompt, _mCO_Enabled, _mEdit_CO,
                                _mEdit_TRL_Prompt, _mTRL_Enabled, _mEdit_TRL,
                                _mEdit_TUL_Prompt, _mTUL_Enabled, _mEdit_TUL,
                                _mEdit_IL_Prompt, _mIL_Enabled, _mEdit_IL);
    }

    private void _mNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mNewActionPerformed
            newOrOpenFile(false);
    }//GEN-LAST:event__mNewActionPerformed

    private void _mSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mSaveActionPerformed
        if (!validToSaveAtThisTime(Bundle.getMessage("MenuSave"), "")) return;  // NOI18N
        _mCTCSerialData.writeDataToXMLFile(_mProgramProperties._mFilename);
        _mOriginalCopy.makeDeepCopy(_mCTCSerialData);
    }//GEN-LAST:event__mSaveActionPerformed

    private boolean validToSaveAtThisTime(String whatIsTriggeringSave, String hint) {
        if (_mColumns.anyErrorsPresent()) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("FrmMainFormValidError1") + whatIsTriggeringSave + Bundle.getMessage("FrmMainFormValidError2") + hint, Bundle.getMessage("FrmMainFormValidError3"), JOptionPane.ERROR_MESSAGE);   // NOI18N
            return false;
        }
        return true;
    }

    private void _mFindAndReplaceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mFindAndReplaceActionPerformed
        if (_mAnySubFormOpen) return;
        _mAnySubFormOpen = true;
        FrmFindAndReplace dialog = new FrmFindAndReplace(_mAwtWindowProperties, _mCTCSerialData);
        InternalSensorManager.doDialog(dialog, _mCTCSerialData);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                _mAnySubFormOpen = false;
            }
        });
    }//GEN-LAST:event__mFindAndReplaceActionPerformed

    private void _mFleetingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mFleetingActionPerformed
        if (_mAnySubFormOpen) return;
        _mAnySubFormOpen = true;
        FrmFleeting dialog = new FrmFleeting(_mAwtWindowProperties,  _mCTCSerialData.getOtherData());
        InternalSensorManager.doDialog(dialog, _mCTCSerialData);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                _mAnySubFormOpen = false;
            }
        });
    }//GEN-LAST:event__mFleetingActionPerformed

    private void _mPatternsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mPatternsActionPerformed
        if (_mAnySubFormOpen) return;
        _mAnySubFormOpen = true;
        FrmPatterns dialog = new FrmPatterns(_mAwtWindowProperties, _mProgramProperties);
        InternalSensorManager.doDialog(dialog, _mCTCSerialData);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                _mAnySubFormOpen = false;
            }
        });
    }//GEN-LAST:event__mPatternsActionPerformed

    private void _mDefaultsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mDefaultsActionPerformed
        if (_mAnySubFormOpen) return;
        _mAnySubFormOpen = true;
        FrmDefaults dialog = new FrmDefaults(_mAwtWindowProperties, _mProgramProperties,  _mCTCSerialData.getOtherData());
        InternalSensorManager.doDialog(dialog, _mCTCSerialData);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                _mAnySubFormOpen = false;
            }
        });
    }//GEN-LAST:event__mDefaultsActionPerformed

    private void _mDebuggingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mDebuggingActionPerformed
        if (_mAnySubFormOpen) return;
        _mAnySubFormOpen = true;
        FrmDebugging dialog = new FrmDebugging(_mAwtWindowProperties,  _mCTCSerialData.getOtherData());
        InternalSensorManager.doDialog(dialog, _mCTCSerialData);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                _mAnySubFormOpen = false;
            }
        });
    }//GEN-LAST:event__mDebuggingActionPerformed

    private void _mGUIDesignActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mGUIDesignActionPerformed
        if (_mAnySubFormOpen) return;
        _mAnySubFormOpen = true;
        FrmGUIDesign dialog = new FrmGUIDesign(_mAwtWindowProperties,  _mCTCSerialData.getOtherData());
        InternalSensorManager.doDialog(dialog, _mCTCSerialData);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                _mAnySubFormOpen = false;
            }
        });
    }//GEN-LAST:event__mGUIDesignActionPerformed

    private void _mMoveUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mMoveUpActionPerformed
        int selectedIndex = _mPresentlyDefinedColumns.getSelectedIndex();
        if (selectedIndex != -1) { // Safety:
            _mCTCSerialData.moveUp(selectedIndex);
            _mColumns.updateFrame();
            if (selectedIndex > 0) selectedIndex--;
            _mPresentlyDefinedColumns.setSelectedIndex(selectedIndex);
        }
    }//GEN-LAST:event__mMoveUpActionPerformed

    private void _mMoveDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mMoveDownActionPerformed
        int selectedIndex = _mPresentlyDefinedColumns.getSelectedIndex();
        if (selectedIndex != -1) { // Safety:
            _mCTCSerialData.moveDown(selectedIndex);
            _mColumns.updateFrame();
            if (selectedIndex != _mCTCSerialData.getCodeButtonHandlerDataSize() - 1) selectedIndex++;
            _mPresentlyDefinedColumns.setSelectedIndex(selectedIndex);
        }
    }//GEN-LAST:event__mMoveDownActionPerformed

    private void _mQuitWithoutSavingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mQuitWithoutSavingActionPerformed
        if (_mOriginalCopy.changed(_mCTCSerialData)) {
            if (JOptionPane.showConfirmDialog(this, Bundle.getMessage("FrmMainFormFileModWarn1"),
                    Bundle.getMessage("WarningTitle"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) { // NOI18N
                shutdown();
            }
        } else {    // No changes, just close.
            shutdown();
        }
    }//GEN-LAST:event__mQuitWithoutSavingActionPerformed

    private void _mFixErrorsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mFixErrorsActionPerformed
        if (_mAnySubFormOpen) return;
        _mAnySubFormOpen = true;
        FrmFixErrors dialog = new FrmFixErrors(_mAwtWindowProperties, _mColumns);
        InternalSensorManager.doDialog(dialog, _mCTCSerialData);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                _mAnySubFormOpen = false;
            }
        });
    }//GEN-LAST:event__mFixErrorsActionPerformed

    private void _mHelpAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mHelpAboutActionPerformed
        if (_mAnySubFormOpen) return;
        _mAnySubFormOpen = true;
        FrmAbout dialog = new FrmAbout(_mAwtWindowProperties);
        InternalSensorManager.doDialog(dialog, _mCTCSerialData);    // Technically don't modify anything, but for consistency
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                _mAnySubFormOpen = false;
            }
        });
    }//GEN-LAST:event__mHelpAboutActionPerformed

    private void _mCheckEverythingWithJMRIActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__mCheckEverythingWithJMRIActionPerformed
        boolean showErrors = JOptionPane.showConfirmDialog(this, Bundle.getMessage("FrmMainFormSeeErrors"), Bundle.getMessage("Info"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;    // NOI18N
        _mColumns.updateFrame();    // Sets ERROR_STRING at the end of each line.
        if (showErrors) {
            ArrayList<String> errors = new ArrayList<>();
            _mCTCSerialData.getCodeButtonHandlerDataArrayList().forEach((codeButtonHandlerData) -> {
                _mCheckJMRIObject.analyzeClass(codeButtonHandlerData, errors);
            });
            if (!errors.isEmpty()) {
                StringBuilder stringBuffer = new StringBuilder();
                errors.forEach((error) -> {
                    stringBuffer.append(error).append("\n");
                });
                JOptionPane.showMessageDialog(this, stringBuffer.toString());
            }
        }
    }//GEN-LAST:event__mCheckEverythingWithJMRIActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton _mButtonWriteXMLFiles;
    private javax.swing.JLabel _mCB_EditAlwaysEnabled;
    private javax.swing.JCheckBox _mCO_Enabled;
    private javax.swing.JButton _mCheckEverythingWithJMRI;
    private javax.swing.JMenuItem _mDebugging;
    private javax.swing.JMenuItem _mDefaults;
    private javax.swing.JButton _mEdit_CB;
    private javax.swing.JLabel _mEdit_CB_Prompt;
    private javax.swing.JButton _mEdit_CO;
    private javax.swing.JLabel _mEdit_CO_Prompt;
    private javax.swing.JButton _mEdit_IL;
    private javax.swing.JLabel _mEdit_IL_Prompt;
    private javax.swing.JButton _mEdit_SIDI;
    private javax.swing.JLabel _mEdit_SIDI_Prompt;
    private javax.swing.JButton _mEdit_SIDL;
    private javax.swing.JLabel _mEdit_SIDL_Prompt;
    private javax.swing.JButton _mEdit_SWDI;
    private javax.swing.JLabel _mEdit_SWDI_Prompt;
    private javax.swing.JButton _mEdit_SWDL;
    private javax.swing.JLabel _mEdit_SWDL_Prompt;
    private javax.swing.JButton _mEdit_TRL;
    private javax.swing.JLabel _mEdit_TRL_Prompt;
    private javax.swing.JButton _mEdit_TUL;
    private javax.swing.JLabel _mEdit_TUL_Prompt;
    private javax.swing.JMenu _mFile;
    private javax.swing.JMenuItem _mFindAndReplace;
    private javax.swing.JMenuItem _mFixErrors;
    private javax.swing.JMenuItem _mFleeting;
    private javax.swing.JMenuItem _mGUIDesign;
    private javax.swing.JMenu _mHelp;
    private javax.swing.JMenuItem _mHelpAbout;
    private javax.swing.JCheckBox _mIL_Enabled;
    private javax.swing.JLabel _mJMRIValidationStatus;
    private javax.swing.JButton _mMoveDown;
    private javax.swing.JButton _mMoveUp;
    private javax.swing.JMenuItem _mNew;
    private javax.swing.JMenuItem _mPatterns;
    private javax.swing.JList<String> _mPresentlyDefinedColumns;
    private javax.swing.JMenuItem _mQuitWithoutSaving;
    private javax.swing.JCheckBox _mSIDI_Enabled;
    private javax.swing.JCheckBox _mSIDL_Enabled;
    private javax.swing.JCheckBox _mSWDI_Enabled;
    private javax.swing.JCheckBox _mSWDL_Enabled;
    private javax.swing.JMenuItem _mSave;
    private javax.swing.JCheckBox _mTRL_Enabled;
    private javax.swing.JCheckBox _mTUL_Enabled;
    private javax.swing.JButton addButton;
    private javax.swing.JButton changeNumbersButton;
    private javax.swing.JButton deleteButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton reapplyPatternsButton;
    // End of variables declaration//GEN-END:variables

}
