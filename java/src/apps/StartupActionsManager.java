package apps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import jmri.beans.Bean;
import jmri.configurexml.ConfigXmlManager;
import jmri.configurexml.XmlAdapter;
import jmri.implementation.FileLocationsPreferences;
import jmri.jmrix.ConnectionConfigManager;
import jmri.managers.ManagerDefaultSelector;
import jmri.profile.Profile;
import jmri.profile.ProfileUtils;
import jmri.spi.InitializationException;
import jmri.spi.PreferencesProvider;
import jmri.util.jdom.JDOMUtil;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manager for Startup Actions. Reads preferences at startup and triggers
 * actions, and is responsible for saving the preferences later.
 *
 * @author rhwood
 */
public class StartupActionsManager extends Bean implements PreferencesProvider {

    private final HashMap<Profile, Boolean> initialized = new HashMap<>();
    private final Set<StartupModel> models = new HashSet<>();
    public final static String STARTUP = "startup"; // NOI18N
    public final static String NAMESPACE = "http://jmri.org/xml/schema/auxiliary-configuration/startup-2-9-6.xsd"; // NOI18N
    private final static Logger log = LoggerFactory.getLogger(StartupActionsManager.class);

    @Override
    public void initialize(Profile profile) throws InitializationException {
        try {
            Element startup = JDOMUtil.toJDOMElement(ProfileUtils.getAuxiliaryConfiguration(profile).getConfigurationFragment(STARTUP, NAMESPACE, true));
            startup.getChildren().stream().forEach((perform) -> {
                String adapter = perform.getAttributeValue("class"); // NOI18N
                String name = perform.getAttributeValue("name"); // NOI18N
                String type = perform.getAttributeValue("type"); // NOI18N
                log.debug("Read {} {} adapter {}", type, name, adapter);
                try {
                    log.debug("Creating {} {} adapter {}...", type, name, adapter);
                    ((XmlAdapter) Class.forName(adapter).newInstance()).load(perform, null); // no perNode preferences
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                    log.error("Unable to create {} for {}", adapter, perform, ex);
                } catch (Exception ex) {
                    log.error("Unable to load {} into {}", perform, adapter, ex);
                }
            });
        } catch (NullPointerException ex) {
            // ignore - this indicates migration has not occured
        }
        this.initialized.put(profile, Boolean.TRUE);
    }

    @Override
    public boolean isInitialized(Profile profile) {
        return this.initialized.getOrDefault(profile, false);
    }

    @Override
    public Iterable<Class<? extends PreferencesProvider>> getRequires() {
        ArrayList<Class<? extends PreferencesProvider>> requires = new ArrayList<>();
        requires.add(ConnectionConfigManager.class);
        requires.add(ManagerDefaultSelector.class);
        requires.add(FileLocationsPreferences.class);
        return requires;
    }

    @Override
    public Iterable<Class<?>> getProvides() {
        ArrayList<Class<?>> provides = new ArrayList<>();
        provides.add(StartupActionsManager.class);
        return provides;
    }

    @Override
    public synchronized void savePreferences(Profile profile) {
        Element element = new Element(STARTUP, NAMESPACE);
        for (StartupModel model : models) {
            log.debug("model is {} ({})", model.getName(), model);
            if (model.getName() != null) {
                Element e = ConfigXmlManager.elementFromObject(model, true);
                if (e != null) {
                    element.addContent(e);
                }
            } else {
                // get an error with a stack trace if this occurs
                log.error("model cannot have a name.", new Exception());
            }
        }
        try {
            ProfileUtils.getAuxiliaryConfiguration(profile).putConfigurationFragment(JDOMUtil.toW3CElement(element), true);
        } catch (JDOMException ex) {
            log.error("Unable to create create XML", ex);
        }
    }

    public Set<StartupModel> getModels() {
        return this.models;
    }

    public void addModel(StartupModel model) {
        this.models.add(model);
    }

    public void removeModel(StartupModel model) {
        this.models.remove(model);
    }
}
