package jmri.jmrit.withrottle;


import jmri.util.JmriJFrameAction;

/**
 *	@author Brett Hoffman   Copyright (C) 2010
 *	@version $Revision: 1.1 $
 */
public class ControllerFilterAction extends JmriJFrameAction{

    public ControllerFilterAction(String name) {
        super(name);
        if ((jmri.InstanceManager.turnoutManagerInstance()==null) && (jmri.InstanceManager.routeManagerInstance()==null)) {
            setEnabled(false);
        }
    }

    public ControllerFilterAction() {
        this("Filter Controls");
    }
    
    public String getName(){
        return "jmri.jmrit.withrottle.ControllerFilterFrame";
    }


    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ControllerFilterAction.class.getName());

}