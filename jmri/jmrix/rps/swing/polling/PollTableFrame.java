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
 * @version   $Revision: 1.3 $
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

        // check at shutdown
        setShutDownTask();
        
        // prepare for display
        pack();
    }
        
    protected void storeValues() {
        pane.setDefaults();
        setModifiedFlag(false);
    }
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PollTableFrame.class.getName());
}
