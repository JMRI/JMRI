// PollTableFrame.java
 
package jmri.jmrix.rps.swing.polling;

import jmri.jmrix.rps.*;
import jmri.implementation.AbstractShutDownTask;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;
import java.util.ResourceBundle;
import javax.vecmath.Point3d;
import java.io.*;

/**
 * Frame for control of RPS polling
 *
 * @author	   Bob Jacobsen   Copyright (C) 2008
 * @version   $Revision: 1.2 $
 */


public class PollTableFrame extends jmri.util.JmriJFrame  {

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.rps.swing.polling.PollingBundle");
    PollTablePane pane;
    
    public PollTableFrame() {
        super();
        setTitle(title());
    }

    protected String title() { return rb.getString("TitlePolling"); }  // product name, not translated

    public void dispose() {
        super.dispose();
    }
    
    public void initComponents() {
        // only one, so keep around on close
	    setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

        // create a table and add
        pane = new PollTablePane(this);
        getContentPane().add(pane);
        
        // add help
        addHelpMenu("package.jmri.jmrix.rps.swing.polling.PollTableFrame", true);

        // create ShutDownTask
        // no need to remember this, as don't need to remove it
        // once this frame is created
        if (jmri.InstanceManager.shutDownManagerInstance()!=null) {
            AbstractShutDownTask task = 
                    new AbstractShutDownTask("rps.PollingTableFrame"){
                        public boolean execute() {
                            checkForSave();
                            return true;
                        }
            };
            jmri.InstanceManager.shutDownManagerInstance().register(task);
        }
        // prepare for display
        pack();
    }
    
    /**
     * Check for modified when closing
     */
    public void windowClosing(java.awt.event.WindowEvent e) {
        checkForSave();
    }
    
    void checkForSave() {
        if (getModifiedFlag()) {
            int result = JOptionPane.showOptionDialog(this,
                rb.getString("WarnChanged"),
                rb.getString("WarnTitle"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null, // icon
                new String[]{rb.getString("LabelYesSave"),rb.getString("LabelNoClose")},
                rb.getString("LabelYesSave")
            );
            if (result == JOptionPane.YES_OPTION) {
                // user wants to save
                pane.setDefaults();
            }
        }
    }
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PollTableFrame.class.getName());
}
