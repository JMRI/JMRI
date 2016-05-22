//PowerPane.java

package jmri.jmrix.sprog.swing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.JmriException;
import jmri.PowerManager;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import java.util.ResourceBundle;
import java.util.List;
import jmri.jmrix.sprog.*;

/**
 * Pane for power control
 * @author	    Bob Jacobsen   Copyright (C) 2001
 * @version	    $Revision$
 */
public class PowerPane extends javax.swing.JPanel implements java.beans.PropertyChangeListener {

    // GUI member declarations
    static ResourceBundle res = ResourceBundle.getBundle("jmri.jmrit.powerpanel.PowerPanelBundle");
    JLabel onOffStatus 	= new JLabel(res.getString("LabelUnknown"));
    JButton onButton 	= new JButton(res.getString("ButtonOn"));
    JButton offButton 	= new JButton(res.getString("ButtonOff"));

    public PowerPane() {
        // add listeners to buttons
        onButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    onButtonPushed();
                }
            });
        offButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    offButtonPushed();
                }
            });

        // general GUI config
        setLayout(new GridLayout(2,2));

        // install items in GUI
        add(new JLabel(res.getString("LabelLayoutPower")));
        add(onOffStatus);
        add(onButton);
        add(offButton);

	// Check to see if the Power Manger has a current status
        if(mgrOK()) {
            if (p.getPower()==PowerManager.ON) onOffStatus.setText(res.getString("StatusOn"));
            else if (p.getPower()==PowerManager.OFF) onOffStatus.setText(res.getString("StatusOff"));
            else if (p.getPower()==PowerManager.UNKNOWN) onOffStatus.setText(res.getString("StatusUnknown"));
            else {
                onOffStatus.setText(res.getString("StatusUnknown"));
                log.error("Unexpected state value: +"+p.getPower());
            }
        }
    }

    private boolean mgrOK() {
        if (p==null) {
            List<Object> connList = jmri.InstanceManager.getList(jmri.jmrix.SystemConnectionMemo.class);
            if (connList == null) return false; // nothing to do, no connections are configured
            for (int x = 0; x<connList.size(); x++) {
                jmri.jmrix.SystemConnectionMemo memo = (jmri.jmrix.SystemConnectionMemo)connList.get(x);
                if(memo instanceof SprogSystemConnectionMemo)
                    p = ((SprogSystemConnectionMemo)memo).getPowerManager();
            }
            if (p == null) {
                log.error("No power manager instance found, panel not active");
                return false;
            }
            else p.addPropertyChangeListener(this);
        }
        return true;
    }

    public void onButtonPushed() {
        if (mgrOK())
            try {
                p.setPower(PowerManager.ON);
            }
            catch (JmriException e) {
                log.error("Exception trying to turn power on " +e);
            }
    }

    public void offButtonPushed() {
        if (mgrOK())
            try {
                p.setPower(PowerManager.OFF);
            }
            catch (JmriException e) {
                log.error("Exception trying to turn power off " +e);
            }
    }

    public void propertyChange(java.beans.PropertyChangeEvent ev) {
        if (p.getPower()==PowerManager.ON) onOffStatus.setText(res.getString("StatusOn"));
        else if (p.getPower()==PowerManager.OFF) onOffStatus.setText(res.getString("StatusOff"));
        else if (p.getPower()==PowerManager.UNKNOWN) onOffStatus.setText(res.getString("StatusUnknown"));
        else {
            onOffStatus.setText(res.getString("StatusUnknown"));
            log.error("Unexpected state value: +"+p.getPower());
        }
    }

    public void dispose() {
        if (p!=null) p.removePropertyChangeListener(this);
    }

    SprogPowerManager p = null;
    static Logger log = LoggerFactory.getLogger(PowerPane.class.getName());

}
