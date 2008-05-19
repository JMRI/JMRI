// PollTableFrame.java
 
package jmri.jmrix.rps.swing.polling;

import jmri.jmrix.rps.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;
import javax.vecmath.Point3d;
import java.io.*;

/**
 * Frame for control of RPS polling
 *
 * @author	   Bob Jacobsen   Copyright (C) 2008
 * @version   $Revision: 1.1 $
 */


public class PollTableFrame extends jmri.util.JmriJFrame  {

    public PollTableFrame() {
        super();
        setTitle(title());
    }

    protected String title() { return "RPS Polling Control"; }  // product name, not translated

    public void dispose() {
        super.dispose();
    }
    
    public void initComponents() {
        // create a table and add
        getContentPane().add(new PollTablePane());
        
        // add help
        addHelpMenu("package.jmri.jmrix.rps.swing.polling.PollTableFrame", true);

        // prepare for display
        pack();
    }
            
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PollTableFrame.class.getName());
}
