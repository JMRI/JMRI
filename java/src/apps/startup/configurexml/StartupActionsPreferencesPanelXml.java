package apps.startup.configurexml;

import apps.StartupActionsManager;
import apps.StartupModel;
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
    public void load(Element e, Object o) throws Exception {
        log.error("Unexpected call of load(Element, Object)");
    }

    /**
     * Arrange for all {@link apps.StartupModel} objects to be stored.
     *
     * @param o Object to store, of type
     *          {@link apps.startup.StartupActionsPreferencesPanel}
     * @return null
     */
    @Override
    public Element store(Object o) {
        for (StartupModel model : InstanceManager.getDefault(StartupActionsManager.class).getActions()) {
            InstanceManager.configureManagerInstance().registerPref(model);
        }
        return null;
    }
}
