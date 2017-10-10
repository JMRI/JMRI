package jmri.jmrit.simpleturnoutctrl;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import jmri.InstanceManager;
import jmri.Turnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame to control a single turnout.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class SimpleTurnoutCtrlFrame extends jmri.util.JmriJFrame implements java.beans.PropertyChangeListener {

    private static final String LOCKED = Bundle.getMessage("Locked");
    private static final String UNLOCKED = Bundle.getMessage("Normal");

    // GUI member declarations
    javax.swing.JTextField adrTextField = new javax.swing.JTextField(8);

    javax.swing.JButton throwButton = new javax.swing.JButton();
    javax.swing.JButton closeButton = new javax.swing.JButton();

    javax.swing.JLabel nowStateLabel = new javax.swing.JLabel();

    javax.swing.JLabel nowFeedbackLabel = new javax.swing.JLabel();

    javax.swing.JLabel lockButtonLabel = new javax.swing.JLabel();
    javax.swing.JButton lockButton = new javax.swing.JButton();

    javax.swing.JLabel lockPushButtonLabel = new javax.swing.JLabel();
    javax.swing.JButton lockPushButton = new javax.swing.JButton();

    public SimpleTurnoutCtrlFrame() {
        super();

        // configure items for GUI        
        adrTextField.setText("");
        adrTextField.setVisible(true);
        adrTextField.setToolTipText(Bundle.getMessage("AddressToolTip"));

        throwButton.setText(InstanceManager.turnoutManagerInstance().getThrownText());
        throwButton.setVisible(true);
        throwButton.setToolTipText(Bundle.getMessage("ThrowButtonToolTip",
                InstanceManager.turnoutManagerInstance().getThrownText()));
        throwButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                throwButtonActionPerformed(e);
            }
        });

        closeButton.setText(InstanceManager.turnoutManagerInstance().getClosedText());
        closeButton.setVisible(true);
        closeButton.setToolTipText(Bundle.getMessage("ThrowButtonToolTip",
                InstanceManager.turnoutManagerInstance().getClosedText()));
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                closeButtonActionPerformed(e);
            }
        });

        nowStateLabel.setText(Bundle.getMessage("BeanStateUnknown"));
        nowStateLabel.setVisible(true);

        nowFeedbackLabel.setText(Bundle.getMessage("BeanStateUnknown"));
        nowFeedbackLabel.setVisible(true);

        lockButtonLabel.setText(Bundle.getMessage("LockButtonLabel") + " ");
        lockButtonLabel.setVisible(true);

        lockButton.setText(UNLOCKED);
        lockButton.setVisible(true);
        lockButton.setEnabled(false);
        lockButton.setToolTipText(Bundle.getMessage("LockButtonToolTip"));
        lockButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                lockButtonActionPerformed(e);
            }
        });

        lockPushButtonLabel.setText(Bundle.getMessage("PushButtonLabel"));
        lockPushButtonLabel.setVisible(true);

        lockPushButton.setText(UNLOCKED);
        lockPushButton.setVisible(true);
        lockPushButton.setEnabled(false);
        lockPushButton.setToolTipText(Bundle.getMessage("PushButtonToolTip"));
        lockPushButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                lockPushButtonActionPerformed(e);
            }
        });
        // general GUI config
        setTitle(Bundle.getMessage("FrameTitle"));
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1.0;

        // install items in GUI
        JPanel tPanel = new JPanel();
        tPanel.setLayout(new GridBagLayout());
        tPanel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BeanNameTurnout") + " "));

        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.EAST;
        tPanel.add(adrTextField, c);

        c.gridx = 0;
        c.gridy = 1;
        c.anchor = GridBagConstraints.EAST;
        tPanel.add(throwButton, c);
        c.gridx = 2;
        c.anchor = GridBagConstraints.WEST;
        tPanel.add(closeButton, c);

        JPanel sPanel = new JPanel();
        sPanel.setLayout(new GridBagLayout());
        sPanel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("CurrentStateTitle") + " "));

        sPanel.add(nowStateLabel);

        JPanel fPanel = new JPanel();
        fPanel.setLayout(new GridBagLayout());
        fPanel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("FeedbackModeTitle") + " "));

        fPanel.add(nowFeedbackLabel);

        JPanel avPanel = new JPanel();
        avPanel.setLayout(new GridBagLayout());
        avPanel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("AdvancedFeaturesTitle") + " "));

        c.gridx = 0;
        c.gridy = 5;
        c.anchor = GridBagConstraints.EAST;
        avPanel.add(lockButtonLabel, c);
        c.gridx = 1;
        c.anchor = GridBagConstraints.WEST;
        avPanel.add(lockButton, c);

        c.gridx = 0;
        c.gridy = 6;
        c.anchor = GridBagConstraints.EAST;
        avPanel.add(lockPushButtonLabel, c);
        c.gridx = 1;
        c.anchor = GridBagConstraints.WEST;
        avPanel.add(lockPushButton, c);

        getContentPane().add(tPanel);
        getContentPane().add(sPanel);
        getContentPane().add(fPanel);
        getContentPane().add(avPanel);

        // add help menu to window
        addHelpMenu("package.jmri.jmrit.simpleturnoutctrl.SimpleTurnoutCtrl", true);

        pack();
    }

    public void closeButtonActionPerformed(java.awt.event.ActionEvent e) {
        // load address from switchAddrTextField
        if (adrTextField.getText().length() < 1) {
            nowStateLabel.setText(Bundle.getMessage("NoAddressHint"));
            return;
        }
        try {
            if (turnout != null) {
                turnout.removePropertyChangeListener(this);
            }
            turnout = InstanceManager.turnoutManagerInstance().provideTurnout(
                    adrTextField.getText());

            turnout.addPropertyChangeListener(this);
            updateTurnoutStatusFields();
            if (turnout.getCommandedState() == Turnout.CLOSED) {
                nowStateLabel.setText(InstanceManager
                        .turnoutManagerInstance().getClosedText());
            }
            log.debug("about to command CLOSED");
            // and set commanded state to CLOSED
            turnout.setCommandedState(Turnout.CLOSED);

        } catch (IllegalArgumentException ex1) {
            invalidTurnout(adrTextField.getText(), ex1);
        } catch (Exception ex2) {
            log.error("exception during closeButtonActionPerformed", ex2); // NOI18N
            nowStateLabel.setText(Bundle.getMessage("ErrorTitle"));
            nowFeedbackLabel.setText(Bundle.getMessage("BeanStateUnknown"));
        }
    }

    public void throwButtonActionPerformed(java.awt.event.ActionEvent e) {
        // load address from switchAddrTextField
        if (adrTextField.getText().length() < 1) {
            nowStateLabel.setText(Bundle.getMessage("NoAddressHint"));
            return;
        }
        try {
            if (turnout != null) {
                turnout.removePropertyChangeListener(this);
            }
            turnout = InstanceManager.turnoutManagerInstance().provideTurnout(
                    adrTextField.getText());

            turnout.addPropertyChangeListener(this);
            updateTurnoutStatusFields();
            if (turnout.getCommandedState() == Turnout.THROWN) {
                nowStateLabel.setText(InstanceManager
                        .turnoutManagerInstance().getThrownText());
            }
            log.debug("about to command THROWN");
            // and set commanded state to THROWN
            turnout.setCommandedState(Turnout.THROWN);
        } catch (IllegalArgumentException ex1) {
            invalidTurnout(adrTextField.getText(), ex1);
        } catch (Exception ex2) {
            log.error("exception during throwButtonActionPerformed", ex2); // NOI18N
            nowStateLabel.setText(Bundle.getMessage("ErrorTitle"));
            nowFeedbackLabel.setText(Bundle.getMessage("BeanStateUnknown"));
        }
    }

    public void lockButtonActionPerformed(java.awt.event.ActionEvent e) {
        // load address from switchAddrTextField
        try {
            if (turnout != null) {
                turnout.removePropertyChangeListener(this);
            }
            turnout = InstanceManager.turnoutManagerInstance().provideTurnout(
                    adrTextField.getText());

            turnout.addPropertyChangeListener(this);
            updateTurnoutStatusFields();

            if (lockButton.getText().equals(LOCKED)) {
                turnout.setLocked(Turnout.CABLOCKOUT, false);
            } else if (turnout.canLock(Turnout.CABLOCKOUT)) {
                turnout.setLocked(Turnout.CABLOCKOUT, true);
            }
        } catch (IllegalArgumentException ex1) {
            invalidTurnout(adrTextField.getText(), ex1);
        } catch (Exception ex2) {
            log.error("exception during lockButtonActionPerformed", ex2); // NOI18N
            nowStateLabel.setText(Bundle.getMessage("ErrorTitle"));
            nowFeedbackLabel.setText(Bundle.getMessage("BeanStateUnknown"));
        }
    }

    public void lockPushButtonActionPerformed(java.awt.event.ActionEvent e) {
        // load address from switchAddrTextField
        try {
            if (turnout != null) {
                turnout.removePropertyChangeListener(this);
            }

            turnout = InstanceManager.turnoutManagerInstance().provideTurnout(
                    adrTextField.getText());
            turnout.addPropertyChangeListener(this);
            updateTurnoutStatusFields();

            if (lockPushButton.getText().equals(LOCKED)) {
                turnout.setLocked(Turnout.PUSHBUTTONLOCKOUT, false);
            } else if (turnout.canLock(Turnout.PUSHBUTTONLOCKOUT)) {
                turnout.setLocked(Turnout.PUSHBUTTONLOCKOUT, true);
            }
        } catch (IllegalArgumentException ex1) {
            invalidTurnout(adrTextField.getText(), ex1);
        } catch (Exception ex2) {
            log.error("exception during lockPushButtonActionPerformed", ex2); // NOI18N
            nowStateLabel.setText(Bundle.getMessage("ErrorTitle"));
            nowFeedbackLabel.setText(Bundle.getMessage("BeanStateUnknown"));
        }
    }

    /**
     * Update state field in GUI as state of turnout changes.
     */
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        // If the Commanded State changes, show transition state as "<inconsistent>" 
        if (e.getPropertyName().equals("CommandedState")) { // NOI18N
            nowStateLabel.setText(Bundle.getMessage("BeanStateInconsistent"));
        }
        if (e.getPropertyName().equals("KnownState")) { // NOI18N
            int now = ((Integer) e.getNewValue()).intValue();
            switch (now) {
                case Turnout.UNKNOWN:
                    nowStateLabel.setText(Bundle.getMessage("BeanStateUnknown"));
                    return;
                case Turnout.CLOSED:
                    nowStateLabel.setText(InstanceManager.turnoutManagerInstance().getClosedText());
                    return;
                case Turnout.THROWN:
                    nowStateLabel.setText(InstanceManager.turnoutManagerInstance().getThrownText());
                    return;
                default:
                    nowStateLabel.setText(Bundle.getMessage("BeanStateInconsistent"));
                    return;
            }
        }
        if (e.getPropertyName().equals("locked")) { // NOI18N
            if (turnout.canLock(Turnout.CABLOCKOUT)) {
                if (turnout.getLocked(Turnout.CABLOCKOUT)) {
                    lockButton.setText(LOCKED);
                } else {
                    lockButton.setText(UNLOCKED);
                }
                lockButton.setEnabled(true);
            } else {
                lockButton.setText(UNLOCKED);
                lockButton.setEnabled(false);
            }
            if (turnout.canLock(Turnout.PUSHBUTTONLOCKOUT)) {
                if (turnout.getLocked(Turnout.PUSHBUTTONLOCKOUT)) {
                    lockPushButton.setText(LOCKED);
                } else {
                    lockPushButton.setText(UNLOCKED);
                }
                lockPushButton.setEnabled(true);
            } else {
                lockPushButton.setText(UNLOCKED);
                lockPushButton.setEnabled(false);
            }
        }
        if (e.getPropertyName().equals("feedbackchange")) { // NOI18N
            updateTurnoutStatusFields();
        }
    }

    private void updateTurnoutStatusFields() {

        nowFeedbackLabel.setText(turnout.getFeedbackModeName());
        if (turnout.canLock(Turnout.CABLOCKOUT)) {
            if (turnout.getLocked(Turnout.CABLOCKOUT)) {
                lockButton.setText(LOCKED);
            } else {
                lockButton.setText(UNLOCKED);
            }
            lockButton.setEnabled(true);
        } else {
            lockButton.setText(UNLOCKED);
            lockButton.setEnabled(false);
        }
        if (turnout.canLock(Turnout.PUSHBUTTONLOCKOUT)) {
            if (turnout.getLocked(Turnout.PUSHBUTTONLOCKOUT)) {
                lockPushButton.setText(LOCKED);
            } else {
                lockPushButton.setText(UNLOCKED);
            }
            lockPushButton.setEnabled(true);
        } else {
            lockPushButton.setText(UNLOCKED);
            lockPushButton.setEnabled(false);
        }
        int knownState = turnout.getKnownState();
        switch (knownState) {
            case Turnout.UNKNOWN:
                nowStateLabel.setText(Bundle.getMessage("BeanStateUnknown"));
                return;
            case Turnout.CLOSED:
                nowStateLabel.setText(InstanceManager.turnoutManagerInstance().getClosedText());
                return;
            case Turnout.THROWN:
                nowStateLabel.setText(InstanceManager.turnoutManagerInstance().getThrownText());
                return;
            default:
                nowStateLabel.setText(Bundle.getMessage("BeanStateInconsistent"));
                return;
        }
    }

    void invalidTurnout(String name, Exception ex) {
        jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class)
                .showErrorMessage(Bundle.getMessage("ErrorTitle"),
                        (Bundle.getMessage("ErrorConvertHW", name)),
                        ex.toString(), "", true, false);
    }

    Turnout turnout = null;
    String newState = "";

    private final static Logger log = LoggerFactory.getLogger(SimpleTurnoutCtrlFrame.class);

}
