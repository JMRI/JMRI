// RevHistoryAction.java

package jmri.jmrit.revhistory.swing;

import jmri.jmrit.revhistory.RevHistory;
import jmri.util.JmriJFrame;
import jmri.*;

import java.awt.event.*;
import javax.swing.*;

/**
 * Swing action to display the revision history
 *
 * @author	Bob Jacobsen    Copyright (C) 2009
 * @version     $Revision: 1.1 $
 */
public class RevHistoryAction extends AbstractAction {

    public RevHistoryAction(String s) { 
	    super(s);
    }

    public RevHistoryAction() { 
        this("History");
    }

    public void actionPerformed(ActionEvent e) {
        JFrame frame = new JmriJFrame(){};  // JmriJFrame to ensure fits on screen
                
        JTextArea pane = new JTextArea();
        pane.append("\n"); // add a little space at top
        pane.setEditable(false);
 
        JScrollPane  scroll = new JScrollPane(pane);
        frame.getContentPane().add(scroll);
        
        RevHistory r = InstanceManager.getDefault(RevHistory.class);
        if (r == null) {
            pane.append("<No History Found>\n");
        } else {
            pane.append(r.toString());
        }
        
        pane.append("\n"); // add a little space at bottom

        frame.pack();

        // start scrolled to top
        JScrollBar b = scroll.getVerticalScrollBar();
        b.setValue(b.getMaximum());

        // show
        frame.setVisible(true);
    }
}

/* @(#)RevHistoryAction.java */
