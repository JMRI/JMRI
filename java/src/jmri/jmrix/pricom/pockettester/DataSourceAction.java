// pricom.pockettester.DataSourceAction.java
package jmri.jmrix.pricom.pockettester;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JmriJFrameAction to create and register a pricom.pockettester.DataSource
 * object
 *
 * @see jmri.jmrix.pricom.pockettester.DataSource
 *
 * @author	Bob Jacobsen Copyright (C) 2002,2005
 * @version	$Revision$
 */
public class DataSourceAction extends jmri.util.JmriJFrameAction {

    /**
     *
     */
    private static final long serialVersionUID = -3504361321427171189L;

    public DataSourceAction(String s) {
        super(s);
    }

    public DataSourceAction() {
        super(""); // have to invoke a ctor that exists
        java.util.ResourceBundle rb
                = java.util.ResourceBundle.getBundle("jmri.jmrix.pricom.pockettester.TesterBundle");
        putValue(javax.swing.Action.NAME, rb.getString("ActionSource"));
    }

    /**
     * Method to be overridden to make this work. Provide a completely qualified
     * class name, must be castable to JmriJFrame
     */
    public String getName() {
        return "jmri.jmrix.pricom.pockettester.DataSource";
    }

    private final static Logger log = LoggerFactory.getLogger(DataSourceAction.class.getName());

}

/* @(#)DataSourceAction.java */
