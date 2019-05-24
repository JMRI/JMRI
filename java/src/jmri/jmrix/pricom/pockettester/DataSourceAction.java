package jmri.jmrix.pricom.pockettester;


/**
 * JmriJFrameAction to create and register a pricom.pockettester.DataSource
 * object
 *
 * @see jmri.jmrix.pricom.pockettester.DataSource
 *
 * @author	Bob Jacobsen Copyright (C) 2002,2005
 */
public class DataSourceAction extends jmri.util.JmriJFrameAction {

    public DataSourceAction(String s) {
        super(s);
    }

    public DataSourceAction() {
        super(""); // have to invoke a ctor that exists
        putValue(javax.swing.Action.NAME, Bundle.getMessage("ActionSource"));
    }

    /**
     * Method to be overridden to make this work. Provide a completely qualified
     * class name, must be castable to JmriJFrame
     */
    @Override
    public String getName() {
        return "jmri.jmrix.pricom.pockettester.DataSource";
    }

}
