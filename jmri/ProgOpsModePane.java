// ProgOpsModePane.java

package jmri;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import jmri.Programmer;
import jmri.ProgListener;

/**
 * Provide a JPanel to configure the ops programming mode.
 * <P>
 * Note that you should call the dispose() method when you're really done, so that
 * a ProgModePane object can disconnect its listeners.
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Revision: 1.1 $
 */
public class ProgOpsModePane extends javax.swing.JPanel implements java.beans.PropertyChangeListener {

    // GUI member declarations
    
    javax.swing.ButtonGroup modeGroup 		= new javax.swing.ButtonGroup();
    javax.swing.JRadioButton opsByteButton  	= new javax.swing.JRadioButton();
    
    /*
     * direction is BoxLayout.X_AXIS or BoxLayout.Y_AXIS
     */
    public ProgOpsModePane(int direction) {
        
        // configure items for GUI
        opsByteButton.setText("Ops Byte Mode");
        modeGroup.add(opsByteButton);
        
        // if a programmer is available, disable buttons for unavailable modes
        if (InstanceManager.programmerManagerInstance()!=null) {
            ProgrammerManager p = InstanceManager.programmerManagerInstance();
            if (!p.isOpsModePossible()) opsByteButton.setEnabled(false);
        } else {
            log.warn("No programmer available, so modes not set");
        }
        
        // add listeners to buttons
        opsByteButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
				// get mode, and tell programmer
                    connect();
                    if (connected) setProgrammerMode(getMode());
                }
            });
        
        // connect for updates
        connect();
        
        // general GUI config
        setLayout(new BoxLayout(this, direction));
        
        // install items in GUI
        add(opsByteButton);
    }
    
    public int getMode() {
        if (opsByteButton.isSelected())
            return jmri.Programmer.OPSBYTEMODE;
        else
            return 0;
    }
    
    protected void setMode(int mode) {
        switch (mode) {
        case jmri.Programmer.OPSBYTEMODE:
            opsByteButton.setSelected(true);
            break;
        default:
            log.warn("propertyChange without valid mode value");
            break;
        }
    }
    
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        // mode changed in programmer, change GUI here if needed
        if (e.getPropertyName() == "Mode") {
            int mode = ((Integer)e.getNewValue()).intValue();
            setMode(mode);
        } else log.warn("propertyChange with unexpected propertyName: "+e.getPropertyName());
    }
    
    // connect to the Programmer interface
    boolean connected = false;
    
    private void connect() {
        if (!connected) {
            if (InstanceManager.programmerManagerInstance() != null
                && InstanceManager.programmerManagerInstance().getServiceModeProgrammer() != null) {
                InstanceManager.programmerManagerInstance()
                    .getServiceModeProgrammer().addPropertyChangeListener(this);
                connected = true;
                log.debug("Connecting to programmer");
            } else {
                log.debug("No programmer present to connect");
            }
        }
    }
    
    // set the programmer to the current mode
    private void setProgrammerMode(int mode) {
        log.debug("Setting programmer to mode "+mode);
        if (InstanceManager.programmerManagerInstance() != null
            && InstanceManager.programmerManagerInstance().getServiceModeProgrammer() != null)
            InstanceManager.programmerManagerInstance().getServiceModeProgrammer().setMode(mode);
    }

    // no longer needed, disconnect if still connected
    public void dispose() {
        if (connected) {
            if (InstanceManager.programmerManagerInstance() != null
                && InstanceManager.programmerManagerInstance().getServiceModeProgrammer() != null)
                InstanceManager.programmerManagerInstance().getServiceModeProgrammer().removePropertyChangeListener(this);
            connected = false;
        }
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ProgOpsModePane.class.getName());

}
