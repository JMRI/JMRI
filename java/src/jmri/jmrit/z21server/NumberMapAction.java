package jmri.jmrit.z21server;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Called from a menu to display the Turnout Number Mapping Window.
 * 
 * @author Eckart Meyer Copyright (C) 2025
 * 
 * Derived from jmri.jmrit.withrottle.ControllerFilterAction.
 */
public class NumberMapAction extends AbstractAction {

/**
 * Constructor.
 * If none of the supported managers exist, disable the action instance.
 * Called when building the menu.
 * 
 * @param name - the menu entry
 */
    @SuppressWarnings("unchecked")
    public NumberMapAction(String name) {
        super(name);
        boolean mgrFound = false;
        for (Class<?> clazz : jmri.jmrit.z21server.TurnoutNumberMapHandler.getManagerClassList()) {
            if (jmri.InstanceManager.getNullableDefault(clazz) != null) {
                mgrFound = true;
            }
        }
        if (!mgrFound) {
            setEnabled(false);
        }
    }

    public NumberMapAction() {
        this(Bundle.getMessage("MenuMenuNumberMap"));
    }

    public String getName() {
        return "jmri.jmrit.z21server.NumberMapFrame";
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        JmriJFrame frame = new NumberMapFrame();
        try {
            frame.initComponents();
            frame.setVisible(true);
        } catch (Exception ex) {
            log.error("Could not create Number Map frame");
        }

    }

    private final static Logger log = LoggerFactory.getLogger(NumberMapAction.class);

}
