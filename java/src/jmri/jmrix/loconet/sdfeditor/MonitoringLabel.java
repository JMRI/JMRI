// MonitoringPane.java

package jmri.jmrix.loconet.sdfeditor;


/**
 * Label which displays the contents of 
 * parameter messages.
 *
 * @author	    Bob Jacobsen   Copyright (C) 2007
 * @version	    $Revision$
 */
public class MonitoringLabel extends javax.swing.JTextArea implements java.beans.PropertyChangeListener {

    public MonitoringLabel() {
        super();
    }
    public MonitoringLabel(int row, int col) {
        super(row, col);
    }

    /**
     * Listening method, diplays results
     */
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        setText(evt.getNewValue().toString());
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MonitoringLabel.class.getName());

}
