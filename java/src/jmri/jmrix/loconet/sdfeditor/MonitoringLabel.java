// MonitoringPane.java
package jmri.jmrix.loconet.sdfeditor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Label which displays the contents of parameter messages.
 *
 * @author	Bob Jacobsen Copyright (C) 2007
 * @version	$Revision$
 */
public class MonitoringLabel extends javax.swing.JTextArea implements java.beans.PropertyChangeListener {

    /**
     *
     */
    private static final long serialVersionUID = 266331403840457618L;

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

    private final static Logger log = LoggerFactory.getLogger(MonitoringLabel.class.getName());

}
