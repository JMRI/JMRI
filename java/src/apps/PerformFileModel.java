package apps;

import apps.startup.AbstractStartupModel;
import java.io.File;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.JmriException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A PerformFileModel object loads an xml file when the program is started.
 *
 * @author Bob Jacobsen Copyright 2003
 * @author Randall Wood (c) 2016
 * @see apps.startup.PerformFileModelFactory
 */
public class PerformFileModel extends AbstractStartupModel {

    private final static Logger log = LoggerFactory.getLogger(PerformFileModel.class);

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
            cm.load(file);
        }
    }
}
