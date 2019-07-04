package jmri.jmrit.beantable.beanedit;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.Turnout;
import jmri.TurnoutOperation;
import jmri.TurnoutOperationManager;
import jmri.NamedBean.DisplayOptions;
import jmri.implementation.SignalSpeedMap;
import jmri.jmrit.turnoutoperations.TurnoutOperationConfig;
import jmri.swing.NamedBeanComboBox;

/**
 * Provides an edit panel for a turnout object.
 *
 * @author Kevin Dickerson Copyright (C) 2011
 */
public class TurnoutEditAction extends BeanEditAction<Turnout> {
    @Override
    public String helpTarget() {
        return "package.jmri.jmrit.beantable.TurnoutAddEdit";
    } //NOI18N

    @Override
    public void actionPerformed(ActionEvent e) {
        oldAutomationSelection = bean.getTurnoutOperation();
        oldModeSelection = bean.getFeedbackModeName();
        super.actionPerformed(e);
    }

    @Override
    protected void initPanels() {
        super.initPanels();
        feedback();
        lock();
        speed();
    }

    @Override
    public String getBeanType() {
        return Bundle.getMessage("BeanNameTurnout");
    }

    @Override
    public Turnout getByUserName(String name) {
        return InstanceManager.turnoutManagerInstance().getByUserName(name);
    }

    private JCheckBox inverted = new JCheckBox();

    @Override
    protected BeanItemPanel basicDetails() {
        BeanItemPanel basic = super.basicDetails();
        basic.addItem(new BeanEditItem(inverted, Bundle.getMessage("Inverted"),
                Bundle.getMessage("InvertedToolTip")));
        return basic;
    }

    @Override
    protected void saveBasicItems(ActionEvent e) {
        super.saveBasicItems(e);
        Turnout turn = bean;
        turn.setInverted(inverted.isSelected());
    }

    @Override
    protected void resetBasicItems(ActionEvent e) {
        super.resetBasicItems(e);
        Turnout turn = bean;
        if (turn.canInvert()) {
            inverted.setSelected(turn.getInverted());
        }
        inverted.setEnabled(turn.canInvert());
    }

    private NamedBeanComboBox<Sensor> sensorFeedBack1ComboBox;
    private NamedBeanComboBox<Sensor> sensorFeedBack2ComboBox;
    private JComboBox<String> modeBox;
    private JComboBox<String> automationBox;
    private String useBlockSpeed = Bundle.getMessage("UseGlobal", "Block Speed");
    private TurnoutOperationConfig config;
    private BeanItemPanel feedback;
    private JPanel turnoutOperation = new JPanel();

    private BeanItemPanel feedback() {
        feedback = new BeanItemPanel();
        feedback.setName(Bundle.getMessage("Feedback"));

        modeBox = new JComboBox<String>(bean.getValidFeedbackNames());
        modeBox.setMaximumRowCount(modeBox.getItemCount());
        oldModeSelection = bean.getFeedbackModeName();
        modeBox.setSelectedItem(oldModeSelection);
        modeBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateFeedbackOptions();
            }
        });

        feedback.addItem(new BeanEditItem(null, null, Bundle.getMessage("FeedbackToolTip")));
        feedback.addItem(new BeanEditItem(modeBox, Bundle.getMessage("FeedbackMode"),
                Bundle.getMessage("FeedbackModeToolTip")));

        sensorFeedBack1ComboBox = new NamedBeanComboBox<>(
                InstanceManager.sensorManagerInstance(),
                bean.getFirstSensor(),
                DisplayOptions.DISPLAYNAME);
        sensorFeedBack1ComboBox.setAllowNull(true);
        feedback.addItem(new BeanEditItem(sensorFeedBack1ComboBox,
                Bundle.getMessage("FeedbackSensor1"),
                Bundle.getMessage("FeedbackSensorToolTip")));

        sensorFeedBack2ComboBox = new NamedBeanComboBox<>(
                InstanceManager.sensorManagerInstance(),
                bean.getSecondSensor(),
                DisplayOptions.DISPLAYNAME);
        sensorFeedBack2ComboBox.setAllowNull(true);
        feedback.addItem(new BeanEditItem(sensorFeedBack2ComboBox,
                Bundle.getMessage("FeedbackSensor2"),
                Bundle.getMessage("FeedbackSensorToolTip")));

        String[] str = new String[]{"empty"};
        automationBox = new JComboBox<String>(str);
        feedback.addItem(new BeanEditItem(automationBox,
                Bundle.getMessage("TurnoutAutomation"),
                Bundle.getMessage("TurnoutAutomationToolTip")));

        oldAutomationSelection = bean.getTurnoutOperation();
        if (oldAutomationSelection != null) {
            config = TurnoutOperationConfig.getConfigPanel(oldAutomationSelection);
        } else {
            config = TurnoutOperationConfig.getConfigPanel(new jmri.RawTurnoutOperation());
            config.setEnabled(false);
            for (Component j : config.getComponents()) {
                j.setEnabled(false);
            }
        }

        turnoutOperation.add(config);
        feedback.addItem(new BeanEditItem(turnoutOperation,
                Bundle.getMessage("FeedbackOperation"),
                Bundle.getMessage("FeedbackOperationToolTip")));
        feedback.addItem(new BeanEditItem(operationsName,
                Bundle.getMessage("FeedbackNameSet"),
                Bundle.getMessage("FeedbackNameSetToolTip")));

        feedback.setSaveItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String modeName = (String) modeBox.getSelectedItem();
                if (modeName != null) {
                    bean.setFeedbackMode(modeName);
                }
                String newName = operationsName.getText();
                if ((currentOperation != null) && (newName != null) && !newName.isEmpty()) {
                    if (!currentOperation.rename(newName)) {
                        JOptionPane.showMessageDialog(null,
                                Bundle.getMessage("ErrorDuplicateUserName", newName),
                                Bundle.getMessage("ErrorTitle"),
                                JOptionPane.ERROR_MESSAGE);
                    } else {
                        automationBox.addItem(newName);
                        automationBox.setSelectedItem(newName);
                    }
                    bean.setTurnoutOperation(null);
                    bean.setTurnoutOperation(currentOperation);
                }
                config.endConfigure();
                switch (automationBox.getSelectedIndex()) {
                    case 0:   // Off
                        bean.setInhibitOperation(true);
                        bean.setTurnoutOperation(null);
                        break;
                    case 1:   // Default
                        bean.setInhibitOperation(false);
                        bean.setTurnoutOperation(null);
                        break;
                    default:  // named operation
                        bean.setInhibitOperation(false);
                        String autoMode = (String) automationBox.getSelectedItem();
                        if (autoMode != null) {
                            bean.setTurnoutOperation(InstanceManager.getDefault(TurnoutOperationManager.class).
                                    getOperation((autoMode)));
                        }
                        break;
                }
                oldAutomationSelection = bean.getTurnoutOperation();
                oldModeSelection = bean.getFeedbackModeName();
                try {
                    bean.provideFirstFeedbackSensor(sensorFeedBack1ComboBox.getSelectedItemDisplayName());
                } catch (jmri.JmriException ex) {
                    JOptionPane.showMessageDialog(null, ex.toString());
                }
                try {
                    bean.provideSecondFeedbackSensor(sensorFeedBack2ComboBox.getSelectedItemDisplayName());
                } catch (jmri.JmriException ex) {
                    JOptionPane.showMessageDialog(null, ex.toString());
                }
            }
        });

        feedback.setResetItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sensorFeedBack1ComboBox.setSelectedItem(bean.getFirstSensor());
                sensorFeedBack2ComboBox.setSelectedItem(bean.getSecondSensor());

                automationBox.removeActionListener(automationSelectionListener);
                jmri.jmrit.beantable.TurnoutTableAction.updateAutomationBox(bean, automationBox);
                automationBox.addActionListener(automationSelectionListener);

                bean.setFeedbackMode(oldModeSelection);
                updateFeedbackOptions();
            }
        });
        bei.add(feedback);
        return feedback;
    }   // feedback()

    private String oldModeSelection;
    private TurnoutOperation oldAutomationSelection;
    private TurnoutOperation currentOperation;
    private JTextField operationsName = new JTextField(10);

    transient ActionListener automationSelectionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            updateAutomationOptions();
        }
    };

    private void updateFeedbackOptions() {
        sensorFeedBack1ComboBox.setEnabled(false);
        sensorFeedBack2ComboBox.setEnabled(false);

        String mode = (String) modeBox.getSelectedItem();
        if (mode != null) {
            if (mode.equals("ONESENSOR")) {
                sensorFeedBack1ComboBox.setEnabled(true);
            } else if (mode.equals("TWOSENSOR")) {
                sensorFeedBack1ComboBox.setEnabled(true);
                sensorFeedBack2ComboBox.setEnabled(true);
            }
            bean.setFeedbackMode(mode);
        }

        bean.setFeedbackMode((String) modeBox.getSelectedItem());
        jmri.jmrit.beantable.TurnoutTableAction.updateAutomationBox(bean, automationBox);
    }

    private void updateAutomationOptions() {

        currentOperation = null;
        automationBox.removeActionListener(automationSelectionListener);
        if (automationBox.getSelectedIndex() > 1) {
            String autoMode = (String) automationBox.getSelectedItem();
            if (autoMode != null) {
                currentOperation = InstanceManager.getDefault(TurnoutOperationManager.class).
                        getOperation((autoMode));
            }
        }

        if (currentOperation != null) {
            turnoutOperation.remove(config);
            if (!currentOperation.isNonce()) {
                currentOperation = currentOperation.makeNonce(bean);
            }
            config = TurnoutOperationConfig.getConfigPanel(currentOperation);
            operationsName.setEnabled(true);
            config.setEnabled(true);
            turnoutOperation.add(config);
            feedback.revalidate();
            feedback.repaint();
        } else {
            operationsName.setEnabled(false);
            config.setEnabled(false);
            for (Component j : config.getComponents()) {
                j.setEnabled(false);
            }
        }
        automationBox.addActionListener(automationSelectionListener);
    }

    @Override
    protected void cancelButtonAction(ActionEvent e) {
        bean.setFeedbackMode(oldModeSelection);
        bean.setTurnoutOperation(oldAutomationSelection);
        super.cancelButtonAction(e);
    }

    private final static String bothText = "Both"; // TODO I18N using bundle. Note: check how this property is stored/loaded
    private final static String cabOnlyText = "Cab only";
    private final static String pushbutText = "Pushbutton only";
    private final static String noneText = "None";

    private JComboBox<String> lockBox;
    protected BeanItemPanel lock() {
        BeanItemPanel lock = new BeanItemPanel();
        lock.setName(Bundle.getMessage("Lock"));

        if (bean.getPossibleLockModes() != 0) {
            // lock operations are available, configure pane for them
            lock.addItem(new BeanEditItem(null, null, Bundle.getMessage("LockToolTip")));

            // Vector is a JComboBox ctor; List is not
            java.util.Vector<String> lockOperations = new java.util.Vector<>();
            int modes = bean.getPossibleLockModes();
            if (((modes & Turnout.CABLOCKOUT) != 0)
                    && ((modes & Turnout.PUSHBUTTONLOCKOUT) != 0)) {
                lockOperations.add(bothText);
            }
            if ((modes & Turnout.CABLOCKOUT) != 0) {
                lockOperations.add(cabOnlyText);
            }
            if ((modes & Turnout.PUSHBUTTONLOCKOUT) != 0) {
                lockOperations.add(pushbutText);
            }
            lockOperations.add(noneText);
            JComboBox<String> lockOperationBox = new JComboBox<String>(lockOperations);
            lockOperationBox.setMaximumRowCount(lockOperationBox.getItemCount());

            lock.addItem(new BeanEditItem(lockOperationBox,
                    Bundle.getMessage("LockMode"),
                    Bundle.getMessage("LockModeToolTip")));
            lockOperationBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String lockOp = (String) lockOperationBox.getSelectedItem();
                    if (lockOp != null) {
                        if (lockOp.equals(noneText)) {
                            lockBox.setEnabled(false);
                        } else {
                            lockBox.setEnabled(true);
                        }
                    }
                }
            });

            if ((bean.getPossibleLockModes() & Turnout.PUSHBUTTONLOCKOUT) != 0) {
                lockBox = new JComboBox<String>(bean.getValidDecoderNames());
            } else {
                lockBox = new JComboBox<String>(new String[]{bean.getDecoderName()});
            }
            lock.addItem(new BeanEditItem(lockBox,
                    Bundle.getMessage("LockModeDecoder"),
                    Bundle.getMessage("LockModeDecoderToolTip")));

            lock.setSaveItem(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String lockOpName = (String) lockOperationBox.getSelectedItem();
                    if (lockOpName != null) {
                        if (lockOpName.equals(bothText)) {
                            bean.enableLockOperation(Turnout.CABLOCKOUT
                                    + Turnout.PUSHBUTTONLOCKOUT, true);
                        }
                        if (lockOpName.equals(cabOnlyText)) {
                            bean.enableLockOperation(Turnout.CABLOCKOUT, true);
                            bean.enableLockOperation(Turnout.PUSHBUTTONLOCKOUT, false);
                        }
                        if (lockOpName.equals(pushbutText)) {
                            bean.enableLockOperation(Turnout.CABLOCKOUT, false);
                            bean.enableLockOperation(Turnout.PUSHBUTTONLOCKOUT, true);
                        }
                    }
                    String decoderName = (String) lockBox.getSelectedItem();
                    if (decoderName != null) {
                        bean.setDecoderName(decoderName);
                    }
                }
            });

            lock.setResetItem(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    lockBox.setSelectedItem(bean.getDecoderName());
                    lockBox.setEnabled(true);
                    if (bean.canLock(Turnout.CABLOCKOUT)
                            && bean.canLock(Turnout.PUSHBUTTONLOCKOUT)) {
                        lockOperationBox.setSelectedItem(bothText);
                    } else if (bean.canLock(Turnout.PUSHBUTTONLOCKOUT)) {
                        lockOperationBox.setSelectedItem(pushbutText);
                    } else if (bean.canLock(Turnout.CABLOCKOUT)) {
                        lockOperationBox.setSelectedItem(cabOnlyText);
                    } else {
                        lockOperationBox.setSelectedItem(noneText);
                        lockBox.setEnabled(false);
                    }
                }
            });
        } else {
            // lock operations are not available for this kind of Turnout
            lock.addItem(new BeanEditItem(null, null, Bundle.getMessage("LockModeUnavailable")));
        }
        bei.add(lock);
        return lock;
    }   // lock() 

    private java.util.Vector<String> speedListClosed = new java.util.Vector<String>();
    private java.util.Vector<String> speedListThrown = new java.util.Vector<String>();

    private JComboBox<String> closedSpeedBox;
    private JComboBox<String> thrownSpeedBox;
    private String defaultThrownSpeedText;
    private String defaultClosedSpeedText;

    protected BeanItemPanel speed() {
        BeanItemPanel speed = new BeanItemPanel();
        speed.setName(Bundle.getMessage("Speed"));

        speed.addItem(new BeanEditItem(null, null, Bundle.getMessage("SpeedTabToolTip")));

        defaultThrownSpeedText = (Bundle.getMessage("UseGlobal", "Global")
                + " " + InstanceManager.turnoutManagerInstance().getDefaultThrownSpeed());
        defaultClosedSpeedText = (Bundle.getMessage("UseGlobal", "Global")
                + " " + InstanceManager.turnoutManagerInstance().getDefaultClosedSpeed());

      useBlockSpeed = Bundle.getMessage("UseGlobal", "Block Speed");

        speedListClosed.add(defaultClosedSpeedText);
        speedListThrown.add(defaultThrownSpeedText);

        speedListClosed.add(useBlockSpeed);
        speedListThrown.add(useBlockSpeed);

        java.util.Vector<String> _speedMap = jmri.InstanceManager.
                getDefault(SignalSpeedMap.class).getValidSpeedNames();
        for (String speedMap : _speedMap) {
            if (!speedListClosed.contains(speedMap)) {
                speedListClosed.add(speedMap);
            }
            if (!speedListThrown.contains(speedMap)) {
                speedListThrown.add(speedMap);
            }
        }

        closedSpeedBox = new JComboBox<String>(speedListClosed);
        closedSpeedBox.setMaximumRowCount(closedSpeedBox.getItemCount());
        closedSpeedBox.setEditable(true);

        speed.addItem(new BeanEditItem(closedSpeedBox,
                Bundle.getMessage("ClosedSpeed"),
                Bundle.getMessage("ClosedSpeedToolTip")));
        thrownSpeedBox = new JComboBox<String>(speedListThrown);
        thrownSpeedBox.setMaximumRowCount(thrownSpeedBox.getItemCount());
        thrownSpeedBox.setEditable(true);
        speed.addItem(new BeanEditItem(thrownSpeedBox,
                Bundle.getMessage("ThrownSpeed"),
                Bundle.getMessage("ThrownSpeedToolTip")));

      speed.setSaveItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String speed = (String) closedSpeedBox.getSelectedItem();
                if (speed != null) {
                    try {
                        bean.setStraightSpeed(speed);
                        if ((!speedListClosed.contains(speed)) && !speed.contains("Global")) {
                            speedListClosed.add(speed);
                        }
                    } catch (jmri.JmriException ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage() + "\n" + speed);
                    }
                }
                speed = (String) thrownSpeedBox.getSelectedItem();
                if (speed != null) {
                    try {
                        bean.setDivergingSpeed(speed);
                        if ((!speedListThrown.contains(speed)) && !speed.contains("Global")) {
                            speedListThrown.add(speed);
                        }
                    } catch (jmri.JmriException ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage() + "\n" + speed);
                    }
                }
            }
        });

        speed.setResetItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String speed = bean.getDivergingSpeed();
                speedListThrown.remove(defaultThrownSpeedText);
                defaultThrownSpeedText = (Bundle.getMessage("UseGlobal", "Global")
                        + " " + InstanceManager.turnoutManagerInstance().getDefaultThrownSpeed());
                speedListThrown.add(0, defaultThrownSpeedText);
                if (!speedListThrown.contains(speed)) {
                    speedListThrown.add(speed);
                    thrownSpeedBox.addItem(speed);
                }
                thrownSpeedBox.setSelectedItem(speed);
                speed = bean.getStraightSpeed();
                speedListClosed.remove(defaultClosedSpeedText);
                defaultClosedSpeedText = (Bundle.getMessage("UseGlobal", "Global")
                        + " " + InstanceManager.turnoutManagerInstance().getDefaultClosedSpeed());
                speedListClosed.add(0, defaultClosedSpeedText);
                if (!speedListClosed.contains(speed)) {
                    speedListClosed.add(speed);
                    closedSpeedBox.addItem(speed);
                }
                closedSpeedBox.setSelectedItem(speed);
            }
        });

        bei.add(speed);
        return speed;
    }

} 
