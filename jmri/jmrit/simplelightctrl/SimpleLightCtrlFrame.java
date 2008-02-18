// SimpleLightCtrlFrame.java

package jmri.jmrit.simplelightctrl;

import jmri.InstanceManager;
import jmri.Light;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JMenuBar;

/**
 * Frame controlling a single light
 * 
 * This was a copy of simple turnout control.
 * 
 * @author	Ken Cameron   Copyright (C) 2008
 * @author	Bob Jacobsen   Copyright (C) 2001
 * @version     $Revision: 1.1 $
 */
public class SimpleLightCtrlFrame extends jmri.util.JmriJFrame implements java.beans.PropertyChangeListener {
	
	private static final String LOCKED = "Locked";
	private static final String UNLOCKED = "Normal";

    // GUI member declarations
    javax.swing.JLabel textAdrLabel = new javax.swing.JLabel();
    javax.swing.JTextField adrTextField = new javax.swing.JTextField(3);

    javax.swing.JButton onButton = new javax.swing.JButton();
    javax.swing.JButton offButton = new javax.swing.JButton();

    javax.swing.JLabel textStateLabel = new javax.swing.JLabel();
    javax.swing.JLabel nowStateLabel = new javax.swing.JLabel();
    
    public SimpleLightCtrlFrame() {
        super();
        
        // configure items for GUI
        textAdrLabel.setText(" light:");
        textAdrLabel.setVisible(true);

        adrTextField.setText("");
        adrTextField.setVisible(true);
        adrTextField.setToolTipText("light number being controlled");

        onButton.setText("On");
        onButton.setVisible(true);
        onButton.setToolTipText("Press to turn light on/bright");
        onButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    onButtonActionPerformed(e);
                }
            });

        offButton.setText("Off");
        offButton.setVisible(true);
        offButton.setToolTipText("Press to turn light off/dim");
        offButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    offButtonActionPerformed(e);
                }
            });

        textStateLabel.setText(" current state: ");
        textStateLabel.setVisible(true);

        nowStateLabel.setText("<unknown>");
        nowStateLabel.setVisible(true);
        
        // general GUI config
        setTitle("Light Control");
        getContentPane().setLayout(new GridLayout(8,2));

        // install items in GUI
        getContentPane().add(textAdrLabel);
        getContentPane().add(adrTextField);

        getContentPane().add(textStateLabel);
        getContentPane().add(nowStateLabel);
        
        getContentPane().add(onButton);
        getContentPane().add(offButton);
              
        // add help menu to window
    	addHelpMenu("package.jmri.jmrit.simplelightctrl.SimpleLightCtrl", true);

        pack();

    }

    public void offButtonActionPerformed(java.awt.event.ActionEvent e) {
		// load address from switchAddrTextField
		try {
			if (light != null)
				light.removePropertyChangeListener(this);
			light = InstanceManager.lightManagerInstance().provideLight(
					adrTextField.getText());

			if (light == null) {
				log.error("Light " + adrTextField.getText()
						+ " is not available");
			} else {
				light.addPropertyChangeListener(this);
				updateLightStatusFields();
				if (light.getState() == light.OFF) {
					nowStateLabel.setText("OFF");
				}
				if (log.isDebugEnabled())
					log.debug("about to command CLOSED");
				// and set commanded state to CLOSED
				light.setState(Light.OFF);
			}
		} catch (Exception ex) {
			log.error("closeButtonActionPerformed, exception: "
							+ ex.toString());
			nowStateLabel.setText("ERROR");
		}
	}

    public void onButtonActionPerformed(java.awt.event.ActionEvent e) {
		// load address from switchAddrTextField
		try {
			if (light != null)
				light.removePropertyChangeListener(this);
			light = InstanceManager.lightManagerInstance().provideLight(
					adrTextField.getText());

			if (light == null) {
				log.error("Light " + adrTextField.getText()
						+ " is not available");
			} else {
				light.addPropertyChangeListener(this);
				updateLightStatusFields();
				if (light.getState() == light.ON) {
					nowStateLabel.setText("ON");
				}
				if (log.isDebugEnabled())
					log.debug("about to command ON");
				// and set commanded state to ON
				light.setState(Light.ON);
			}
		} catch (Exception ex) {
			log.error("lightButtonActionPerformed, exception: "
							+ ex.toString());
			nowStateLabel.setText("ERROR");
		}
	}

	public void lockButtonActionPerformed(java.awt.event.ActionEvent e) {
		// load address from switchAddrTextField
		try {
			if (light != null)
				light.removePropertyChangeListener(this);
			light = InstanceManager.lightManagerInstance().provideLight(
					adrTextField.getText());

			if (light == null) {
				log.error("Light " + adrTextField.getText()
						+ " is not available");
			} else {
				light.addPropertyChangeListener(this);
				updateLightStatusFields();

			}
		} catch (Exception ex) {
			log.error("LockButtonActionPerformed, exception: "
							+ ex.toString());
			nowStateLabel.setText("ERROR");
		}
	}
	
	public void lockPushButtonActionPerformed(java.awt.event.ActionEvent e) {
		// load address from switchAddrTextField
		try {
			if (light != null)
				light.removePropertyChangeListener(this);
			light = InstanceManager.lightManagerInstance().provideLight(
					adrTextField.getText());

			if (light == null) {
				log.error("Light " + adrTextField.getText()
						+ " is not available");
			} else {
				light.addPropertyChangeListener(this);
				updateLightStatusFields();
				
			}
		} catch (Exception ex) {
			log.error("LockPushButtonActionPerformed, exception: "
							+ ex.toString());
			nowStateLabel.setText("ERROR");
		}
	}


    // update state field in GUI as state of light changes
    public void propertyChange(java.beans.PropertyChangeEvent e) {
    	// If the Commanded State changes, show transition state as "<inconsistent>" 
     	if (e.getPropertyName().equals("CommandedState")){
    		nowStateLabel.setText("<inconsistent>");
 		}
        if (e.getPropertyName().equals("KnownState")) {
            int now = ((Integer) e.getNewValue()).intValue();
            switch (now) {
            case Light.OFF:
                nowStateLabel.setText("OFF");
                return;
            case Light.ON:
                nowStateLabel.setText("ON");
                return;
            default:
                nowStateLabel.setText("<inconsistent>");
                return;
            }
        }
     }
    
    private void updateLightStatusFields(){
    	
		int knownState = light.getState();
        switch (knownState) {
        case Light.OFF:
            nowStateLabel.setText("OFF");
            return;
        case Light.ON:
            nowStateLabel.setText("ON");
            return;
        default:
            nowStateLabel.setText("<inconsistent>");
            return;
        }
      }

    Light light = null;

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SimpleLightCtrlFrame.class.getName());

    String newState = "";
}



