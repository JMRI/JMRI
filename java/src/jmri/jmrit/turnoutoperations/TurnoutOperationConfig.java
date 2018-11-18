package jmri.jmrit.turnoutoperations;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JPanel;
import jmri.TurnoutOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration panel for TurnoutOperation class. Must be overridden to define
 * specific panel details for class. Must have exactly one constructor like the
 * one shown below.
 *
 * @author John Harper Copyright 2005
 */
public class TurnoutOperationConfig extends JPanel {

    TurnoutOperation myOperation;
    //boolean valid = true;

    TurnoutOperationConfig(TurnoutOperation op) {
        myOperation = op;
    }

    TurnoutOperation getOperation() {
        return myOperation;
    }

    public void endConfigure() {
        log.error("Should have been overridden!");
    }

    /**
     * Given an instance of a concrete subclass of the TurnoutOperation class,
     * looks for a corresponding ...Config class and creates an instance of it.
     * If anything goes wrong (no such class, wrong constructors, instantiation
     * error, ....) just return null
     *
     * @param op operation for which configurator is required
     * @return the configurator or null in case of an error
     */
    static public TurnoutOperationConfig getConfigPanel(TurnoutOperation op) {
        TurnoutOperationConfig config = null;
        String[] path = op.getClass().getName().split("\\.");
        String configName = "jmri.jmrit.turnoutoperations." + path[path.length - 1] + "Config";
        try {
            Class<?> configClass = Class.forName(configName);
            Constructor<?>[] constrs = configClass.getConstructors();
            if (constrs.length == 1) {
                try {
                    config = (TurnoutOperationConfig) constrs[0].newInstance(new Object[]{op});
                } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | InvocationTargetException ex) {
                    log.error("Error configuring TurnoutOperation", ex);
                }
            }
        } catch (ClassNotFoundException e) {
        }
        if (config == null) {
            //config = null;
            log.debug("could not create configurator for {} \"{}\"", op.getClass().getName(), op.getName());
        }
        return config;
    }

    private final static Logger log = LoggerFactory.getLogger(TurnoutOperationConfig.class);

}
