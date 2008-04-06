// SimpleLightCtrlFrame.java

package jmri.jmrit.simplelightctrl;

import jmri.InstanceManager;
import jmri.Light;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.text.DecimalFormat;
import java.util.ResourceBundle;

import javax.swing.BoxLayout;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

/**
 * Frame controlling a single light
 * 
 * This was a copy of simple turnout control.
 * 
 * @author	Ken Cameron   Copyright (C) 2008
 * @author	Bob Jacobsen   Copyright (C) 2001, 2008
 * @version     $Revision: 1.5 $
 */
public class SimpleLightCtrlFrame extends jmri.util.JmriJFrame implements java.beans.PropertyChangeListener {

    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.simplelightctrl.SimpleLightCtrlBundle");
    
	private static final String LOCKED = "Locked";
	private static final String UNLOCKED = "Normal";

    DecimalFormat threeDigits = new DecimalFormat("000");
    DecimalFormat oneDigits = new DecimalFormat("0");

    // GUI member declarations
    javax.swing.JLabel textAdrLabel = new javax.swing.JLabel();
    javax.swing.JTextField adrTextField = new javax.swing.JTextField(5);
    javax.swing.JButton statusButton = new javax.swing.JButton();

    javax.swing.JButton onButton = new javax.swing.JButton();
    javax.swing.JButton offButton = new javax.swing.JButton();

    javax.swing.JLabel textStateLabel = new javax.swing.JLabel();
    javax.swing.JLabel nowStateLabel = new javax.swing.JLabel();

    javax.swing.JLabel intensityTextLabel1 = new javax.swing.JLabel();
    javax.swing.JTextField intensityTextField = new javax.swing.JTextField(4);
    javax.swing.JLabel intensityTextLabel2 = new javax.swing.JLabel();
    javax.swing.JButton intensityButton = new javax.swing.JButton();
    
    javax.swing.JLabel intensityMinTextLabel = new javax.swing.JLabel();
    javax.swing.JTextField intensityMinTextField = new javax.swing.JTextField(4);
    javax.swing.JLabel intensityMaxTextLabel = new javax.swing.JLabel();
    javax.swing.JTextField intensityMaxTextField = new javax.swing.JTextField(4);
    javax.swing.JLabel transitionTimeTextLabel = new javax.swing.JLabel();
    javax.swing.JTextField transitionTimeTextField = new javax.swing.JTextField(4);
    
    javax.swing.JButton applyButton = new javax.swing.JButton();
    
    public SimpleLightCtrlFrame() {
        super();
        
        // configure items for GUI
        textAdrLabel.setText(rb.getString("LightAdrLabel"));
        textAdrLabel.setVisible(true);

        adrTextField.setText("");
        adrTextField.setVisible(true);
        adrTextField.setToolTipText(rb.getString("LightAdrTextToolTip"));

        statusButton.setText(rb.getString("LightGetStatusButton"));
        statusButton.setVisible(true);
        statusButton.setToolTipText(rb.getString("LightGetStatusToolTip"));
        statusButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    statusButtonActionPerformed(e);
                }
            });

        textStateLabel.setText(rb.getString("LightStatusLabel"));
        textStateLabel.setVisible(true);
        nowStateLabel.setText(rb.getString("LightStatusTextDefault"));
        nowStateLabel.setVisible(true);
        
        onButton.setText(rb.getString("LightOnButton"));
        onButton.setVisible(true);
        onButton.setToolTipText(rb.getString("LightOnButtonToolTip"));
        onButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    onButtonActionPerformed(e);
                }
            });

        offButton.setText(rb.getString("LightOffButton"));
        offButton.setVisible(true);
        offButton.setToolTipText(rb.getString("LightOffButtonToolTip"));
        offButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    offButtonActionPerformed(e);
                }
            });
        
        intensityTextLabel1.setText(rb.getString("LightIntensityTextLabel"));
        intensityTextLabel1.setVisible(true);
        intensityTextField.setText(oneDigits.format(0));
        intensityTextField.setVisible(true);
        intensityTextLabel2.setText("%");
        intensityTextField.setToolTipText(rb.getString("LightIntensityTextToolTip"));
        
        intensityMinTextLabel.setText(rb.getString("LightMinIntensityLabel"));
        intensityMinTextField.setText(oneDigits.format(0));
        intensityMinTextField.setVisible(true);
        intensityMinTextField.setToolTipText(rb.getString("LightMinIntensityToolTip"));
        intensityMaxTextLabel.setText(rb.getString("LightMaxIntensityLabel"));
        intensityMaxTextField.setText(oneDigits.format(100));
        intensityMaxTextField.setVisible(true);
        intensityMaxTextField.setToolTipText(rb.getString("LightMinIntensityToolTip"));
        transitionTimeTextLabel.setText(rb.getString("LightTransitionTimeLabel"));
        transitionTimeTextField.setText(oneDigits.format(0));
        transitionTimeTextField.setVisible(true);
        transitionTimeTextField.setEnabled(false);
        transitionTimeTextField.setToolTipText(rb.getString("LightTransitionTimeToolTip"));
        intensityButton.setText(rb.getString("LightSetButton"));
        intensityButton.setVisible(true);
        intensityButton.setToolTipText(rb.getString("LightSetButtonToolTip"));
        intensityButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    intensityButtonActionPerformed(e);
                }
            });

        applyButton.setText(rb.getString("LightApplyButton"));
        applyButton.setVisible(true);
        applyButton.setToolTipText(rb.getString("LightApplyButtonToolTip"));
        applyButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				applyButtonActionPerformed(e);
			}
		});
        
        // general GUI config
        setTitle(rb.getString("LightBorder"));
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // install items in GUI
        JPanel pane2 = new JPanel();
        pane2.add(textAdrLabel);
        pane2.add(adrTextField);
        pane2.add(statusButton);
        getContentPane().add(pane2);

        pane2 = new JPanel();
        pane2.add(textStateLabel);
        pane2.add(nowStateLabel);
        getContentPane().add(pane2);

        pane2 = new JPanel();
        pane2.add(onButton);
        pane2.add(offButton);
        getContentPane().add(pane2);

        pane2 = new JPanel();
        pane2.add(intensityTextLabel1);
        pane2.add(intensityTextField);
        pane2.add(intensityTextLabel2);
        pane2.add(intensityButton);
        getContentPane().add(pane2);

        pane2 = new JPanel();
        pane2.add(intensityMinTextLabel);
        pane2.add(intensityMinTextField);
        pane2.add(intensityMaxTextLabel);
        pane2.add(intensityMaxTextField);
        pane2.add(transitionTimeTextLabel);
        pane2.add(transitionTimeTextField);
        getContentPane().add(pane2);

        pane2 = new JPanel();
        pane2.add(applyButton);
        getContentPane().add(pane2);

        // add help menu to window
    	addHelpMenu("package.jmri.jmrit.simplelightctrl.SimpleLightCtrl", true);

        setMinimumSize(new Dimension(200, 200));
        setSize(300, 300);
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
				if (log.isDebugEnabled())
					log.debug("about to command CLOSED");
				// and set commanded state to CLOSED
				light.setState(Light.OFF);
				intensityTextField.setText(oneDigits.format(light.getTargetIntensity() * 100));
			}
		} catch (Exception ex) {
			log.error("offButtonActionPerformed, exception: "
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
				if (log.isDebugEnabled())
					log.debug("about to command ON");
				// and set commanded state to ON
				light.setState(Light.ON);
				intensityTextField.setText(oneDigits.format(light.getTargetIntensity() * 100));
			}
		} catch (Exception ex) {
			log.error("onButtonActionPerformed, exception: "
							+ ex.toString());
			nowStateLabel.setText("ERROR");
		}
	}

    public void intensityButtonActionPerformed(java.awt.event.ActionEvent e) {
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
				if (log.isDebugEnabled())
					log.debug("about to command DIM");
				// and set commanded state to DIM
				light.setTargetIntensity(Double.parseDouble(intensityTextField.getText().trim()) / 100);
			}
		} catch (Exception ex) {
			log.error("intensityButtonActionPerformed, exception: "
							+ ex.toString());
			nowStateLabel.setText("ERROR");
		}
	}
    
    /**
     * handle changes for intensity, rate, etc...
     */
    public void applyButtonActionPerformed(java.awt.event.ActionEvent e) {
		// load address from switchAddrTextField
    	try {
			if (light != null)
				light.removePropertyChangeListener(this);
			light = InstanceManager.lightManagerInstance().provideLight(adrTextField.getText());
	
			if (light == null) {
				nowStateLabel.setText("Light " + adrTextField.getText() + " is not available");
			} else {
				double min = Double.parseDouble(intensityMinTextField.getText())/100.;
				double max = Double.parseDouble(intensityMaxTextField.getText())/100.;
				double time = Double.parseDouble(transitionTimeTextField.getText());
				log.debug("setting min: " + min + " max: " + max + " transition: " + time);
				light.setMinIntensity(min);
				light.setMaxIntensity(max);
				light.setTransitionTime(time);
				updateLightStatusFields();
			}
    	} catch (Exception ex) {
			log.error("applyButtonActionPerformed, exception: "
					+ ex.toString());
			nowStateLabel.setText("ERROR");
    	}
    }

    /**
     * handles request to update status
     * @param e
     */
    public void statusButtonActionPerformed(java.awt.event.ActionEvent e) {
		// load address from switchAddrTextField
    	try {
			if (light != null)
				light.removePropertyChangeListener(this);
			light = InstanceManager.lightManagerInstance().provideLight(adrTextField.getText());
	
			if (light == null) {
				nowStateLabel.setText("Light " + adrTextField.getText() + " is not available");
			} else {
				intensityTextField.setText(oneDigits.format(light.getTargetIntensity() * 100));
				updateLightStatusFields();
			}
    	} catch (Exception ex) {
			log.error("LockButtonActionPerformed, exception: "
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
				
			}
		} catch (Exception ex) {
			log.error("LockPushButtonActionPerformed, exception: "
							+ ex.toString());
			nowStateLabel.setText("ERROR");
		}
	}

    // update state field in GUI as state of light changes
    public void propertyChange(java.beans.PropertyChangeEvent e) {
     	updateLightStatusFields();
     }

    static final ResourceBundle rbean = ResourceBundle.getBundle("jmri.NamedBeanBundle");

    private void updateLightStatusFields(){
    	
		int knownState = light.getState();
        switch (knownState) {
            case Light.ON: nowStateLabel.setText(rbean.getString("LightStateOn")); break;
            case Light.INTERMEDIATE: nowStateLabel.setText(rbean.getString("LightStateIntermediate")); break;
            case Light.OFF: nowStateLabel.setText(rbean.getString("LightStateOff")); break;
            case Light.TRANSITIONINGTOFULLON: nowStateLabel.setText(rbean.getString("LightTransitioningToFullOn")); break;
            case Light.TRANSITIONINGHIGHER: nowStateLabel.setText(rbean.getString("LightTransitioningHigher")); break;
            case Light.TRANSITIONINGLOWER: nowStateLabel.setText(rbean.getString("LightTransitioningLower")); break;
            case Light.TRANSITIONINGTOFULLOFF: nowStateLabel.setText(rbean.getString("LightTransitioningToFullOff")); break;
            default: nowStateLabel.setText("Unexpected value: "+knownState); break; 
        }
        intensityTextField.setText(oneDigits.format(light.getTargetIntensity() * 100));
        transitionTimeTextField.setText(oneDigits.format(light.getTransitionTime()));
        intensityMinTextField.setText(oneDigits.format(light.getMinIntensity() * 100));
        intensityMaxTextField.setText(oneDigits.format(light.getMaxIntensity() * 100));
      }

    Light light = null;

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SimpleLightCtrlFrame.class.getName());

    String newState = "";
}



