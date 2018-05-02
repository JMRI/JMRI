package jmri.jmrit.withrottle;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import javax.swing.Icon;
import jmri.InstanceManager;
import jmri.ThrottleManager;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;

/**
 * Start, and create if needed, the WiThrottle server.
 *
 * @author Brett Hoffman Copyright (C) 2009
 *
 */
public class WiThrottleCreationAction extends JmriAbstractAction {

    public WiThrottleCreationAction(String s, WindowInterface wi) {
        super(s, wi);
    }

    public WiThrottleCreationAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
    }

    /**
     * Create a new network server.
     *
     * @param name Labels frame in GUI
     */
    public WiThrottleCreationAction(String name) {
        super(name);
        if (InstanceManager.getNullableDefault(ThrottleManager.class) == null) {
            super.setEnabled(false);
        }
    }

    /**
     * Create a new network server.
     */
    public WiThrottleCreationAction() {
        this(Bundle.getMessage("MenuStartWiThrottleServer"));
    }

    /**
     * Start the server end of WiThrottle.
     *
     * @param e The event causing the action.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        InstanceManager.getOptionalDefault(DeviceManager.class).orElseGet(() -> {
            return InstanceManager.setDefault(DeviceManager.class, new FacelessServer());
        });
        // ensure the GUI is visible if we are not in headless mode.
        if (!GraphicsEnvironment.isHeadless()) {
           UserInterface ui = InstanceManager.getOptionalDefault(UserInterface.class).orElseGet(() -> {
            return InstanceManager.setDefault(UserInterface.class, new UserInterface());
           });
           ui.setVisible(true);
        }
    }

    // never invoked, because we overrode actionPerformed above
    @Override
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }

}
