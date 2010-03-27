package jmri.jmrit.withrottle;

import java.util.ArrayList;

/**
 * Abstract for controllers that want to recieve or send communications to a
 * connected wi-fi device.
 *
 *
 *	@author Brett Hoffman   Copyright (C) 2010
 *	@version $Revision: 1.1 $
 */
abstract public class AbstractController {

    ArrayList<ControllerInterface> listeners = null;

    boolean isValid = false;

    /**
     * isValid is used to indicate if the Controller is created.
     * If false, we can null the controller and reduce overhead.
     * @return isValid
     */
    abstract boolean verifyCreation();

    abstract void handleMessage(String message);

    public void addControllerListener(ControllerInterface listener){
        if (listeners == null)
                listeners = new ArrayList<ControllerInterface>(1);
        if (!listeners.contains(listener))
                listeners.add(listener);
    }

    public void removeControllerListener(ControllerInterface listener){
        if (listeners == null)
                return;
        if (listeners.contains(listener))
                listeners.remove(listener);
    }

}
