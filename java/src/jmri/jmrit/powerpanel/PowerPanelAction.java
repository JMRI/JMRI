package jmri.jmrit.powerpanel;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Swing action to create and register a PowerPanelFrame object.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2010
 */
@API(status = MAINTAINED)
public class PowerPanelAction extends jmri.util.swing.JmriNamedPaneAction {

    public PowerPanelAction(String s, jmri.util.swing.WindowInterface wi) {
        super(s, wi, "jmri.jmrit.powerpanel.PowerPane");
        checkManager();
    }

    public PowerPanelAction(String s, javax.swing.Icon i, jmri.util.swing.WindowInterface wi) {
        super(s, i, wi, "jmri.jmrit.powerpanel.PowerPane");
        checkManager();
    }

    public PowerPanelAction(String s) {
        super(s, "jmri.jmrit.powerpanel.PowerPane");
        checkManager();
    }

    public PowerPanelAction() {
        this(Bundle.getMessage("TitlePowerPanel"));
    }

    void checkManager() {
        // disable ourself if there is no power Manager
        if (jmri.InstanceManager.getNullableDefault(jmri.PowerManager.class) == null) {
            setEnabled(false);
        }
    }

}
