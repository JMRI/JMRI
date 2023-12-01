package jmri.util.plugin;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import jmri.JmriException;
import jmri.util.startup.AbstractStartupModel;

/**
 * A LoadPluginModel object loads a plugin when the program is started.
 *
 * @author Bob Jacobsen Copyright 2003
 * @author Randall Wood (c) 2016
 * @author Daniel Bergqvist (C) 2023
 * @see jmri.util.plugin.LoadPluginModelFactory
 */
public class LoadPluginModel extends AbstractStartupModel {

    public String getFileName() {
        return this.getName();
    }

    public void setFileName(String n) {
        this.setName(n);
    }

    @Override
    public void performAction() throws JmriException {
        log.info("Load plugin {}", this.getFileName());
        try {
            PluginLoader.loadJarFile(this.getFileName());
        } catch (IOException | ClassNotFoundException | IllegalAccessException
                | InstantiationException | NoSuchMethodException
                | InvocationTargetException ex) {
            throw new JmriException(Bundle.getMessage("CannotLoadPlugin",
                    this.getFileName(), ex.getLocalizedMessage()));
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoadPluginModel.class);
}
