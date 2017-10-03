package apps.startup.configurexml;

import apps.StartupActionsManager;
import apps.startup.StartupModel;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.configurexml.AbstractXmlAdapter;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Randall Wood (C) 2016
 */
public class StartupActionsPreferencesPanelXml extends AbstractXmlAdapter {

    private final static Logger log = LoggerFactory.getLogger(StartupActionsPreferencesPanelXml.class);

    @Override
    public void load(Element e, Object o) {
        log.error("Unexpected call of load(Element, Object)");
    }

    /**
     * Arrange for all {@link apps.startup.StartupModel} objects to be stored.
     *
     * @param o Object to store, of type
     *          {@link apps.startup.StartupActionsPreferencesPanel}
     * @return null
     */
    @Override
    public Element store(Object o) {
        ConfigureManager cm = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        if (cm == null) {
            log.error("Failed to get default configure manager, can not store.");
        } else {
            for (StartupModel model : InstanceManager.getDefault(StartupActionsManager.class).getActions()) {
                cm.registerPref(model);
            }
        }
        return null;
    }
}
