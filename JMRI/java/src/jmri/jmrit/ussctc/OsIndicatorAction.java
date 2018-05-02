package jmri.jmrit.ussctc;

/**
 * Swing action to create and register a OsIndicatorFrame object
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2007
 */
public class OsIndicatorAction extends jmri.util.JmriJFrameAction {

    public OsIndicatorAction(String s) {
        super(s);

        // disable ourself if there is no route manager object available
        if (jmri.InstanceManager.getNullableDefault(jmri.RouteManager.class) == null) {
            setEnabled(false);
        }
    }

    /**
     * Method to be overridden to make this work. Provide a completely qualified
     * class name, must be castable to JmriJFrame
     */
    @Override
    public String getName() {
        return "jmri.jmrit.ussctc.OsIndicatorFrame";
    }

}
