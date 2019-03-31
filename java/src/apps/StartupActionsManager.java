package apps;

import apps.gui.GuiLafPreferencesManager;
import apps.startup.StartupActionModelUtil;
import apps.startup.StartupModel;
import apps.startup.StartupModelFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ServiceLoader;
import java.util.Set;
import jmri.JmriException;
import jmri.configurexml.ConfigXmlManager;
import jmri.configurexml.XmlAdapter;
import jmri.implementation.FileLocationsPreferences;
import jmri.jmrit.logix.WarrantPreferences;
import jmri.jmrit.roster.RosterConfigManager;
import jmri.jmrit.symbolicprog.ProgrammerConfigManager;
import jmri.managers.ManagerDefaultSelector;
import jmri.profile.Profile;
import jmri.profile.ProfileUtils;
import jmri.server.web.app.WebAppManager;
import jmri.spi.PreferencesManager;
import jmri.swing.JmriJTablePersistenceManager;
import jmri.util.jdom.JDOMUtil;
import jmri.util.prefs.AbstractPreferencesManager;
import jmri.util.prefs.InitializationException;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manager for Startup Actions. Reads preferences at startup and triggers
 * actions, and is responsible for saving the preferences later.
 *
 * @author Randall Wood (C) 2015, 2016
 */
@ServiceProvider(service = PreferencesManager.class)
public class StartupActionsManager extends AbstractPreferencesManager {

    private final List<StartupModel> actions = new ArrayList<>();
    private final HashMap<Class<? extends StartupModel>, StartupModelFactory> factories = new HashMap<>();
    private boolean isDirty = false;
    private boolean restartRequired = false;
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

    /**
     * {@inheritDoc}
     * <p>
     * Loads the startup action preferences and, if all required managers have
     * initialized without exceptions, performs those actions. Startup actions
     * are only performed if {@link apps.startup.StartupModel#isValid()} is true
     * for the action. It is assumed that the action has retained an Exception
     * that can be used to explain why isValid() is false.
     */
    @Override
    public void initialize(Profile profile) throws InitializationException {
        if (!this.isInitialized(profile)) {
            boolean perform = true;
            try {
                this.requiresNoInitializedWithExceptions(profile, Bundle.getMessage("StartupActionsManager.RefusalToInitialize"));
            } catch (InitializationException ex) {
                perform = false;
            }
            try {
                Element startup;
                try {
                    startup = JDOMUtil.toJDOMElement(ProfileUtils.getAuxiliaryConfiguration(profile).getConfigurationFragment(STARTUP, NAMESPACE, true));
                } catch (NullPointerException ex) {
                    log.debug("Reading element from version 2.9.6 namespace...");
                    startup = JDOMUtil.toJDOMElement(ProfileUtils.getAuxiliaryConfiguration(profile).getConfigurationFragment(STARTUP, NAMESPACE_OLD, true));
                }
                for (Element action : startup.getChildren()) {
                    String adapter = action.getAttributeValue("class"); // NOI18N
                    String name = action.getAttributeValue("name"); // NOI18N
                    String override = StartupActionModelUtil.getDefault().getOverride(name);
                    if (override != null) {
                        action.setAttribute("name", override);
                        log.info("Overridding statup action class {} with {}", name, override);
                        this.addInitializationException(profile, new InitializationException(Bundle.getMessage(Locale.ENGLISH, "StartupActionsOverriddenClasses", name, override),
                                Bundle.getMessage(Locale.ENGLISH, "StartupActionsOverriddenClasses", name, override)));
                        name = override; // after logging difference and creating error message
                    }
                    String type = action.getAttributeValue("type"); // NOI18N
                    log.debug("Read {} {} adapter {}", type, name, adapter);
                    try {
                        log.debug("Creating {} {} adapter {}...", type, name, adapter);
                        ((XmlAdapter) Class.forName(adapter).getDeclaredConstructor().newInstance()).load(action, null); // no perNode preferences
                    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                        log.error("Unable to create {} for {}", adapter, action, ex);
                        this.addInitializationException(profile, new InitializationException(Bundle.getMessage(Locale.ENGLISH, "StartupActionsCreationError", adapter, name),
                                Bundle.getMessage("StartupActionsCreationError", adapter, name))); // NOI18N
                    } catch (Exception ex) {
                        log.error("Unable to load {} into {}", action, adapter, ex);
                        this.addInitializationException(profile, new InitializationException(Bundle.getMessage(Locale.ENGLISH, "StartupActionsLoadError", adapter, name),
                                Bundle.getMessage("StartupActionsLoadError", adapter, name))); // NOI18N
                    }
                }
            } catch (NullPointerException ex) {
                // ignore - this indicates migration has not occurred
                log.debug("No element to read");
            }
            if (perform) {
                this.actions.stream().filter((action) -> (action.isValid())).forEachOrdered((action) -> {
                    try {
                        action.performAction();
                    } catch (JmriException ex) {
                        this.addInitializationException(profile, ex);
                    }
                });
            }
            this.isDirty = false;
            this.restartRequired = false;
            this.setInitialized(profile, true);
            List<Exception> exceptions = this.getInitializationExceptions(profile);
            if (exceptions.size() == 1) {
                throw new InitializationException(exceptions.get(0));
            } else if (exceptions.size() > 1) {
                throw new InitializationException(Bundle.getMessage(Locale.ENGLISH, "StartupActionsMultipleErrors"),
                        Bundle.getMessage("StartupActionsMultipleErrors")); // NOI18N
            }
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
        requires.add(WarrantPreferences.class);
        requires.add(WebAppManager.class);
        requires.add(JmriJTablePersistenceManager.class);
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

    /**
     * Insert a {@link apps.startup.StartupModel} at the given position.
     * Triggers an {@link java.beans.IndexedPropertyChangeEvent} where the old
     * value is null and the new value is the inserted model.
     *
     * @param index The position where the model will be inserted
     * @param model The model to be inserted
     */
    public void setActions(int index, StartupModel model) {
        this.setActions(index, model, true);
    }

    private void setActions(int index, StartupModel model, boolean fireChange) {
        if (!this.actions.contains(model)) {
            this.actions.add(index, model);
            this.setRestartRequired();
            if (fireChange) {
                this.fireIndexedPropertyChange(STARTUP, index, null, model);
            }
        }
    }

    /**
     * Move a {@link apps.startup.StartupModel} from position start to position
     * end. Triggers an {@link java.beans.IndexedPropertyChangeEvent} where the
     * index is end, the old value is start and the new value is the moved
     * model.
     *
     * @param start the original position
     * @param end   the new position
     */
    public void moveAction(int start, int end) {
        StartupModel model = this.getActions(start);
        this.removeAction(model, false);
        this.setActions(end, model, false);
        this.fireIndexedPropertyChange(STARTUP, end, start, model);
    }

    public void addAction(StartupModel model) {
        this.setActions(this.actions.size(), model);
    }

    /**
     * Remove a {@link apps.startup.StartupModel}. Triggers an
     * {@link java.beans.IndexedPropertyChangeEvent} where the index is the
     * position of the removed model, the old value is the model, and the new
     * value is null.
     *
     * @param model The startup action to remove
     */
    public void removeAction(StartupModel model) {
        this.removeAction(model, true);
    }

    private void removeAction(StartupModel model, boolean fireChange) {
        int index = this.actions.indexOf(model);
        this.actions.remove(model);
        this.setRestartRequired();
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

    /**
     * Mark that a change requires a restart. As a side effect, marks this
     * manager dirty.
     */
    public void setRestartRequired() {
        this.restartRequired = true;
        this.isDirty = true;
    }

    /**
     * Indicate if a restart is required for preferences to be applied.
     *
     * @return true if a restart is required, false otherwise
     */
    public boolean isRestartRequired() {
        return this.isDirty || this.restartRequired;
    }
}
