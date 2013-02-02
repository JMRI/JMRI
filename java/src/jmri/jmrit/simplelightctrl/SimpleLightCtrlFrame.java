// SimpleLightCtrlFrame.java

package jmri.jmrit.simplelightctrl;

import org.apache.log4j.Logger;
import jmri.InstanceManager;
import jmri.Light;
import java.awt.Dimension;
import java.text.DecimalFormat;
import java.util.ResourceBundle;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

/**
 * Frame controlling a single light
 * 
 * This was a copy of simple turnout control.
 * 
 * @author	Ken Cameron   Copyright (C) 2008
 * @author	Bob Jacobsen   Copyright (C) 2001, 2008
 * @version     $Revision$
 */
public class SimpleLightCtrlFrame extends jmri.util.JmriJFrame implements java.beans.PropertyChangeListener {

    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.simplelightctrl.SimpleLightCtrlBundle");
    static final ResourceBundle rbean = ResourceBundle.getBundle("jmri.NamedBeanBundle");
    
    DecimalFormat threeDigits = new DecimalFormat("000");
    DecimalFormat oneDigits = new DecimalFormat("0");
    DecimalFormat oneDotTwoDigits = new DecimalFormat("0.00");

    Light light = null;
    String newState = "";

    // GUI member declarations
    javax.swing.JLabel textAdrLabel = new javax.swing.JLabel();
    javax.swing.JTextField adrTextField = new javax.swing.JTextField(5);
    javax.swing.JButton statusButton = new javax.swing.JButton();

    javax.swing.JButton onButton = new javax.swing.JButton();
    javax.swing.JButton offButton = new javax.swing.JButton();

    javax.swing.JLabel textStateLabel = new javax.swing.JLabel();
    javax.swing.JLabel nowStateTextField = new javax.swing.JLabel();
    javax.swing.JLabel textIsEnabledLabel = new javax.swing.JLabel();
    javax.swing.JCheckBox statusIsEnabledCheckBox = new javax.swing.JCheckBox();
    javax.swing.JLabel textIsVariableLabel = new javax.swing.JLabel();
    javax.swing.JCheckBox statusIsVariableCheckBox = new javax.swing.JCheckBox();
    javax.swing.JLabel textIsTransitionLabel = new javax.swing.JLabel();
    javax.swing.JCheckBox statusIsTransitionCheckBox = new javax.swing.JCheckBox();

    javax.swing.JLabel intensityTextLabel1 = new javax.swing.JLabel();
    javax.swing.JLabel nowIntensityLabel = new javax.swing.JLabel();
    javax.swing.JTextField intensityTextField = new javax.swing.JTextField(4);
    javax.swing.JLabel intensityTextLabel2 = new javax.swing.JLabel();
    javax.swing.JButton intensityButton = new javax.swing.JButton();
    
    javax.swing.JLabel intensityMinTextLabel = new javax.swing.JLabel();
    javax.swing.JLabel nowIntensityMinLabel = new javax.swing.JLabel();
    javax.swing.JTextField intensityMinTextField = new javax.swing.JTextField(4);
    javax.swing.JLabel intensityMaxTextLabel = new javax.swing.JLabel();
    javax.swing.JLabel nowIntensityMaxLabel = new javax.swing.JLabel();
    javax.swing.JTextField intensityMaxTextField = new javax.swing.JTextField(4);
    javax.swing.JLabel transitionTimeTextLabel = new javax.swing.JLabel();
    javax.swing.JLabel nowTransitionTimeLabel = new javax.swing.JLabel();
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
        nowStateTextField.setText(rb.getString("LightStatusTextDefault"));
        nowStateTextField.setVisible(true);
        textIsEnabledLabel.setText(rb.getString("LightIsEnabledLabel"));
        textIsEnabledLabel.setVisible(true);
        statusIsEnabledCheckBox.setVisible(true);
        statusIsEnabledCheckBox.setEnabled(false);
        textIsVariableLabel.setText(rb.getString("LightIsVariableLabel"));
        textIsVariableLabel.setVisible(true);
        statusIsVariableCheckBox.setVisible(true);
        statusIsVariableCheckBox.setEnabled(false);
        textIsTransitionLabel.setText(rb.getString("LightIsTransitionLabel"));
        textIsTransitionLabel.setVisible(true);
        statusIsTransitionCheckBox.setVisible(true);
        statusIsTransitionCheckBox.setEnabled(false);
        
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
        nowIntensityLabel.setText("");
        nowIntensityLabel.setVisible(true);
        intensityTextField.setText(oneDigits.format(0));
        intensityTextField.setVisible(true);
        intensityTextLabel2.setText("%");
        intensityTextField.setToolTipText(rb.getString("LightIntensityTextToolTip"));
        
        intensityMinTextLabel.setText(rb.getString("LightMinIntensityLabel"));
        nowIntensityMinLabel.setText("");
        nowIntensityMinLabel.setVisible(true);
        intensityMinTextField.setText(oneDigits.format(0));
        intensityMinTextField.setVisible(true);
        intensityMinTextField.setToolTipText(rb.getString("LightMinIntensityToolTip"));
        intensityMaxTextLabel.setText(rb.getString("LightMaxIntensityLabel"));
        nowIntensityMaxLabel.setText("");
        nowIntensityMaxLabel.setVisible(true);
        intensityMaxTextField.setText(oneDigits.format(100));
        intensityMaxTextField.setVisible(true);
        intensityMaxTextField.setToolTipText(rb.getString("LightMinIntensityToolTip"));
        transitionTimeTextLabel.setText(rb.getString("LightTransitionTimeLabel"));
        nowTransitionTimeLabel.setText("");
        nowTransitionTimeLabel.setVisible(true);
        transitionTimeTextField.setText(oneDigits.format(0));
        transitionTimeTextField.setVisible(true);
        transitionTimeTextField.setEnabled(true);
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
        pane2.add(nowStateTextField);
        pane2.add(textIsEnabledLabel);
        pane2.add(statusIsEnabledCheckBox);
        pane2.add(textIsVariableLabel);
        pane2.add(statusIsVariableCheckBox);
        pane2.add(textIsTransitionLabel);
        pane2.add(statusIsTransitionCheckBox);
        getContentPane().add(pane2);

        pane2 = new JPanel();
        pane2.add(onButton);
        pane2.add(offButton);
        getContentPane().add(pane2);

        pane2 = new JPanel();
        pane2.add(intensityTextLabel1);
        pane2.add(nowIntensityLabel);
        pane2.add(intensityTextField);
        pane2.add(intensityTextLabel2);
        pane2.add(intensityButton);
        getContentPane().add(pane2);

        pane2 = new JPanel();
        pane2.add(intensityMinTextLabel);
        pane2.add(nowIntensityMinLabel);
        pane2.add(intensityMinTextField);
        pane2.add(intensityMaxTextLabel);
        pane2.add(nowIntensityMaxLabel);
        pane2.add(intensityMaxTextField);
        pane2.add(transitionTimeTextLabel);
        pane2.add(nowTransitionTimeLabel);
        pane2.add(transitionTimeTextField);
        getContentPane().add(pane2);

        pane2 = new JPanel();
        pane2.add(applyButton);
        getContentPane().add(pane2);

        // add help menu to window
    	addHelpMenu("package.jmri.jmrit.simplelightctrl.SimpleLightCtrl", true);

        setMinimumSize(new Dimension(600, 200));
        setSize(700, 300);
        pack();

    }
    

    public void offButtonActionPerformed(java.awt.event.ActionEvent e) {
		// load address from switchAddrTextField
		try {
			if (light != null) {
				// we're changing the light we're watching
				light.removePropertyChangeListener(this);
			}
			light = InstanceManager.lightManagerInstance().provideLight(
					adrTextField.getText());

			if (light == null) {
				log.error(rb.getString("LightErrorButtonNameBad") + adrTextField.getText());
			} else {
				light.addPropertyChangeListener(this);
				if (log.isDebugEnabled())
					log.debug("about to command CLOSED");
				// and set commanded state to CLOSED
				light.setState(Light.OFF);
			}
		} catch (Exception ex) {
			log.error(rb.getString("LightErrorOffButtonException") + ex.toString());
			nowStateTextField.setText("ERROR");
		}
	}

    public void onButtonActionPerformed(java.awt.event.ActionEvent e) {
		// load address from switchAddrTextField
		try {
			if (light != null) {
				// we're changing the light we're watching
				light.removePropertyChangeListener(this);
			}
			light = InstanceManager.lightManagerInstance().provideLight(
					adrTextField.getText());

			if (light == null) {
				log.error(rb.getString("LightErrorButtonNameBad") + adrTextField.getText());
			} else {
				light.addPropertyChangeListener(this);
				if (log.isDebugEnabled())
					log.debug("about to command ON");
				// and set commanded state to ON
				light.setState(Light.ON);
			}
		} catch (Exception ex) {
			log.error(rb.getString("LightErrorOnButtonException") + ex.toString());
			nowStateTextField.setText("ERROR");
		}
	}

    public void intensityButtonActionPerformed(java.awt.event.ActionEvent e) {
		// load address from switchAddrTextField
		try {
			if (light != null) {
				// we're changing the light we're watching
				light.removePropertyChangeListener(this);
			}
			light = InstanceManager.lightManagerInstance().provideLight(
					adrTextField.getText());

			if (light == null) {
				log.error(rb.getString("LightErrorButtonNameBad") + adrTextField.getText());
			} else {
				light.addPropertyChangeListener(this);
				if (log.isDebugEnabled())
					log.debug("about to command DIM");
				// and set commanded state to DIM
				light.setTargetIntensity(Double.parseDouble(intensityTextField.getText().trim()) / 100);
			}
		} catch (Exception ex) {
			log.error(rb.getString("LightErrorIntensityButtonException") + ex.toString());
			nowStateTextField.setText("ERROR");
		}
	}
    
    /**
     * handle changes for intensity, rate, etc...
     */
    public void applyButtonActionPerformed(java.awt.event.ActionEvent e) {
		// load address from switchAddrTextField
    	try {
			if (light != null) {
				// we're changing the light we're watching
				light.removePropertyChangeListener(this);
	    	}
	    	light = InstanceManager.lightManagerInstance().provideLight(adrTextField.getText());
	
			if (light == null) {
				nowStateTextField.setText(rb.getString("LightErrorButtonNameBad") + adrTextField.getText());
			} else {
				double min = Double.parseDouble(intensityMinTextField.getText())/100.;
				double max = Double.parseDouble(intensityMaxTextField.getText())/100.;
				double time = Double.parseDouble(transitionTimeTextField.getText());
		     	if (log.isDebugEnabled()) {
		     		log.debug("setting min: " + min + " max: " + max + " transition: " + time);
		     	}
		     	light.setMinIntensity(min);
				light.setMaxIntensity(max);
				light.setTransitionTime(time);
				updateLightStatusFields(false);
			}
    	} catch (Exception ex) {
			log.error(rb.getString("LightErrorApplyButtonException") + ex.toString());
			nowStateTextField.setText("ERROR");
    	}
    }

    /**
     * handles request to update status
     * @param e
     */
    public void statusButtonActionPerformed(java.awt.event.ActionEvent e) {
		// load address from switchAddrTextField
    	try {
			if (light != null) {
				// we're changing the light we're watching
				light.removePropertyChangeListener(this);
			}
			light = InstanceManager.lightManagerInstance().provideLight(adrTextField.getText());
	
			if (light == null) {
				nowStateTextField.setText(rb.getString("LightErrorButtonNameBad") + adrTextField.getText());
			} else {
				updateLightStatusFields(true);
			}
    	} catch (Exception ex) {
			log.error(rb.getString("LightErrorStatusButtonException") + ex.toString());
			nowStateTextField.setText("ERROR");
    	}
	}

//	public void lockButtonActionPerformed(java.awt.event.ActionEvent e) {
//		// load address from switchAddrTextField
//		try {
//			if (light != null)
//				light.removePropertyChangeListener(this);
//			light = InstanceManager.lightManagerInstance().provideLight(
//					adrTextField.getText());
//
//			if (light == null) {
//				log.error("Light " + adrTextField.getText()
//						+ " is not available");
//			} else {
//				light.addPropertyChangeListener(this);
//
//			}
//		} catch (Exception ex) {
//			log.error("LockButtonActionPerformed, exception: "
//							+ ex.toString());
//			nowStateTextField.setText("ERROR");
//		}
//	}
	
//	public void lockPushButtonActionPerformed(java.awt.event.ActionEvent e) {
//		// load address from switchAddrTextField
//		try {
//			if (light != null)
//				light.removePropertyChangeListener(this);
//			light = InstanceManager.lightManagerInstance().provideLight(
//					adrTextField.getText());
//
//			if (light == null) {
//				log.error("Light " + adrTextField.getText()
//						+ " is not available");
//			} else {
//				light.addPropertyChangeListener(this);
//				
//			}
//		} catch (Exception ex) {
//			log.error("LockPushButtonActionPerformed, exception: "
//							+ ex.toString());
//			nowStateTextField.setText("ERROR");
//		}
//	}

    // update state field in GUI as state of light changes
    public void propertyChange(java.beans.PropertyChangeEvent e) {
     	if (log.isDebugEnabled()) {
         	log.debug("recv propertyChange: " + e.getPropertyName() + " " + e.getOldValue() + " -> " + e.getNewValue());
     	}
     	updateLightStatusFields(false);
     }

    private void updateLightStatusFields(boolean flag){
    	int knownState = light.getState();
        switch (knownState) {
            case Light.ON: nowStateTextField.setText(rbean.getString("LightStateOn")); break;
            case Light.INTERMEDIATE: nowStateTextField.setText(rbean.getString("LightStateIntermediate")); break;
            case Light.OFF: nowStateTextField.setText(rbean.getString("LightStateOff")); break;
            case Light.TRANSITIONINGTOFULLON: nowStateTextField.setText(rbean.getString("LightStateTransitioningToFullOn")); break;
            case Light.TRANSITIONINGHIGHER: nowStateTextField.setText(rbean.getString("LightStateTransitioningHigher")); break;
            case Light.TRANSITIONINGLOWER: nowStateTextField.setText(rbean.getString("LightStateTransitioningLower")); break;
            case Light.TRANSITIONINGTOFULLOFF: nowStateTextField.setText(rbean.getString("LightStateTransitioningToFullOff")); break;
            default: nowStateTextField.setText("Unexpected value: " + knownState); break; 
        }
        statusIsEnabledCheckBox.setSelected(light.getEnabled());
        statusIsVariableCheckBox.setSelected(light.isIntensityVariable());
        statusIsTransitionCheckBox.setSelected(light.isTransitionAvailable());
        nowIntensityLabel.setText(oneDigits.format(light.getCurrentIntensity() * 100));
        nowTransitionTimeLabel.setText(oneDotTwoDigits.format(light.getTransitionTime()));
        nowIntensityMinLabel.setText(oneDigits.format(light.getMinIntensity() * 100));
        nowIntensityMaxLabel.setText(oneDigits.format(light.getMaxIntensity() * 100));
        if (flag) {
        	intensityTextField.setText(oneDigits.format(light.getTargetIntensity() * 100));
		    transitionTimeTextField.setText(oneDotTwoDigits.format(light.getTransitionTime()));
		    intensityMinTextField.setText(oneDigits.format(light.getMinIntensity() * 100));
		    intensityMaxTextField.setText(oneDigits.format(light.getMaxIntensity() * 100));
        }
      }

    static Logger log = Logger.getLogger(SimpleLightCtrlFrame.class.getName());
}



