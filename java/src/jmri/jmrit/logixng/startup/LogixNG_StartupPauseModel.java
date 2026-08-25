package jmri.jmrit.logixng.startup;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.CheckForNull;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.Module;
import jmri.util.startup.AbstractStartupModel;

/**
 * A startup action that evaluates a LogixNG Digital Module and waits until it returns true.
 *
 * @author Randall Wood     (C) 2016
 * @author Daniel Bergqvist (C) 2026
 */
public class LogixNG_StartupPauseModel extends AbstractStartupModel {

    private String _moduleName;

    public void setModuleName(String moduleName) {
        log.info("setModule: {}", _moduleName);
        _moduleName = moduleName;
    }

    @CheckForNull
    public String getModuleName() {
        log.info("getModule: {}", _moduleName);
        return _moduleName;
    }

    @Override
    public String getName() {
        if (_moduleName != null) {
            return Bundle.getMessage("LogixNG_StartupPauseModel.name", _moduleName);
        } else {
            return Bundle.getMessage("LogixNG_StartupPauseModel.name_NoModule");
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return true if we have a valid module
     */
    @Override
    public boolean isValid() {
        if (_moduleName != null) {
            Module module = InstanceManager.getDefault(ModuleManager.class).getModule(_moduleName);
            return module != null;
        } else {
            return false;
        }
    }

    @Override
    public void performAction() throws JmriException {
        // We need to ensure that all the LogixNGs are setup
        InstanceManager.getDefault(LogixNG_Manager.class).setupAllLogixNGs();

        Module module = InstanceManager.getDefault(ModuleManager.class).getModule(_moduleName);

        if (module != null) {
            Map<String, Object> parameters = new HashMap<>();
            log.info("Pausing startup actions processing until LogixNG Module \"{}\" returns true.", module.getDisplayName());
            try {
                while (true) {
                    boolean result = InstanceManager.getDefault(LogixNG_Manager.class)
                            .evaluateModule(module, parameters);
                    log.info("Waiting for LogixNG Module \"{}\" to return true. Last result: {}", module.getDisplayName(), result);
                    if (result) return;
                    Thread.sleep(100);
                }
            } catch (InterruptedException ex) {
                // warn the user that the pause was not as long as expected
                // this does not throw an error displayed to the user; should it?
                log.info("Pause in startup actions interrupted.");
            }
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LogixNG_StartupPauseModel.class);
}
