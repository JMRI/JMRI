// SimpleTurnoutCtrlFrame.java

package jmri.jmrit.simpleturnoutctrl;

import jmri.InstanceManager;
import jmri.Turnout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JMenuBar;

/**
 * Frame controlling a single turnout
 * @author	Bob Jacobsen   Copyright (C) 2001
 * @version     $Revision: 1.17 $
 */
public class SimpleTurnoutCtrlFrame extends jmri.util.JmriJFrame implements java.beans.PropertyChangeListener {
	
	private static final String LOCKED = "Locked";
	private static final String UNLOCKED = "Normal";

    // GUI member declarations
    javax.swing.JLabel textAdrLabel = new javax.swing.JLabel();
    javax.swing.JTextField adrTextField = new javax.swing.JTextField(3);

    javax.swing.JButton throwButton = new javax.swing.JButton();
    javax.swing.JButton closeButton = new javax.swing.JButton();

    javax.swing.JLabel textStateLabel = new javax.swing.JLabel();
    javax.swing.JLabel nowStateLabel = new javax.swing.JLabel();
    
    javax.swing.JLabel textFeedbackLabel = new javax.swing.JLabel();
    javax.swing.JLabel nowFeedbackLabel = new javax.swing.JLabel();
    
    javax.swing.JLabel textAdvancedLabel = new javax.swing.JLabel();
    javax.swing.JLabel textFeaturesLabel = new javax.swing.JLabel();
    
    javax.swing.JLabel lockButtonLabel = new javax.swing.JLabel();
    javax.swing.JButton lockButton = new javax.swing.JButton();

    public SimpleTurnoutCtrlFrame() {

        // configure items for GUI
        textAdrLabel.setText(" turnout:");
        textAdrLabel.setVisible(true);

        adrTextField.setText("");
        adrTextField.setVisible(true);
        adrTextField.setToolTipText("turnout number being controlled");

        throwButton.setText(InstanceManager.turnoutManagerInstance().getThrownText());
        throwButton.setVisible(true);
        throwButton.setToolTipText("Press to set turnout '"+
			InstanceManager.turnoutManagerInstance().getThrownText()+"'");
        throwButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    throwButtonActionPerformed(e);
                }
            });

        closeButton.setText(InstanceManager.turnoutManagerInstance().getClosedText());
        closeButton.setVisible(true);
        closeButton.setToolTipText("Press to set turnout '"+
			InstanceManager.turnoutManagerInstance().getClosedText()+"'");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    closeButtonActionPerformed(e);
                }
            });

        textStateLabel.setText(" current state: ");
        textStateLabel.setVisible(true);

        nowStateLabel.setText("<unknown>");
        nowStateLabel.setVisible(true);
        
        textFeedbackLabel.setText(" feedback mode: ");
        textFeedbackLabel.setVisible(true);

        nowFeedbackLabel.setText("<unknown>");
        nowFeedbackLabel.setVisible(true);
        
        textAdvancedLabel.setText("      -- Advanced ");
        textAdvancedLabel.setVisible(true);
        
        textFeaturesLabel.setText("Features -- ");
        textFeaturesLabel.setVisible(true);
        
        lockButtonLabel.setText("Cab operation: ");
        lockButtonLabel.setVisible(true);
        
        lockButton.setText(UNLOCKED);
        lockButton.setVisible(true);
        lockButton.setEnabled(false);
        lockButton.setToolTipText ("When locked, turnout can not be changed by cabs"); 
        lockButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                lockButtonActionPerformed(e);
            }
        });
        // general GUI config
        setTitle("Turnout Control");
        getContentPane().setLayout(new GridLayout(6,2));

        // install items in GUI
        getContentPane().add(textAdrLabel);
        getContentPane().add(adrTextField);

        getContentPane().add(textStateLabel);
        getContentPane().add(nowStateLabel);
        
        getContentPane().add(textFeedbackLabel);
        getContentPane().add(nowFeedbackLabel);

        getContentPane().add(throwButton);
        getContentPane().add(closeButton);
        
        getContentPane().add(textAdvancedLabel);
        getContentPane().add(textFeaturesLabel);
        
        getContentPane().add(lockButtonLabel);
        getContentPane().add(lockButton);
      
        // add help menu to window
    	addHelpMenu("package.jmri.jmrit.simpleturnoutctrl.SimpleTurnoutCtrl", true);

        pack();

    }

    public void closeButtonActionPerformed(java.awt.event.ActionEvent e) {
		// load address from switchAddrTextField
		try {
			if (turnout != null)
				turnout.removePropertyChangeListener(this);
			turnout = InstanceManager.turnoutManagerInstance().provideTurnout(
					adrTextField.getText());

			if (turnout == null) {
				log.error("Turnout " + adrTextField.getText()
						+ " is not available");
			} else {
				turnout.addPropertyChangeListener(this);
				updateTurnoutStatusFields();
				if (turnout.getCommandedState() == turnout.CLOSED) {
					nowStateLabel.setText(InstanceManager
							.turnoutManagerInstance().getClosedText());
				}
				if (log.isDebugEnabled())
					log.debug("about to command CLOSED");
				// and set commanded state to CLOSED
				turnout.setCommandedState(Turnout.CLOSED);
			}
		} catch (Exception ex) {
			log.error("closeButtonActionPerformed, exception: "
							+ ex.toString());
			nowStateLabel.setText("ERROR");
			nowFeedbackLabel.setText("<unknown>");
		}
	}

    public void throwButtonActionPerformed(java.awt.event.ActionEvent e) {
		// load address from switchAddrTextField
		try {
			if (turnout != null)
				turnout.removePropertyChangeListener(this);
			turnout = InstanceManager.turnoutManagerInstance().provideTurnout(
					adrTextField.getText());

			if (turnout == null) {
				log.error("Turnout " + adrTextField.getText()
						+ " is not available");
			} else {
				turnout.addPropertyChangeListener(this);
				updateTurnoutStatusFields();
				if (turnout.getCommandedState() == turnout.THROWN) {
					nowStateLabel.setText(InstanceManager
							.turnoutManagerInstance().getThrownText());
				}
				if (log.isDebugEnabled())
					log.debug("about to command THROWN");
				// and set commanded state to THROWN
				turnout.setCommandedState(Turnout.THROWN);
			}
		} catch (Exception ex) {
			log.error("throwButtonActionPerformed, exception: "
							+ ex.toString());
			nowStateLabel.setText("ERROR");
			nowFeedbackLabel.setText("<unknown>");
		}
	}

	public void lockButtonActionPerformed(java.awt.event.ActionEvent e) {
		// load address from switchAddrTextField
		try {
			if (turnout != null)
				turnout.removePropertyChangeListener(this);
			turnout = InstanceManager.turnoutManagerInstance().provideTurnout(
					adrTextField.getText());

			if (turnout == null) {
				log.error("Turnout " + adrTextField.getText()
						+ " is not available");
			} else {
				turnout.addPropertyChangeListener(this);
				updateTurnoutStatusFields();

				if (lockButton.getText() == LOCKED) {
					turnout.setLocked(false);
				} else if (turnout.canLock()) {
					turnout.setLocked(true);
				}
			}
		} catch (Exception ex) {
			log.error("LockButtonActionPerformed, exception: "
							+ ex.toString());
			nowStateLabel.setText("ERROR");
			nowFeedbackLabel.setText("<unknown>");
		}
	}

    // update state field in GUI as state of turnout changes
    public void propertyChange(java.beans.PropertyChangeEvent e) {
    	// If the Commanded State changes, show transition state as "<inconsistent>" 
     	if (e.getPropertyName().equals("CommandedState")){
    		nowStateLabel.setText("<inconsistent>");
 		}
        if (e.getPropertyName().equals("KnownState")) {
            int now = ((Integer) e.getNewValue()).intValue();
            switch (now) {
            case Turnout.UNKNOWN:
                nowStateLabel.setText("<unknown>");
                return;
            case Turnout.CLOSED:
                nowStateLabel.setText(InstanceManager.turnoutManagerInstance().getClosedText());
                return;
            case Turnout.THROWN:
                nowStateLabel.setText(InstanceManager.turnoutManagerInstance().getThrownText());
                return;
            default:
                nowStateLabel.setText("<inconsistent>");
                return;
            }
        }
        if (e.getPropertyName().equals("locked")) {
        	if (turnout.canLock()) {
    			if (turnout.getLocked()){
    				lockButton.setText(LOCKED);
    			}else{
    				lockButton.setText(UNLOCKED);
    			}
    			lockButton.setEnabled(true);
    		}else{
    			lockButton.setText(UNLOCKED);
    			lockButton.setEnabled(false);
    		}
        }
        if (e.getPropertyName().equals("feedbackchange")) {
        	updateTurnoutStatusFields();
        }
     }
    
    private void updateTurnoutStatusFields(){
    	
    	nowFeedbackLabel.setText (turnout.getFeedbackModeName());
		if (turnout.canLock()) {
			if (turnout.getLocked()){
				lockButton.setText(LOCKED);
			}else{
				lockButton.setText(UNLOCKED);
			}
			lockButton.setEnabled(true);
		}else{
			lockButton.setText(UNLOCKED);
			lockButton.setEnabled(false);
		}
		int knownState = turnout.getKnownState();
        switch (knownState) {
        case Turnout.UNKNOWN:
            nowStateLabel.setText("<unknown>");
            return;
        case Turnout.CLOSED:
            nowStateLabel.setText(InstanceManager.turnoutManagerInstance().getClosedText());
            return;
        case Turnout.THROWN:
            nowStateLabel.setText(InstanceManager.turnoutManagerInstance().getThrownText());
            return;
        default:
            nowStateLabel.setText("<inconsistent>");
            return;
        }
      }

    Turnout turnout = null;

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SimpleTurnoutCtrlFrame.class.getName());

    String newState = "";
}



