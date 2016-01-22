package apps;

import apps.gui.GuiLafPreferencesManager;
import apps.startup.StartupModelFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import jmri.configurexml.ConfigXmlManager;
import jmri.configurexml.XmlAdapter;
import jmri.implementation.FileLocationsPreferences;
import jmri.jmrit.roster.RosterConfigManager;
import jmri.jmrit.symbolicprog.ProgrammerConfigManager;
import jmri.managers.ManagerDefaultSelector;
import jmri.profile.Profile;
import jmri.profile.ProfileUtils;
import jmri.spi.AbstractPreferencesProvider;
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
 * @author Randall Wood (C) 2015
 */
public class StartupActionsManager extends AbstractPreferencesProvider {

    private final List<StartupModel> actions = new ArrayList<>();
    private final HashMap<Class<? extends StartupModel>, StartupModelFactory> factories = new HashMap<>();
    public final static String STARTUP = "startup"; // NOI18N
    public final static String NAMESPACE = "http://jmri.org/xml/schema/auxiliary-configuration/startup-2-9-6.xsd"; // NOI18N
    private final static Logger log = LoggerFactory.getLogger(StartupActionsManager.class);

    public StartupActionsManager() {
        super();
        for (StartupModelFactory factory : ServiceLoader.load(StartupModelFactory.class)) {
            this.factories.put(factory.getModelClass(), factory);
        }
    }

    @Override
    public void initialize(Profile profile) throws InitializationException {
        if (!this.isInitialized(profile)) {
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
            this.setIsInitialized(profile, true);
        }
    }

    @Override
    public Set<Class<? extends PreferencesProvider>> getRequires() {
        Set<Class<? extends PreferencesProvider>> requires = super.getRequires();
        requires.add(ManagerDefaultSelector.class);
        requires.add(FileLocationsPreferences.class);
        requires.add(RosterConfigManager.class);
        requires.add(ProgrammerConfigManager.class);
        requires.add(GuiLafPreferencesManager.class);
        return requires;
    }

    @Override
    public synchronized void savePreferences(Profile profile) {
        Element element = new Element(STARTUP, NAMESPACE);
        for (StartupModel action : actions) {
            log.debug("model is {} ({})", action.getName(), action);
            if (action.getName() != null) {
                Element e = ConfigXmlManager.elementFromObject(action, true);
                if (e != null) {
                    element.addContent(e);
                }
            } else {
                // get an error with a stack trace if this occurs
                log.error("model does not have a name.", new Exception());
            }
        }
        try {
            ProfileUtils.getAuxiliaryConfiguration(profile).putConfigurationFragment(JDOMUtil.toW3CElement(element), true);
        } catch (JDOMException ex) {
            log.error("Unable to create create XML", ex);
        }
    }

    public StartupModel[] getActions() {
        return this.actions.toArray(new StartupModel[this.actions.size()]);
    }

    @SuppressWarnings("unchecked")
    public <T extends StartupModel> List<T> getActions(Class<T> type) {
        ArrayList<T> result = new ArrayList<>();
        this.actions.stream().filter((action) -> (type.isInstance(action))).forEach((action) -> {
            result.add((T) action);
        });
        return result;
    }

    public StartupModel getActions(int index) {
        return this.actions.get(index);
    }

    public void setActions(int index, StartupModel model) {
        if (!this.actions.contains(model)) {
            this.actions.add(index, model);
            this.propertyChangeSupport.fireIndexedPropertyChange(STARTUP, index, null, model);
        }
    }

    public void moveAction(int start, int end) {
        StartupModel model = this.getActions(start);
        this.removeAction(model, false);
        this.setActions(end, model);
    }

    public void addAction(StartupModel model) {
        this.setActions(this.actions.size(), model);
    }

    public void removeAction(StartupModel model) {
        this.removeAction(model, true);
    }

    private void removeAction(StartupModel model, boolean fireChange) {
        int index = this.actions.indexOf(model);
        this.actions.remove(model);
        if (fireChange) {
            this.propertyChangeSupport.fireIndexedPropertyChange(STARTUP, index, model, null);
        }
    }

    public HashMap<Class<? extends StartupModel>, StartupModelFactory> getFactories() {
        return this.factories;
    }

    public StartupModelFactory getFactories(Class<? extends StartupModel> model) {
        return this.factories.get(model);
    }
}
