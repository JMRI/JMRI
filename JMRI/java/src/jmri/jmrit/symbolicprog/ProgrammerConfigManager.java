package jmri.jmrit.symbolicprog;

import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import jmri.implementation.FileLocationsPreferences;
import jmri.jmrit.symbolicprog.tabbedframe.PaneProgFrame;
import jmri.profile.Profile;
import jmri.profile.ProfileUtils;
import jmri.spi.PreferencesManager;
import jmri.util.prefs.AbstractPreferencesManager;
import jmri.util.prefs.InitializationException;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Randall Wood (C) 2015
 */
@ServiceProvider(service = PreferencesManager.class)
public class ProgrammerConfigManager extends AbstractPreferencesManager {

    private final static Logger log = LoggerFactory.getLogger(ProgrammerConfigManager.class);
    public final static String DEFAULT_FILE = "defaultFile";
    public final static String SHOW_EMPTY_PANES = "showEmptyPanes";
    public final static String SHOW_CV_NUMBERS = "showCvNumbers";
    public final static String CAN_CACHE_DEFAULT = "canCacheDefault";
    public final static String DO_CONFIRM_READ = "doConfirmRead";
    private String defaultFile = null;
    private boolean showEmptyPanes = true;
    private boolean showCvNumbers = false;
    private boolean canCacheDefault = false;
    private boolean doConfirmRead = false;

    @Override
    public void initialize(Profile profile) throws InitializationException {
        if (!this.isInitialized(profile)) {
            Preferences preferences = ProfileUtils.getPreferences(profile, this.getClass(), true);
            if (preferences.get(DEFAULT_FILE, this.getDefaultFile()) != null) {
                this.setDefaultFile(preferences.get(DEFAULT_FILE, this.getDefaultFile()));
                ProgDefault.setDefaultProgFile(this.getDefaultFile());
            }
            
            this.setShowEmptyPanes(preferences.getBoolean(SHOW_EMPTY_PANES, this.isShowEmptyPanes()));
            PaneProgFrame.setShowEmptyPanes(this.isShowEmptyPanes());
            
            this.setShowCvNumbers(preferences.getBoolean(SHOW_CV_NUMBERS, this.isShowCvNumbers()));
            PaneProgFrame.setShowCvNumbers(this.isShowCvNumbers());
            
            this.setCanCacheDefault(preferences.getBoolean(CAN_CACHE_DEFAULT, this.isCanCacheDefault()));
            PaneProgFrame.setCanCacheDefault(this.isCanCacheDefault());
            
            this.setDoConfirmRead(preferences.getBoolean(DO_CONFIRM_READ, this.isDoConfirmRead()));
            PaneProgFrame.setDoConfirmRead(this.isDoConfirmRead());
            
            this.setInitialized(profile, true);
        }
    }

    @Override
    public Set<Class<? extends PreferencesManager>> getRequires() {
        Set<Class<? extends PreferencesManager>> requires = super.getRequires();
        requires.add(FileLocationsPreferences.class);
        return requires;
    }

    @Override
    public Set<Class<?>> getProvides() {
        Set<Class<?>> provides = super.getProvides();
        provides.stream().forEach((provide) -> {
            log.debug("ProgammerConfigManager provides {}", provide);
        });
        return provides;
    }

    @Override
    public void savePreferences(Profile profile) {
        Preferences preferences = ProfileUtils.getPreferences(profile, this.getClass(), true);
        if (this.defaultFile != null) {
            preferences.put(DEFAULT_FILE, this.defaultFile);
        } else {
            preferences.remove(DEFAULT_FILE);
        }
        preferences.putBoolean(SHOW_EMPTY_PANES, this.showEmptyPanes);
        preferences.putBoolean(SHOW_CV_NUMBERS, this.showCvNumbers);
        preferences.putBoolean(CAN_CACHE_DEFAULT, this.canCacheDefault);
        preferences.putBoolean(DO_CONFIRM_READ, this.doConfirmRead);
        try {
            preferences.sync();
        } catch (BackingStoreException ex) {
            log.error("Unable to save preferences.", ex);
        }
    }

    /**
     * @return the defaultFile
     */
    public String getDefaultFile() {
        return defaultFile;
    }

    /**
     * @param defaultFile the defaultFile to set
     */
    public void setDefaultFile(String defaultFile) {
        java.lang.String oldDefaultFile = this.defaultFile;
        this.defaultFile = defaultFile;
        firePropertyChange(DEFAULT_FILE, oldDefaultFile, defaultFile);
    }

    /**
     * @return the showEmptyPanes
     */
    public boolean isShowEmptyPanes() {
        return showEmptyPanes;
    }

    /**
     * @param showEmptyPanes the showEmptyPanes to set
     */
    public void setShowEmptyPanes(boolean showEmptyPanes) {
        boolean oldShowEmptyPanes = this.showEmptyPanes;
        this.showEmptyPanes = showEmptyPanes;
        firePropertyChange(SHOW_EMPTY_PANES, oldShowEmptyPanes, showEmptyPanes);
    }

    /**
     * @return the showCvNumbers
     */
    public boolean isShowCvNumbers() {
        return showCvNumbers;
    }

    /**
     * @param showCvNumbers the showCvNumbers to set
     */
    public void setShowCvNumbers(boolean showCvNumbers) {
        boolean oldShowCvNumbers = this.showCvNumbers;
        this.showCvNumbers = showCvNumbers;
        firePropertyChange(SHOW_CV_NUMBERS, oldShowCvNumbers, showCvNumbers);
    }

    /**
     * @return the canCacheDefault
     */
    public boolean isCanCacheDefault() {
        return canCacheDefault;
    }

    /**
     * @param canCacheDefault new value
     */
    public void setCanCacheDefault(boolean canCacheDefault) {
        boolean oldCanCacheDefault = this.canCacheDefault;
        this.canCacheDefault = canCacheDefault;
        firePropertyChange(CAN_CACHE_DEFAULT, oldCanCacheDefault, canCacheDefault);
    }

    /**
     * @return the doConfirmRead
     */
    public boolean isDoConfirmRead() {
        return doConfirmRead;
    }

    /**
     * @param doConfirmRead new value
     */
    public void setDoConfirmRead(boolean doConfirmRead) {
        boolean oldDoConfirmRead = this.doConfirmRead;
        this.doConfirmRead = doConfirmRead;
        firePropertyChange(DO_CONFIRM_READ, oldDoConfirmRead, doConfirmRead);
    }

}
