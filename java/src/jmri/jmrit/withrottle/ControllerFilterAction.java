package jmri.jmrit.withrottle;


import org.apache.log4j.Logger;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.util.JmriJFrame;

/**
 *	@author Brett Hoffman   Copyright (C) 2010
 *	@version $Revision$
 */
public class ControllerFilterAction extends AbstractAction{

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

    public void actionPerformed(ActionEvent ae) {
        JmriJFrame frame = new ControllerFilterFrame();
        try {
            frame.initComponents();
            frame.setVisible(true);
        } catch (Exception ex) {
            log.error("Could not create Route & Turnout Filter frame");
        }

    }


    static Logger log = Logger.getLogger(ControllerFilterAction.class.getName());


}
