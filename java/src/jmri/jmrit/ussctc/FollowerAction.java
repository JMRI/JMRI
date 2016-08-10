package jmri.jmrit.ussctc;

/**
 * JmriJFrameAction to create and register a FollowerFrame object
 *
 * @author	Bob Jacobsen Copyright (C) 2003, 2007
 */
public class FollowerAction extends jmri.util.JmriJFrameAction {

    public FollowerAction(String s) {
        super(s);

        // disable ourself if there is no route manager object available
        if (jmri.InstanceManager.getOptionalDefault(jmri.RouteManager.class) == null) {
            setEnabled(false);
        }
    }

    /**
     * Method to be overridden to make this work. Provide a completely qualified
     * class name, must be castable to JmriJFrame
     */
    public String getName() {
        return "jmri.jmrit.ussctc.FollowerFrame";
    }

}
