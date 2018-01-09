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
import jmri.NamedBean;
import jmri.Turnout;
import jmri.TurnoutOperation;
import jmri.TurnoutOperationManager;
import jmri.implementation.SignalSpeedMap;
import jmri.jmrit.turnoutoperations.TurnoutOperationConfig;
import jmri.util.swing.JmriBeanComboBox;

/**
 * Provides an edit panel for a turnout object.
 *
 * @author Kevin Dickerson Copyright (C) 2011
 */
public class TurnoutEditAction extends BeanEditAction {
    @Override
    public String helpTarget() {
        return "package.jmri.jmrit.beantable.TurnoutTable";
    } //NOI18N

    @Override
    public void actionPerformed(ActionEvent e) {
        oldAutomationSelection = ((Turnout) bean).getTurnoutOperation();
        oldModeSelection = ((Turnout) bean).getFeedbackModeName();
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
    public NamedBean getByUserName(String name) {
        return InstanceManager.turnoutManagerInstance().getByUserName(name);
    }

    private JCheckBox useCurrent = new JCheckBox();
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
        Turnout turn = (Turnout) bean;
        turn.setInverted(inverted.isSelected());
    }

    @Override
    protected void resetBasicItems(ActionEvent e) {
        super.resetBasicItems(e);
        Turnout turn = (Turnout) bean;
        if (turn.canInvert()) {
            inverted.setSelected(turn.getInverted());
        }
        inverted.setEnabled(turn.canInvert());
    }

    private JmriBeanComboBox sensorFeedBack1ComboBox;
    private JmriBeanComboBox sensorFeedBack2ComboBox;
    private JComboBox<String> modeBox;
    private JComboBox<String> automationBox;
    private String useBlockSpeed = Bundle.getMessage("UseGlobal", "Block Speed");
    private TurnoutOperationConfig config;
    private BeanItemPanel feedback;
    private JPanel turnoutOperation = new JPanel();

    private BeanItemPanel feedback() {
        feedback = new BeanItemPanel();
        feedback.setName(Bundle.getMessage("Feedback"));

        modeBox = new JComboBox<String>(((Turnout) bean).getValidFeedbackNames());
        modeBox.setMaximumRowCount(modeBox.getItemCount());
        oldModeSelection = ((Turnout) bean).getFeedbackModeName();
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

        sensorFeedBack1ComboBox = new JmriBeanComboBox(
                InstanceManager.sensorManagerInstance(),
                ((Turnout) bean).getFirstSensor(),
                JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
        sensorFeedBack1ComboBox.setFirstItemBlank(true);
        feedback.addItem(new BeanEditItem(sensorFeedBack1ComboBox,
                Bundle.getMessage("FeedbackSensor1"),
                Bundle.getMessage("FeedbackSensorToolTip")));

        sensorFeedBack2ComboBox = new JmriBeanComboBox(
                InstanceManager.sensorManagerInstance(),
                ((Turnout) bean).getSecondSensor(),
                JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
        sensorFeedBack2ComboBox.setFirstItemBlank(true);
        feedback.addItem(new BeanEditItem(sensorFeedBack2ComboBox,
                Bundle.getMessage("FeedbackSensor2"),
                Bundle.getMessage("FeedbackSensorToolTip")));

        String[] str = new String[]{"empty"};
        automationBox = new JComboBox<String>(str);
        feedback.addItem(new BeanEditItem(automationBox,
                Bundle.getMessage("TurnoutAutomation"),
                Bundle.getMessage("TurnoutAutomationToolTip")));

        oldAutomationSelection = ((Turnout) bean).getTurnoutOperation();
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
                Turnout t = (Turnout) bean;
                String modeName = (String) modeBox.getSelectedItem();
                t.setFeedbackMode(modeName);
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
                    t.setTurnoutOperation(null);
                    t.setTurnoutOperation(currentOperation);
                }
                config.endConfigure();
                switch (automationBox.getSelectedIndex()) {
                    case 0:   // Off
                        t.setInhibitOperation(true);
                        t.setTurnoutOperation(null);
                        break;
                    case 1:   // Default
                        t.setInhibitOperation(false);
                        t.setTurnoutOperation(null);
                        break;
                    default:  // named operation
                        t.setInhibitOperation(false);
                        t.setTurnoutOperation(TurnoutOperationManager.getInstance().
                                getOperation(((String) automationBox.getSelectedItem())));
                        break;
                }
                oldAutomationSelection = ((Turnout) bean).getTurnoutOperation();
                oldModeSelection = ((Turnout) bean).getFeedbackModeName();
                try {
                    t.provideFirstFeedbackSensor(sensorFeedBack1ComboBox.getSelectedDisplayName());
                } catch (jmri.JmriException ex) {
                    JOptionPane.showMessageDialog(null, ex.toString());
                }
                try {
                    t.provideSecondFeedbackSensor(sensorFeedBack2ComboBox.getSelectedDisplayName());
                } catch (jmri.JmriException ex) {
                    JOptionPane.showMessageDialog(null, ex.toString());
                }
                if (config.isEnabled()) {
                    // shouldn't there be some code here?
                }
            }
        });

        feedback.setResetItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Turnout t = (Turnout) bean;
                sensorFeedBack1ComboBox.setSelectedBean(t.getFirstSensor());
                sensorFeedBack2ComboBox.setSelectedBean(t.getSecondSensor());

                automationBox.removeActionListener(automationSelectionListener);
                jmri.jmrit.beantable.TurnoutTableAction.updateAutomationBox(t, automationBox);
                automationBox.addActionListener(automationSelectionListener);

                t.setFeedbackMode(oldModeSelection);
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
        Turnout t = (Turnout) bean;
        sensorFeedBack1ComboBox.setEnabled(false);
        sensorFeedBack2ComboBox.setEnabled(false);

        if (modeBox.getSelectedItem().equals("ONESENSOR")) {
            sensorFeedBack1ComboBox.setEnabled(true);
        } else if (modeBox.getSelectedItem().equals("TWOSENSOR")) {
            sensorFeedBack1ComboBox.setEnabled(true);
            sensorFeedBack2ComboBox.setEnabled(true);
        }

        t.setFeedbackMode((String) modeBox.getSelectedItem());
        jmri.jmrit.beantable.TurnoutTableAction.updateAutomationBox(t, automationBox);
    }

    private void updateAutomationOptions() {

        currentOperation = null;
        automationBox.removeActionListener(automationSelectionListener);
        if (automationBox.getSelectedIndex() > 1) {
            currentOperation = TurnoutOperationManager.getInstance().
                    getOperation(((String) automationBox.getSelectedItem()));
        }

        if (currentOperation != null) {
            turnoutOperation.remove(config);
            if (!currentOperation.isNonce()) {
                currentOperation = currentOperation.makeNonce((Turnout) bean);
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
        Turnout t = (Turnout) bean;
        t.setFeedbackMode(oldModeSelection);
        t.setTurnoutOperation(oldAutomationSelection);
        super.cancelButtonAction(e);
    }

    private final static String bothText = "Both";
    private final static String cabOnlyText = "Cab only";
    private final static String pushbutText = "Pushbutton only";
    private final static String noneText = "None";

    private JComboBox<String> lockBox;
    protected BeanItemPanel lock() {
        Turnout t = (Turnout) bean;
        BeanItemPanel lock = new BeanItemPanel();
        lock.setName(Bundle.getMessage("Lock"));

        if (t.getPossibleLockModes() != 0) {
            // lock operations are available, configure pane for them
            lock.addItem(new BeanEditItem(null, null, Bundle.getMessage("LockToolTip")));

            // Vector is a JComboBox ctor; List is not
            java.util.Vector<String> lockOperations = new java.util.Vector<>();
            int modes = t.getPossibleLockModes();
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
                    if (lockOperationBox.getSelectedItem().equals(noneText)) {
                        lockBox.setEnabled(false);
                    } else {
                        lockBox.setEnabled(true);
                    }
                }
            });

            if ((t.getPossibleLockModes() & Turnout.PUSHBUTTONLOCKOUT) != 0) {
                lockBox = new JComboBox<String>(t.getValidDecoderNames());
            } else {
                lockBox = new JComboBox<String>(new String[]{t.getDecoderName()});
            }
            lock.addItem(new BeanEditItem(lockBox,
                    Bundle.getMessage("LockModeDecoder"),
                    Bundle.getMessage("LockModeDecoderToolTip")));

          lock.setSaveItem(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Turnout t = (Turnout) bean;
                    String lockOpName = (String) lockOperationBox.getSelectedItem();
                    if (lockOpName.equals(bothText)) {
                        t.enableLockOperation(Turnout.CABLOCKOUT
                                + Turnout.PUSHBUTTONLOCKOUT, true);
                    }
                    if (lockOpName.equals(cabOnlyText)) {
                        t.enableLockOperation(Turnout.CABLOCKOUT, true);
                        t.enableLockOperation(Turnout.PUSHBUTTONLOCKOUT, false);
                    }
                    if (lockOpName.equals(pushbutText)) {
                        t.enableLockOperation(Turnout.CABLOCKOUT, false);
                        t.enableLockOperation(Turnout.PUSHBUTTONLOCKOUT, true);
                    }
                    String decoderName = (String) lockBox.getSelectedItem();
                    t.setDecoderName(decoderName);
                }
            });

            lock.setResetItem(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Turnout t = (Turnout) bean;
                    lockBox.setSelectedItem(t.getDecoderName());
                    lockBox.setEnabled(true);
                    if (t.canLock(Turnout.CABLOCKOUT)
                            && t.canLock(Turnout.PUSHBUTTONLOCKOUT)) {
                        lockOperationBox.setSelectedItem(bothText);
                    } else if (t.canLock(Turnout.PUSHBUTTONLOCKOUT)) {
                        lockOperationBox.setSelectedItem(pushbutText);
                    } else if (t.canLock(Turnout.CABLOCKOUT)) {
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
                Turnout t = (Turnout) bean;
                String speed = (String) closedSpeedBox.getSelectedItem();
                try {
                    t.setStraightSpeed(speed);
                    if ((!speedListClosed.contains(speed))
                            && !speed.contains("Global")) {
                        speedListClosed.add(speed);
                    }
                } catch (jmri.JmriException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage()
                            + "\n" + speed);
                }
                speed = (String) thrownSpeedBox.getSelectedItem();
                try {
                    t.setDivergingSpeed(speed);
                    if ((!speedListThrown.contains(speed))
                            && !speed.contains("Global")) {
                        speedListThrown.add(speed);
                    }
                } catch (jmri.JmriException ex) {
                    JOptionPane.showMessageDialog(null,
                            ex.getMessage() + "\n" + speed);
                }
            }
        });

        speed.setResetItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Turnout t = (Turnout) bean;
                String speed = t.getDivergingSpeed();
                speedListThrown.remove(defaultThrownSpeedText);
                defaultThrownSpeedText = (Bundle.getMessage("UseGlobal", "Global")
                        + " " + InstanceManager.turnoutManagerInstance().getDefaultThrownSpeed());
                speedListThrown.add(0, defaultThrownSpeedText);
                if (!speedListThrown.contains(speed)) {
                    speedListThrown.add(speed);
                    thrownSpeedBox.addItem(speed);
                }
                thrownSpeedBox.setSelectedItem(speed);
                speed = t.getStraightSpeed();
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
