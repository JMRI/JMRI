/** 
 * PowerPane.java
 *
 * Description:		Pane for power control
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			
 */


package jmri.jmrit.powerpanel;

import jmri.InstanceManager;
import jmri.PowerManager;
import jmri.JmriException;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class PowerPane extends javax.swing.JPanel {

	// define GUI elements
	JLabel onStatus = new JLabel();
	JButton onButton = new JButton("on");
	JButton offButton = new JButton("off");
	
	public PowerPane() {
		p = InstanceManager.powerManagerInstance();
	}

	public void onButtonPushed() {
		try {
			p.setPowerOn();
			}
		catch (JmriException e) {
			log.error("Exception trying to turn power on " +e);
			}
	}

	public void offButtonPushed() {
		try {
			p.setPowerOff();
			}
		catch (JmriException e) {
			log.error("Exception trying to turn power off " +e);
			}
	}
	
	public String shownOnOffState() {
		return onStatus.getText();
	}

	PowerManager p = null;
	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PowerPane.class.getName());

}
