package jmri.jmrit.throttle;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;

/**
 * Create a new throttle.
 *
 *  @author		Lionel Jeanson      Copyright 2009
 *  @version $Revision$
 */
public class LoadDefaultXmlThrottlesLayoutAction extends AbstractAction {

    /**
     * Constructor
     * @param s Name for the action.
     */
    public LoadDefaultXmlThrottlesLayoutAction(String s) {
        super(s);
    // disable the ourselves if there is no throttle Manager
        if (jmri.InstanceManager.throttleManagerInstance()==null) {
            setEnabled(false);
        }         
    }

    public LoadDefaultXmlThrottlesLayoutAction() {
        this("Load default throttle layout...");
    }

    /**
     * The action is performed. Create a new ThrottleFrame.
     * @param e The event causing the action.
     */
    public void actionPerformed(ActionEvent e) {
    	// load throttle preference 
    	LoadXmlThrottlesLayoutAction lxta = new LoadXmlThrottlesLayoutAction();
    	try {
            if (lxta.loadThrottlesLayout(new File(ThrottleFrame.getDefaultThrottleFilename())))
                return;
        } catch (java.io.IOException ex) { 
        	log.error("No default throttle layout, creating an empty throttle window");
        }
		// need to create a new one
    	ThrottleFrame tf = ThrottleFrameManager.instance().createThrottleFrame();
		tf.toFront();
    }

    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ThrottleCreationAction.class.getName());

}
