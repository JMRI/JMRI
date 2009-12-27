// AddSignalMastJFrame.java

package jmri.jmrit.beantable.sensor;

import jmri.*;
import jmri.util.JmriJFrame;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import java.util.ResourceBundle;

/**
 * JFrame to create a new SignalMast
 *
 * @author	Bob Jacobsen    Copyright (C) 2009
 * @version     $Revision: 1.1 $
 */

public class AddSignalMastJFrame extends JmriJFrame {

    public AddSignalMastJFrame() {
        super(ResourceBundle.getBundle("jmri.jmrit.beantable.BeanTableBundle")
                .getString("TitleAddSignalMast"));
        
        addHelpMenu("package.jmri.jmrit.beantable.SignalMastAddEdit", true);
        getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));

        add(new AddSignalMastPanel());
        pack();
    }
    
}


/* @(#)AddSignalMastJFrame.java */
