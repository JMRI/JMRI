package jmri.jmrit.withrottle;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract for controllers that want to receive or send communications to a
 * connected wi-fi device.
 *
 *
 * @author Brett Hoffman Copyright (C) 2010
 */
abstract public class AbstractController {

    ArrayList<ControllerInterface> listeners = null;
    List<String> sysNameList = null;

    boolean isValid = false;
    boolean canBuildList = true;

    /**
     * isValid is used to indicate if the Controller is created. If false, we
     * can null the controller and reduce overhead.
     *
     * @return isValid
     */
    abstract boolean verifyCreation();

    /**
     * Break down a message and use it.
     * @param message message for controller to parse and take action
     * @param deviceServer DeviceServer that sent this message, used to send response messages to proper client
     *
     */
    abstract void handleMessage(String message, DeviceServer deviceServer);

    /**
     * Register as listener of NamedBeans to be updated of changes.
     */
    abstract void register();

    /**
     * Deregister as listener of NamedBeans
     */
    abstract void deregister();

    /**
     * Build list only if there are no controller listeners. This way the list
     * is not changed while in use. This should only be called by a subclass of
     * jmri.Manager *Manager can implement specifics in register().
     *
     */
    @SuppressWarnings({"unchecked", "deprecation"}) // The systemNameList assignment is List<E extends Namedbean> to List<NamedBean>
                                    // Make this class generic on <E extends NamedBean> (and manager) to fix this.
                                    // deprecation needs careful unwinding for Set operations
    public void buildList(jmri.Manager manager) {
        if (sysNameList == null) {
            sysNameList = manager.getSystemNameList();
            filterList();   //  To remove unwanted objects
            register();
            canBuildList = false;
        }

    }

    public void filterList() {
        //  Override to filter by wifiControlled field of turnout or route object.
    }

    /**
     * If no listeners, clear sysNameList pointer and allow list to be re-built
     *Manager can implement specifics in deregister().
     */
    public void checkCanBuildList() {
        if (listeners.isEmpty()) {
            if (sysNameList != null) {
                deregister();
                sysNameList = null;
            }
            canBuildList = true;
        }
    }

    /**
     * Add a listener to handle: listener.sendPacketToDevice(message);
     * @param listener listener to add to listeners list
     */
    public void addControllerListener(ControllerInterface listener) {
        if (listeners == null) {
            listeners = new ArrayList<>(1);
        }
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeControllerListener(ControllerInterface listener) {
        if (listeners == null) {
            return;
        }
        if (listeners.contains(listener)) {
            listeners.remove(listener);
        }
        checkCanBuildList();
    }

}
