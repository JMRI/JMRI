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
 * @author	Bob Jacobsen   Copyright (C) 2001
 * @version     $Revision: 1.3 $
 */
public class SimpleLightCtrlFrame extends jmri.util.JmriJFrame implements java.beans.PropertyChangeListener {

    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.simplelightctrl.SimpleLightCtrlBundle");
    
	private static final String LOCKED = "Locked";
	private static final String UNLOCKED = "Normal";

    DecimalFormat threeDigits = new DecimalFormat("000");
    DecimalFormat oneDigits = new DecimalFormat("0");

    // GUI member declarations
    javax.swing.JLabel textAdrLabel = new javax.swing.JLabel();
    javax.swing.JTextField adrTextField = new javax.swing.JTextField(3);
    javax.swing.JButton statusButton = new javax.swing.JButton();

    javax.swing.JButton onButton = new javax.swing.JButton();
    javax.swing.JButton offButton = new javax.swing.JButton();

    javax.swing.JLabel textStateLabel = new javax.swing.JLabel();
    javax.swing.JLabel nowStateLabel = new javax.swing.JLabel();
    javax.swing.JLabel dimStatusLabel = new javax.swing.JLabel();

    javax.swing.JLabel dimTextLabel1 = new javax.swing.JLabel();
    javax.swing.JTextField dimTextField = new javax.swing.JTextField(4);
    javax.swing.JLabel dimTextLabel2 = new javax.swing.JLabel();
    javax.swing.JButton dimButton = new javax.swing.JButton();
    
    javax.swing.JLabel dimEnabledLabel = new javax.swing.JLabel();
    javax.swing.JCheckBox dimEnabledCheckBox = new javax.swing.JCheckBox();
    javax.swing.JLabel dimMinTextLabel = new javax.swing.JLabel();
    javax.swing.JTextField dimMinTextField = new javax.swing.JTextField(4);
    javax.swing.JLabel dimMaxTextLabel = new javax.swing.JLabel();
    javax.swing.JTextField dimMaxTextField = new javax.swing.JTextField(4);
    javax.swing.JLabel dimRateTextLabel = new javax.swing.JLabel();
    javax.swing.JTextField dimRateTextField = new javax.swing.JTextField(4);
    
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

        dimEnabledLabel.setText(rb.getString("LightEnableDimLabel"));
        dimEnabledCheckBox.setSelected(false);
        dimEnabledCheckBox.setVisible(true);
        dimEnabledCheckBox.setToolTipText(rb.getString("LightDimEnabledToolTip"));
        
        dimTextLabel1.setText(rb.getString("LightDimTextLabel"));
        dimTextLabel1.setVisible(true);
        dimTextField.setText(oneDigits.format(0));
        dimTextField.setVisible(true);
        dimTextLabel2.setText("%");
        dimTextField.setToolTipText(rb.getString("LightDimTextToolTip"));
        
        dimMinTextLabel.setText(rb.getString("LightMinDimLabel"));
        dimMinTextField.setText(oneDigits.format(0));
        dimMinTextField.setVisible(true);
        dimMinTextField.setToolTipText(rb.getString("LightMinDimToolTip"));
        dimMaxTextLabel.setText(rb.getString("LightMaxDimLabel"));
        dimMaxTextField.setText(oneDigits.format(0));
        dimMaxTextField.setVisible(true);
        dimMaxTextField.setToolTipText(rb.getString("LightMinDimToolTip"));
        dimRateTextLabel.setText(rb.getString("LightRateDimLabel"));
        dimRateTextField.setText(oneDigits.format(0));
        dimRateTextField.setVisible(true);
        dimRateTextField.setEnabled(false);
        dimRateTextField.setToolTipText(rb.getString("LightRateDimToolTip"));
        dimButton.setText(rb.getString("LightDimButton"));
        dimButton.setVisible(true);
        dimButton.setToolTipText(rb.getString("LightDimButtonToolTip"));
        dimButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    dimButtonActionPerformed(e);
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
        pane2.add(dimTextLabel1);
        pane2.add(dimTextField);
        pane2.add(dimTextLabel2);
        pane2.add(dimButton);
        getContentPane().add(pane2);

        pane2 = new JPanel();
        pane2.add(dimStatusLabel);
        pane2.add(dimEnabledLabel);
        pane2.add(dimEnabledCheckBox);
        getContentPane().add(pane2);

        pane2 = new JPanel();
        pane2.add(dimMinTextLabel);
        pane2.add(dimMinTextField);
        pane2.add(dimMaxTextLabel);
        pane2.add(dimMaxTextField);
        pane2.add(dimRateTextLabel);
        pane2.add(dimRateTextField);
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
				dimTextField.setText(oneDigits.format(light.getDimRequest() * 100));
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
				dimTextField.setText(oneDigits.format(light.getDimRequest() * 100));
			}
		} catch (Exception ex) {
			log.error("onButtonActionPerformed, exception: "
							+ ex.toString());
			nowStateLabel.setText("ERROR");
		}
	}

    public void dimButtonActionPerformed(java.awt.event.ActionEvent e) {
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
				light.setDimRequest(Double.parseDouble(dimTextField.getText().trim()) / 100);
			}
		} catch (Exception ex) {
			log.error("dimButtonActionPerformed, exception: "
							+ ex.toString());
			nowStateLabel.setText("ERROR");
		}
	}
    
    /**
     * handle changes for dimmable enable/disable, etc...
     */
    public void applyButtonActionPerformed(java.awt.event.ActionEvent e) {
		// load address from switchAddrTextField
    	try {
			light = InstanceManager.lightManagerInstance().provideLight(adrTextField.getText());
	
			if (light == null) {
				nowStateLabel.setText("Light " + adrTextField.getText() + " is not available");
			} else {
				if (dimEnabledCheckBox.isSelected()) {
					if (!light.isCanDim()) {
						light.setCanDim(true);
					}
				} else {
					if (light.isCanDim()) {
						light.setCanDim(false);
					}
				}
				double min = Double.parseDouble(dimMinTextField.getText());
				double max = Double.parseDouble(dimMaxTextField.getText());
				log.debug("setting min: " + min + " max: " + max);
				light.setDimMin(min);
				light.setDimMax(max);
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
			light = InstanceManager.lightManagerInstance().provideLight(adrTextField.getText());
	
			if (light == null) {
				nowStateLabel.setText("Light " + adrTextField.getText() + " is not available");
			} else {
				dimTextField.setText(oneDigits.format(light.getDimRequest() * 100));
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
    	// If the Commanded State changes, show transition state as "<inconsistent>" 
     	if (e.getPropertyName().equals("CommandedState")){
    		nowStateLabel.setText("<inconsistent>");
 		}
     	updateLightStatusFields();
//        if (e.getPropertyName().equals("KnownState")) {
//            int now = ((Integer) e.getNewValue()).intValue();
//            switch (now) {
//            case Light.OFF:
//                nowStateLabel.setText("OFF");
//                return;
//            case Light.ON:
//                nowStateLabel.setText("ON");
//                return;
//            default:
//                nowStateLabel.setText("<inconsistent>");
//                return;
//            }
//        }
     }
    
    private void updateLightStatusFields(){
    	
		int knownState = light.getState();
        switch (knownState) {
        case Light.OFF:
            nowStateLabel.setText("OFF");
            break;
        case Light.ON:
            nowStateLabel.setText("ON");
            break;
        default:
            nowStateLabel.setText("<inconsistent>");
            break;
        }
        String txt = "";
        if (light.isCanDim()) {
        	dimEnabledCheckBox.setSelected(true);
            txt = "Dim ";
            txt = txt + oneDigits.format(light.getDimRequest() * 100) + "% / " + oneDigits.format(light.getDimCurrent() * 100) + "%";
        } else {
        	dimEnabledCheckBox.setSelected(false);
        }
        dimStatusLabel.setText(txt);
        dimRateTextField.setText(oneDigits.format(light.getDimRate()));
        dimMinTextField.setText(oneDigits.format(light.getDimMin() * 100));
        dimMaxTextField.setText(oneDigits.format(light.getDimMax() * 100));
      }

    Light light = null;

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SimpleLightCtrlFrame.class.getName());

    String newState = "";
}



