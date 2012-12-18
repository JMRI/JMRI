// SignalMastRepeaterJFrame.java

package jmri.jmrit.beantable.signalmast;

import jmri.util.JmriJFrame;

import javax.swing.*;
import java.util.ResourceBundle;

/**
 * JFrame to create a new SignalMast
 *
 * @author	Bob Jacobsen    Copyright (C) 2009
 * @version     $Revision: 19910 $
 */

public class SignalMastRepeaterJFrame extends JmriJFrame {

    public SignalMastRepeaterJFrame() {
        super(ResourceBundle.getBundle("jmri.jmrit.beantable.signalmast.RepeaterBundle")
                .getString("TitleSignalMastRepeater"),false, true);
        
        addHelpMenu("package.jmri.jmrit.beantable.SignalMastRepeater", true);
        getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));

        add(sigMastPanel = new SignalMastRepeaterPanel());
        pack();
    }
    
    SignalMastRepeaterPanel sigMastPanel = null;
    
}


/* @(#)SignalMastRepeaterJFrame.java */
