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
 * Provides an edit panel for a block object
 *
 * @author	Kevin Dickerson Copyright (C) 2011
 */
public class TurnoutEditAction extends BeanEditAction {

    /**
     *
     */
    private static final long serialVersionUID = 3432794348005461234L;

    public String helpTarget() {
        return "package.jmri.jmrit.beantable.TurnoutTable";
    } //IN18N

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

    public String getBeanType() {
        return Bundle.getMessage("BeanNameTurnout");
    }

    public NamedBean getByUserName(String name) {
        return InstanceManager.turnoutManagerInstance().getByUserName(name);
    }

    JmriBeanComboBox reporterField;
    JCheckBox useCurrent = new JCheckBox();

    JCheckBox inverted = new JCheckBox();

    @Override
    BeanItemPanel basicDetails() {
        BeanItemPanel basic = super.basicDetails();

        basic.addItem(new BeanEditItem(inverted, Bundle.getMessage("Inverted"), Bundle.getMessage("InvertedToolTip")));

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

    JmriBeanComboBox sensorFeedBack1Field;
    JmriBeanComboBox sensorFeedBack2Field;
    JComboBox<String> modeBox;
    JComboBox<String> automationBox;
    String useBlockSpeed = "Use Block Speed";//IN18N
    TurnoutOperationConfig config;
    BeanItemPanel feedback;
    JPanel turnoutOperation = new JPanel();
    String userDefinedOperation = null;

    BeanItemPanel feedback() {
        feedback = new BeanItemPanel();
        feedback.setName(Bundle.getMessage("Feedback"));

        modeBox = new JComboBox<String>(((Turnout) bean).getValidFeedbackNames());
        oldModeSelection = ((Turnout) bean).getFeedbackModeName();
        modeBox.setSelectedItem(oldModeSelection);

        modeBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateFeedbackOptions();
            }
        });
        feedback.addItem(new BeanEditItem(null, null, Bundle.getMessage("FeedbackToolTip")));
        feedback.addItem(new BeanEditItem(modeBox, Bundle.getMessage("FeedbackMode"), Bundle.getMessage("FeedbackModeToolTip")));

        sensorFeedBack1Field = new JmriBeanComboBox(InstanceManager.sensorManagerInstance(), ((Turnout) bean).getFirstSensor(), JmriBeanComboBox.DISPLAYNAME);
        sensorFeedBack1Field.setFirstItemBlank(true);
        feedback.addItem(new BeanEditItem(sensorFeedBack1Field, Bundle.getMessage("FeedbackSensor1"), Bundle.getMessage("FeedbackSensorToolTip1")));

        sensorFeedBack2Field = new JmriBeanComboBox(InstanceManager.sensorManagerInstance(), ((Turnout) bean).getSecondSensor(), JmriBeanComboBox.DISPLAYNAME);
        sensorFeedBack2Field.setFirstItemBlank(true);
        feedback.addItem(new BeanEditItem(sensorFeedBack2Field, Bundle.getMessage("FeedbackSensor2"), Bundle.getMessage("FeedbackSensorToolTip2")));

        String[] str = new String[]{"empty"};
        automationBox = new JComboBox<String>(str);

        feedback.addItem(new BeanEditItem(automationBox, Bundle.getMessage("TurnoutAutomation"), Bundle.getMessage("TurnoutAutomationToolTip")));

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
        feedback.addItem(new BeanEditItem(turnoutOperation, Bundle.getMessage("FeedbackOperation"), Bundle.getMessage("FeedbackOperationToolTip")));
        feedback.addItem(new BeanEditItem(operationsName, Bundle.getMessage("FeedbackNameSet"), Bundle.getMessage("FeedbackNameSetToolTip")));

        feedback.setSaveItem(new AbstractAction() {
            /**
             *
             */
            private static final long serialVersionUID = 2969190372668700931L;

            public void actionPerformed(ActionEvent e) {
                Turnout t = (Turnout) bean;
                String modeName = (String) modeBox.getSelectedItem();
                t.setFeedbackMode(modeName);
                String newName = operationsName.getText();
                if (currentOperation != null && newName != null && !newName.equals("")) {
                    if (!currentOperation.rename(newName)) {
                        JOptionPane.showMessageDialog(null, "This name '" + newName + "' is already in use",
                                "Name already in use", JOptionPane.ERROR_MESSAGE);
                    } else {
                        automationBox.addItem(newName);
                        automationBox.setSelectedItem(newName);
                    }
                    t.setTurnoutOperation(null);
                    t.setTurnoutOperation(currentOperation);

                }
                config.endConfigure();
                switch (automationBox.getSelectedIndex()) {
                    case 0:			// Off
                        t.setInhibitOperation(true);
                        t.setTurnoutOperation(null);
                        break;
                    case 1:			// Default
                        t.setInhibitOperation(false);
                        t.setTurnoutOperation(null);
                        break;
                    default:		// named operation
                        t.setInhibitOperation(false);
                        t.setTurnoutOperation(TurnoutOperationManager.getInstance().
                                getOperation(((String) automationBox.getSelectedItem())));
                        break;
                }
                oldAutomationSelection = ((Turnout) bean).getTurnoutOperation();
                oldModeSelection = ((Turnout) bean).getFeedbackModeName();
                try {
                    t.provideFirstFeedbackSensor(sensorFeedBack1Field.getSelectedDisplayName());
                } catch (jmri.JmriException ex) {
                    JOptionPane.showMessageDialog(null, ex.toString());
                }
                try {
                    t.provideSecondFeedbackSensor(sensorFeedBack2Field.getSelectedDisplayName());
                } catch (jmri.JmriException ex) {
                    JOptionPane.showMessageDialog(null, ex.toString());
                }
                if (config.isEnabled()) {

                }
            }
        });

        feedback.setResetItem(new AbstractAction() {
            /**
             *
             */
            private static final long serialVersionUID = -6958613309056965212L;

            public void actionPerformed(ActionEvent e) {
                Turnout t = (Turnout) bean;

                sensorFeedBack1Field.setSelectedBean(t.getFirstSensor());
                sensorFeedBack2Field.setSelectedBean(t.getSecondSensor());
                automationBox.removeActionListener(automationSelectionListener);
                jmri.jmrit.beantable.TurnoutTableAction.updateAutomationBox(t, automationBox);
                automationBox.addActionListener(automationSelectionListener);

                t.setFeedbackMode(oldModeSelection);

                updateFeedbackOptions();
            }
        });

        bei.add(feedback);
        return feedback;
    }

    String oldModeSelection;
    TurnoutOperation oldAutomationSelection;
    TurnoutOperation currentOperation;
    JTextField operationsName = new JTextField(10);

    transient ActionListener automationSelectionListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            updateAutomationOptions();
        }
    };

    void updateFeedbackOptions() {
        Turnout t = (Turnout) bean;
        sensorFeedBack1Field.setEnabled(false);
        sensorFeedBack2Field.setEnabled(false);

        if (modeBox.getSelectedItem().equals("ONESENSOR")) {
            sensorFeedBack1Field.setEnabled(true);
        } else if (modeBox.getSelectedItem().equals("TWOSENSOR")) {
            sensorFeedBack1Field.setEnabled(true);
            sensorFeedBack2Field.setEnabled(true);
        }

        t.setFeedbackMode((String) modeBox.getSelectedItem());

        jmri.jmrit.beantable.TurnoutTableAction.updateAutomationBox(t, automationBox);
    }

    void updateAutomationOptions() {

        if (userDefinedOperation != null && userDefinedOperation.equals(automationBox.getSelectedItem())) {
            return;
        }
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

    String bothText = "Both";
    String cabOnlyText = "Cab only";
    String pushbutText = "Pushbutton only";
    String noneText = "None";

    JComboBox<String> lockBox;
    JComboBox<String> lockOperationBox;

    BeanItemPanel lock() {
        BeanItemPanel lock = new BeanItemPanel();
        lock.setName(Bundle.getMessage("Lock"));

        lock.addItem(new BeanEditItem(null, null, Bundle.getMessage("LockToolTip")));

        String[] lockOperations = {bothText, cabOnlyText, pushbutText, noneText};

        lockOperationBox = new JComboBox<String>(lockOperations);
        lock.addItem(new BeanEditItem(lockOperationBox, Bundle.getMessage("LockMode"), Bundle.getMessage("LockModeToolTip")));
        lockOperationBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (lockOperationBox.getSelectedItem().equals(noneText)) {
                    lockBox.setEnabled(false);
                } else {
                    lockBox.setEnabled(true);
                }
            }
        });

        lockBox = new JComboBox<String>(((Turnout) bean).getValidDecoderNames());
        lock.addItem(new BeanEditItem(lockBox, Bundle.getMessage("LockModeDecoder"), Bundle.getMessage("LockModeDecoderToolTip")));

        lock.setSaveItem(new AbstractAction() {
            /**
             *
             */
            private static final long serialVersionUID = -275341435715029798L;

            public void actionPerformed(ActionEvent e) {
                Turnout t = (Turnout) bean;
                String lockOpName = (String) lockOperationBox.getSelectedItem();
                if (lockOpName.equals(bothText)) {
                    t.enableLockOperation(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, true);
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
            /**
             *
             */
            private static final long serialVersionUID = 7063263885175963245L;

            public void actionPerformed(ActionEvent e) {
                Turnout t = (Turnout) bean;
                lockBox.setSelectedItem(t.getDecoderName());
                lockBox.setEnabled(true);
                if (t.canLock(Turnout.CABLOCKOUT) && t.canLock(Turnout.PUSHBUTTONLOCKOUT)) {
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

        bei.add(lock);
        return lock;
    }

    private java.util.Vector<String> speedListClosed = new java.util.Vector<String>();
    private java.util.Vector<String> speedListThrown = new java.util.Vector<String>();

    JComboBox<String> closedSpeedBox;
    JComboBox<String> thrownSpeedBox;
    String defaultThrownSpeedText;
    String defaultClosedSpeedText;

    BeanItemPanel speed() {
        BeanItemPanel speed = new BeanItemPanel();
        speed.setName(Bundle.getMessage("Speed"));

        speed.addItem(new BeanEditItem(null, null, Bundle.getMessage("SpeedTabToolTip")));

        defaultThrownSpeedText = ("Use Global " + InstanceManager.turnoutManagerInstance().getDefaultThrownSpeed());
        defaultClosedSpeedText = ("Use Global " + InstanceManager.turnoutManagerInstance().getDefaultClosedSpeed());

        useBlockSpeed = "Use Block Speed";

        speedListClosed.add(defaultClosedSpeedText);
        speedListThrown.add(defaultThrownSpeedText);

        speedListClosed.add(useBlockSpeed);
        speedListThrown.add(useBlockSpeed);

        java.util.Vector<String> _speedMap = jmri.InstanceManager.getDefault(SignalSpeedMap.class).getValidSpeedNames();
        for (int i = 0; i < _speedMap.size(); i++) {
            if (!speedListClosed.contains(_speedMap.get(i))) {
                speedListClosed.add(_speedMap.get(i));
            }
            if (!speedListThrown.contains(_speedMap.get(i))) {
                speedListThrown.add(_speedMap.get(i));
            }
        }

        closedSpeedBox = new JComboBox<String>(speedListClosed);
        closedSpeedBox.setEditable(true);

        speed.addItem(new BeanEditItem(closedSpeedBox, Bundle.getMessage("ClosedSpeed"), Bundle.getMessage("ClosedSpeedToolTip")));

        thrownSpeedBox = new JComboBox<String>(speedListThrown);
        thrownSpeedBox.setEditable(true);
        speed.addItem(new BeanEditItem(thrownSpeedBox, Bundle.getMessage("ThrownSpeed"), Bundle.getMessage("ThrownSpeedToolTip")));

        speed.setSaveItem(new AbstractAction() {
            /**
             *
             */
            private static final long serialVersionUID = 8189801856564109719L;

            public void actionPerformed(ActionEvent e) {
                Turnout t = (Turnout) bean;
                String speed = (String) closedSpeedBox.getSelectedItem();
                try {
                    t.setStraightSpeed(speed);
                    if ((!speedListClosed.contains(speed)) && !speed.contains("Global")) {
                        speedListClosed.add(speed);
                    }
                } catch (jmri.JmriException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage() + "\n" + speed);
                }
                speed = (String) thrownSpeedBox.getSelectedItem();
                try {
                    t.setDivergingSpeed(speed);
                    if ((!speedListThrown.contains(speed)) && !speed.contains("Global")) {
                        speedListThrown.add(speed);
                    }
                } catch (jmri.JmriException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage() + "\n" + speed);
                }
            }
        });

        speed.setResetItem(new AbstractAction() {
            /**
             *
             */
            private static final long serialVersionUID = 3766958497699526365L;

            public void actionPerformed(ActionEvent e) {
                Turnout t = (Turnout) bean;

                String speed = t.getDivergingSpeed();

                speedListThrown.remove(defaultThrownSpeedText);
                defaultThrownSpeedText = ("Use Global " + InstanceManager.turnoutManagerInstance().getDefaultThrownSpeed());
                speedListThrown.add(0, defaultThrownSpeedText);
                if (!speedListThrown.contains(speed)) {
                    speedListThrown.add(speed);
                    thrownSpeedBox.addItem(speed);
                }
                thrownSpeedBox.setSelectedItem(speed);

                speed = t.getStraightSpeed();

                speedListClosed.remove(defaultClosedSpeedText);
                defaultClosedSpeedText = ("Use Global " + InstanceManager.turnoutManagerInstance().getDefaultClosedSpeed());
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
