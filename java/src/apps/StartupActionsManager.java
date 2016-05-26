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
import jmri.util.jdom.JDOMUtil;
import jmri.util.prefs.AbstractPreferencesManager;
import jmri.util.prefs.InitializationException;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.spi.PreferencesManager;

/**
 * Manager for Startup Actions. Reads preferences at startup and triggers
 * actions, and is responsible for saving the preferences later.
 *
 * @author Randall Wood (C) 2015, 2016
 */
public class StartupActionsManager extends AbstractPreferencesManager {

    private final List<StartupModel> actions = new ArrayList<>();
    private final HashMap<Class<? extends StartupModel>, StartupModelFactory> factories = new HashMap<>();
    private boolean isDirty = false;
    public final static String STARTUP = "startup"; // NOI18N
    public final static String NAMESPACE = "http://jmri.org/xml/schema/auxiliary-configuration/startup-4-3-5.xsd"; // NOI18N
    public final static String NAMESPACE_OLD = "http://jmri.org/xml/schema/auxiliary-configuration/startup-2-9-6.xsd"; // NOI18N
    private final static Logger log = LoggerFactory.getLogger(StartupActionsManager.class);

    public StartupActionsManager() {
        super();
        for (StartupModelFactory factory : ServiceLoader.load(StartupModelFactory.class)) {
            factory.initialize();
            this.factories.put(factory.getModelClass(), factory);
        }
    }

    @Override
    public void initialize(Profile profile) throws InitializationException {
        if (!this.isInitialized(profile)) {
            try {
                Element startup;
                try {
                    startup = JDOMUtil.toJDOMElement(ProfileUtils.getAuxiliaryConfiguration(profile).getConfigurationFragment(STARTUP, NAMESPACE, true));
                } catch (NullPointerException ex) {
                    log.debug("Reading element from version 2.9.6 namespace...");
                    startup = JDOMUtil.toJDOMElement(ProfileUtils.getAuxiliaryConfiguration(profile).getConfigurationFragment(STARTUP, NAMESPACE_OLD, true));
                }
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
                log.debug("No element to read");
            }
            this.isDirty = false;
            this.setInitialized(profile, true);
        }
    }

    @Override
    public Set<Class<? extends PreferencesManager>> getRequires() {
        Set<Class<? extends PreferencesManager>> requires = super.getRequires();
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
        actions.stream().forEach((action) -> {
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
        });
        try {
            ProfileUtils.getAuxiliaryConfiguration(profile).putConfigurationFragment(JDOMUtil.toW3CElement(element), true);
            this.isDirty = false;
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
            this.isDirty = true;
            this.fireIndexedPropertyChange(STARTUP, index, null, model);
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
        this.isDirty = true;
        if (fireChange) {
            this.fireIndexedPropertyChange(STARTUP, index, model, null);
        }
    }

    public HashMap<Class<? extends StartupModel>, StartupModelFactory> getFactories() {
        return new HashMap<>(this.factories);
    }

    public StartupModelFactory getFactories(Class<? extends StartupModel> model) {
        return this.factories.get(model);
    }
    
    public boolean isDirty() {
        return this.isDirty;
    }
}
