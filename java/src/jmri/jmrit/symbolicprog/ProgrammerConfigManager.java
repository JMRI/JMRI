package jmri.jmrit.symbolicprog;

import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import jmri.implementation.FileLocationsPreferences;
import jmri.jmrit.symbolicprog.tabbedframe.PaneProgFrame;
import jmri.profile.Profile;
import jmri.profile.ProfileUtils;
import jmri.util.prefs.AbstractPreferencesProvider;
import jmri.util.prefs.InitializationException;
import jmri.spi.PreferencesProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Randall Wood (C) 2015
 */
public class ProgrammerConfigManager extends AbstractPreferencesProvider {

    private final static Logger log = LoggerFactory.getLogger(ProgrammerConfigManager.class);
    public final static String DEFAULT_FILE = "defaultFile";
    public final static String SHOW_EMPTY_PANES = "showEmptyPanes";
    public final static String SHOW_CV_NUMBERS = "showCvNumbers";
    private String defaultFile = null;
    private boolean showEmptyPanes = true;
    private boolean showCvNumbers = false;

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
            this.setIsInitialized(profile, true);
        }
    }

    @Override
    public Set<Class<? extends PreferencesProvider>> getRequires() {
        Set<Class<? extends PreferencesProvider>> requires = super.getRequires();
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

}
