package jmri.util.startup;

import java.io.File;

import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.JmriException;

/**
 * A PerformFileModel object loads an xml file when the program is started.
 *
 * @author Bob Jacobsen Copyright 2003
 * @author Randall Wood (c) 2016
 * @see jmri.util.startup.PerformFileModelFactory
 */
public class PerformFileModel extends AbstractStartupModel {


    public String getFileName() {
        return this.getName();
    }

    public void setFileName(String n) {
        this.setName(n);
    }

    @Override
    public void performAction() throws JmriException {
        log.info("Loading file {}", this.getFileName());

        // load the file
        File file = new File(this.getFileName());
        ConfigureManager cm = InstanceManager.getNullableDefault(ConfigureManager.class);
        if (cm != null) {
            boolean load = cm.load(file);
            if (!load) {
                log.error("Could not load file {}", getFileName());
            }
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PerformFileModel.class);

}
