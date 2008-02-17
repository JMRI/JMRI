// MonitoringPane.java

package jmri.jmrix.loconet.sdfeditor;

import java.awt.FlowLayout;

/**
 * Label which displays the contents of 
 * parameter messages.
 *
 * @author	    Bob Jacobsen   Copyright (C) 2007
 * @version	    $Revision: 1.2 $
 */
public class MonitoringLabel extends javax.swing.JLabel implements java.beans.PropertyChangeListener {

    public MonitoringLabel() {
    }

    /**
     * Listening method, diplays results
     */
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        setText(evt.getNewValue().toString());
    }
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(MonitoringLabel.class.getName());

}
