// ReportFrame.java

package jmri.jmrit.mailreport;

import jmri.*;
import jmri.util.*;
import java.awt.*;

import javax.swing.*;

/**
 * Frame for sending a problem report
 * 
 * @author			Bob Jacobsen   Copyright (C) 2009
 * @version			$Revision: 1.1 $
 */
public class ReportFrame extends jmri.util.JmriJFrame {

    public ReportFrame() {
        super();
    }

    public void initComponents() throws Exception {

        setTitle(java.util.ResourceBundle.getBundle("jmri.jmrit.mailreport.ReportBundle").getString("Title"));
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        getContentPane().add(new ReportPanel());

        addHelpMenu("package.jmri.jmrit.mailreport.Report", true);
        
        pack();
    }
}
