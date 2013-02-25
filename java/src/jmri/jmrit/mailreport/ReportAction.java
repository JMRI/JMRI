// ReportAction.java

package jmri.jmrit.mailreport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.event.*;
import javax.swing.AbstractAction;

/**
 * Swing action to create and register a ReportFrame object
 *
 * @author	Bob Jacobsen    Copyright (C) 2009
 * @version     $Revision$
 */
public class ReportAction extends AbstractAction {

    public ReportAction(String s) { 
	    super(s);
    }

    public ReportAction() { 
        this(java.util.ResourceBundle.getBundle("jmri.jmrit.mailreport.ReportBundle").getString("Name"));
    }

    public void actionPerformed(ActionEvent e) {
        ReportFrame f = new ReportFrame();
        try {
            f.initComponents();
        }
        catch (Exception ex) {
            log.error("Exception in startup", ex);
        }
        f.setVisible(true);
    }
    static Logger log = LoggerFactory.getLogger(ReportFrame.class.getName());
}

/* @(#)ReportAction.java */
