
package jmri.jmrit.withrottle;

/**
 *  WiThrottle
 *
 *  @author Brett Hoffman   Copyright (C) 2009
 *  @author Created by Brett Hoffman on:
 *  @author 11/07/09.
 *  @version $Revision: 1.1 $
 *
 */

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class WiThrottleCreationAction extends AbstractAction{

    static UserInterface UI;

    /**
     * Create a new network server.
     * @param String name
     */
    public WiThrottleCreationAction(String name) {
        super(name);
        if (jmri.InstanceManager.throttleManagerInstance()==null) {
            setEnabled(false);
        }         
    }

    /**
     * Create a new network server.
     */
    public WiThrottleCreationAction() {
        this("Start WiThrottle");
    }

    /**
     * Start the server end of WiThrottle.
     * @param e The event causing the action.
     */
    public void actionPerformed(ActionEvent e) {
    	if (UI == null){    //  Only allow one to be created
            UI = new UserInterface();
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(WiThrottleCreationAction.class.getName());

}