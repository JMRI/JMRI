package jmri.jmrit.throttle;

import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.Icon;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.JmriPanel;
import jmri.util.swing.WindowInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create a new throttle.
 *
 * @author	Lionel Jeanson Copyright 2009
 */
public class LoadDefaultXmlThrottlesLayoutAction extends JmriAbstractAction {

    public LoadDefaultXmlThrottlesLayoutAction(String s, WindowInterface wi) {
        super(s, wi);
        // disable the ourselves if there is no throttle Manager
        if (jmri.InstanceManager.throttleManagerInstance() == null) {
            setEnabled(false);
        }
    }

    public LoadDefaultXmlThrottlesLayoutAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
        // disable the ourselves if there is no throttle Manager
        if (jmri.InstanceManager.throttleManagerInstance() == null) {
            setEnabled(false);
        }
    }

    /**
     * Constructor
     *
     * @param s Name for the action.
     */
    public LoadDefaultXmlThrottlesLayoutAction(String s) {
        super(s);
        // disable the ourselves if there is no throttle Manager
        if (jmri.InstanceManager.throttleManagerInstance() == null) {
            setEnabled(false);
        }
    }

    public LoadDefaultXmlThrottlesLayoutAction() {
        this("Load default throttle layout...");
    }

    /**
     * The action is performed. Create a new ThrottleFrame.
     *
     * @param e The event causing the action.
     */
    public void actionPerformed(ActionEvent e) {
        // load throttle preference 
        LoadXmlThrottlesLayoutAction lxta = new LoadXmlThrottlesLayoutAction();
        try {
            if (lxta.loadThrottlesLayout(new File(ThrottleFrame.getDefaultThrottleFilename()))) {
                return;
            }
        } catch (java.io.IOException ex) {
            log.error("No default throttle layout, creating an empty throttle window");
        }
        // need to create a new one
        ThrottleFrame tf = ThrottleFrameManager.instance().createThrottleFrame();
        tf.toFront();
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(ThrottleCreationAction.class.getName());

    @Override
    public JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }

}
