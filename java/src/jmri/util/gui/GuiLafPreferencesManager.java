package jmri.util.gui;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import jmri.InstanceManagerAutoDefault;
import jmri.beans.Bean;
import jmri.profile.Profile;
import jmri.profile.ProfileUtils;
import jmri.spi.PreferencesManager;
import jmri.util.prefs.InitializationException;
import jmri.util.swing.SwingSettings;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Randall Wood (C) 2015
 */
@ServiceProvider(service = PreferencesManager.class)
public class GuiLafPreferencesManager extends Bean implements PreferencesManager, InstanceManagerAutoDefault {

    public static final String FONT_NAME = "fontName";
    public static final String FONT_SIZE = "fontSize";
    public static final String LOCALE = "locale";
    public static final String LOOK_AND_FEEL = "lookAndFeel";
    public static final String NONSTANDARD_MOUSE_EVENT = "nonstandardMouseEvent";
    /**
     * Display state in bean tables as icon.
     */
    public static final String GRAPHICTABLESTATE = "graphicTableState";
    public static final String VERTICAL_TOOLBAR = "verticalToolBar";
    public static final String SHOW_TOOL_TIP_TIME = "showToolTipDismissDelay";
    public static final String EDITOR_USE_OLD_LOC_SIZE= "editorUseOldLocSize";
    /**
     * Smallest font size a user can set the font size to other than zero
     * ({@value}). A font size of 0 indicates that the system default font size
     * will be used.
     *
     * @see apps.GuiLafConfigPane#MIN_DISPLAYED_FONT_SIZE
     */
    public static final int MIN_FONT_SIZE = 9;
    /**
     * Largest font size a user can set the font size to ({@value}).
     *
     * @see apps.GuiLafConfigPane#MAX_DISPLAYED_FONT_SIZE
     */
    public static final int MAX_FONT_SIZE = 36;
    public static final String PROP_DIRTY = "dirty";
    public static final String PROP_RESTARTREQUIRED = "restartRequired";

    // preferences with default values
    private Locale locale = Locale.getDefault();
    private Font currentFont = null;
    private Font defaultFont = null;
    private int fontSize = 0;
    private int defaultFontSize = 0;
    private boolean nonStandardMouseEvent = false;
    private boolean graphicTableState = false;
    private boolean editorUseOldLocSize = false;
    private String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();
    private int toolTipDismissDelay = ToolTipManager.sharedInstance().getDismissDelay();
    private boolean dirty = false;
    private boolean restartRequired = false;

    /*
     * Unlike most PreferencesProviders, the GUI Look & Feel preferences should
     * be per-application instead of per-profile.
     */
    private boolean initialized = false;
    private final List<InitializationException> exceptions = new ArrayList<>();
    private static final Logger log = LoggerFactory.getLogger(GuiLafPreferencesManager.class);

    @Override
    public void initialize(Profile profile) throws InitializationException {
        if (!initialized) {
            Preferences preferences = ProfileUtils.getPreferences(profile, getClass(), true);
            setLocale(Locale.forLanguageTag(preferences.get(LOCALE, getLocale().toLanguageTag())));
            setLookAndFeel(preferences.get(LOOK_AND_FEEL, getLookAndFeel()));

            setDefaultFontSize(); // before we change anything
            setFontSize(preferences.getInt(FONT_SIZE, getDefaultFontSize()));
            if (getFontSize() == 0) {
                setFontSize(getDefaultFontSize());
            }

            setFontByName(preferences.get(FONT_NAME, getDefaultFont().getFontName()));
            if (getFont() == null) {
                setFont(getDefaultFont());
            }

            setNonStandardMouseEvent(preferences.getBoolean(NONSTANDARD_MOUSE_EVENT, isNonStandardMouseEvent()));
            setGraphicTableState(preferences.getBoolean(GRAPHICTABLESTATE, isGraphicTableState()));
            setEditorUseOldLocSize(preferences.getBoolean(EDITOR_USE_OLD_LOC_SIZE, isEditorUseOldLocSize()));
            setToolTipDismissDelay(preferences.getInt(SHOW_TOOL_TIP_TIME, getToolTipDismissDelay()));

            log.debug("About to setDefault Locale");
            Locale.setDefault(getLocale());
            javax.swing.JComponent.setDefaultLocale(getLocale());
            javax.swing.JOptionPane.setDefaultLocale(getLocale());

            applyLookAndFeel();
            applyFontSize();
            SwingSettings.setNonStandardMouseEvent(isNonStandardMouseEvent());
            setDirty(false);
            setRestartRequired(false);
            initialized = true;
        }
    }

    @Override
    public boolean isInitialized(Profile profile) {
        return initialized && exceptions.isEmpty();
    }

    @Override
    public Iterable<Class<? extends PreferencesManager>> getRequires() {
        return new HashSet<>();
    }

    @Override
    public Iterable<Class<?>> getProvides() {
        Set<Class<?>> provides = new HashSet<>();
        provides.add(getClass());
        return provides;
    }

    @Override
    public void savePreferences(Profile profile) {
        Preferences preferences = ProfileUtils.getPreferences(profile, getClass(), true);
        preferences.put(LOCALE, getLocale().toLanguageTag());
        preferences.put(LOOK_AND_FEEL, getLookAndFeel());

        if (currentFont == null) {
            currentFont = getDefaultFont();
        }

        String currentFontName = currentFont.getFontName();
        if (currentFontName != null) {
            String prefFontName = preferences.get(FONT_NAME, currentFontName);
            if ((prefFontName == null) || ( ! prefFontName.equals(currentFontName))) {
                preferences.put(FONT_NAME, currentFontName);
            }
        }

        int temp = getFontSize();
        if (temp == getDefaultFontSize()) {
            temp = 0;
        }
        if (temp != preferences.getInt(FONT_SIZE, -1)) {
            preferences.putInt(FONT_SIZE, temp);
        }
        preferences.putBoolean(NONSTANDARD_MOUSE_EVENT, isNonStandardMouseEvent());
        preferences.putBoolean(GRAPHICTABLESTATE, isGraphicTableState()); // use graphic icons in bean table state column
        preferences.putBoolean(EDITOR_USE_OLD_LOC_SIZE, isEditorUseOldLocSize());
        preferences.putInt(SHOW_TOOL_TIP_TIME, getToolTipDismissDelay());
        try {
            preferences.sync();
        } catch (BackingStoreException ex) {
            log.error("Unable to save preferences.", ex);
        }
        setDirty(false);
    }

    /**
     * @return the locale
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * @param locale the locale to set
     */
    public void setLocale(Locale locale) {
        Locale oldLocale = locale;
        this.locale = locale;
        setDirty(true);
        setRestartRequired(true);
        firePropertyChange(LOCALE, oldLocale, locale);
    }

    /**
     * @return the currently selected font
     */
    public Font getFont() {
        return currentFont;
    }

    /**
     * Sets a new font
     *
     * @param newFont the new font to set
     */
    public void setFont(Font newFont) {
        Font oldFont = currentFont;
        currentFont = newFont;
        if (currentFont != oldFont) {
            firePropertyChange(FONT_NAME, oldFont, currentFont);
        }
        setDirty(true);
        setRestartRequired(true);
    }

    /**
     * Sets a new font by name
     *
     * @param newFontName the name of the new font to set
     */
    public void setFontByName(String newFontName) {
        Font oldFont = getFont();
        if (oldFont == null) {
            oldFont = getDefaultFont();
        }
        setFont(new Font(newFontName, oldFont.getStyle(), fontSize));
    }

    /**
     * @return the current {@literal Look & Feel} default font
     */
    public Font getDefaultFont() {
        if (defaultFont == null) {
            setDefaultFont();
        }
        return defaultFont;
    }

    /**
     * Called to load the current {@literal Look & Feel} default font,
     * based on looking up the "List.font"
     * <br><br>
     * The value can be can be read by calling {@link #getDefaultFont()}
     */
    public void setDefaultFont() {
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);

            if (value instanceof javax.swing.plaf.FontUIResource && key.toString().equals("List.font")) {
                Font f = UIManager.getFont(key);
                log.debug("Key: {} Font: {}", key, f.getName());
                defaultFont = f;
                return;
            }
        }
        // couldn't find the default return a reasonable font
        defaultFont = UIManager.getFont("List.font");
        if (defaultFont == null) {
            // or maybe not quite as reasonable
            defaultFont = UIManager.getFont("TextArea.font");
        }
    }

    /**
     * @return the currently selected font size
     */
    public int getFontSize() {
        return fontSize;
    }

    /**
     * Sets a new font size
     *
     * @param newFontSize the new font size to set
     */
    public void setFontSize(int newFontSize) {
        int oldFontSize = fontSize;
        fontSize = newFontSize;
        if (fontSize != 0) {
            fontSize = Integer.max(MIN_FONT_SIZE, Integer.min(MAX_FONT_SIZE, fontSize));
        }
        if (fontSize != oldFontSize) {
            firePropertyChange(FONT_SIZE, oldFontSize, fontSize);
        }
        setDirty(true);
        setRestartRequired(true);
    }

    /**
     * @return the current {@literal Look & Feel} default font size
     */
    public int getDefaultFontSize() {
        return defaultFontSize;
    }

    /**
     * Called to load the current {@literal Look & Feel} default font size,
     * based on looking up the "List.font" size
     * <br><br>
     * The value can be can be read by calling {@link #getDefaultFontSize()}
     */
    public void setDefaultFontSize() {
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);

            if (value instanceof javax.swing.plaf.FontUIResource && key.toString().equals("List.font")) {
                Font f = UIManager.getFont(key);
                log.debug("Key: {} Font: {} size: {}", key, f.getName(), f.getSize());
                defaultFontSize = f.getSize();
                return;
            }
        }
        defaultFontSize = 11;   // couldn't find the default return a reasonable font size
    }

    /**
     * Logs LAF fonts at the TRACE level.
     */
    private void logAllFonts() {
        // avoid any activity if logging at this level is disabled to avoid
        // the unnecessary overhead of getting the fonts
        if (log.isTraceEnabled()) {
            log.trace("******** LAF={}", UIManager.getLookAndFeel().getClass().getName());
            java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                Object value = UIManager.get(key);
                if (value instanceof javax.swing.plaf.FontUIResource || value instanceof java.awt.Font || key.toString().endsWith(".font")) {
                    Font f = UIManager.getFont(key);
                    log.trace("Class={}; Key: {} Font: {} size: {}", value.getClass().getName(), key, f.getName(), f.getSize());
                }
            }
        }
    }

    /**
     * Sets the time a tooltip is displayed before it goes away.
     *
     * Note that this preference takes effect immediately.
     *
     * @param time the delay in seconds.
     */
    public void setToolTipDismissDelay(int time) {
        toolTipDismissDelay = time;
        ToolTipManager.sharedInstance().setDismissDelay(time);
        setDirty(true);
    }

    /**
     *
     * @return the int
     */
    public int getToolTipDismissDelay() {
        return toolTipDismissDelay;
    }

    /**
     * @return the nonStandardMouseEvent
     */
    public boolean isNonStandardMouseEvent() {
        return nonStandardMouseEvent;
    }

    /**
     * @param nonStandardMouseEvent the nonStandardMouseEvent to set
     */
    public void setNonStandardMouseEvent(boolean nonStandardMouseEvent) {
        boolean oldNonStandardMouseEvent = nonStandardMouseEvent;
        this.nonStandardMouseEvent = nonStandardMouseEvent;
        setDirty(true);
        setRestartRequired(true);
        firePropertyChange(NONSTANDARD_MOUSE_EVENT, oldNonStandardMouseEvent, nonStandardMouseEvent);
    }

    /**
     * @return the graphicTableState
     */
    public boolean isGraphicTableState() {
        return graphicTableState;
    }

    /**
     * @param graphicTableState the graphicTableState to set
     */
    public void setGraphicTableState(boolean graphicTableState) {
        boolean oldGraphicTableState = graphicTableState;
        this.graphicTableState = graphicTableState;
        setDirty(true);
        setRestartRequired(true);
        firePropertyChange(GRAPHICTABLESTATE, oldGraphicTableState, graphicTableState);
    }

    /**
     * @return the editorUseOldLocSize value
     */
    public boolean isEditorUseOldLocSize() {
        return editorUseOldLocSize;
    }

    /**
     * @param editorUseOldLocSize the editorUseOldLocSize value to set
     */
    public void setEditorUseOldLocSize(boolean editorUseOldLocSize) {
        boolean oldEditorUseOldLocSize = editorUseOldLocSize;
        this.editorUseOldLocSize = editorUseOldLocSize;
        setDirty(true);
        setRestartRequired(false);
        firePropertyChange(EDITOR_USE_OLD_LOC_SIZE, oldEditorUseOldLocSize, editorUseOldLocSize);
    }

    /**
     * Get the name of the class implementing the preferred look and feel. Note
     * this may not be the in-use look and feel if the preferred look and feel
     * is not available on the current platform; and will be overwritten if
     * preferences are saved on a platform where the preferred look and feel is
     * not available.
     * 
     * @return the look and feel class name
     */
    public String getLookAndFeel() {
        return lookAndFeel;
    }

    /**
     * Set the name of the class implementing the preferred look and feel. Note
     * this change only takes effect after the application is restarted, because
     * Java has some issues setting the look and feel correctly on already open
     * windows.
     * 
     * @param lookAndFeel the look and feel class name
     */
    public void setLookAndFeel(String lookAndFeel) {
        String oldLookAndFeel = lookAndFeel;
        this.lookAndFeel = lookAndFeel;
        setDirty(true);
        setRestartRequired(true);
        firePropertyChange(LOOK_AND_FEEL, oldLookAndFeel, lookAndFeel);
    }

    /**
     * Apply the existing look and feel.
     */
    public void applyLookAndFeel() {
        String lafClassName = null;
        for (LookAndFeelInfo LAF : UIManager.getInstalledLookAndFeels()) {
            // accept either name or classname of look and feel
            if (LAF.getClassName().equals(lookAndFeel) || LAF.getName().equals(lookAndFeel)) {
                lafClassName = LAF.getClassName();
                break; // use first match, not last match (unlikely to be different, but you never know)
            }
        }
        log.debug("Look and feel selection \"{}\" ({})", lookAndFeel, lafClassName);
        if (lafClassName != null) {
            if (!lafClassName.equals(UIManager.getLookAndFeel().getClass().getName())) {
                log.debug("Apply look and feel \"{}\" ({})", lookAndFeel, lafClassName);
                try {
                    UIManager.setLookAndFeel(lafClassName);
                } catch (ClassNotFoundException ex) {
                    log.error("Could not find look and feel \"{}\".", lookAndFeel);
                } catch (IllegalAccessException | InstantiationException ex) {
                    log.error("Could not load look and feel \"{}\".", lookAndFeel);
                } catch (UnsupportedLookAndFeelException ex) {
                    log.error("Look and feel \"{}\" is not supported on this platform.", lookAndFeel);
                }
            } else {
                log.debug("Not updating look and feel {} matching existing look and feel", lafClassName);
            }
        }
    }

    /**
     * Applies a new calculated font size to all found fonts.
     * <br><br>
     * Calls {@link #getCalcFontSize(int) getCalcFontSize} to calculate new size
     * for each.
     */
    private void applyFontSize() {
        if (log.isTraceEnabled()) {
            logAllFonts();
        }
        if (getFontSize() != getDefaultFontSize()) {
            Enumeration<Object> keys = UIManager.getDefaults().keys();
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                Object value = UIManager.get(key);
                if (value instanceof javax.swing.plaf.FontUIResource || value instanceof java.awt.Font || key.toString().endsWith(".font")) {
                    UIManager.put(key, UIManager.getFont(key).deriveFont(((Font) value).getStyle(), getCalcFontSize(((Font) value).getSize())));
                }
            }
            if (log.isTraceEnabled()) {
                logAllFonts();
            }
        }
    }

    /**
     * Stand-alone service routine to
     * set the default Locale.
     *
     * Intended to be invoked early, as soon as a profile is
     * available, to ensure the correct language is set as
     * startup proceeds. Must be followed eventually
     * by a complete {@link #setLocale}.
     * 
     * @param profile The profile to get the locale from
     */
    public static void setLocaleMinimally(Profile profile) {
        String name = ProfileUtils.getPreferences(profile, GuiLafPreferencesManager.class, true).get("locale","en"); // "en" is default if not found
        log.debug("setLocaleMinimally found language {}, setting", name);
        Locale.setDefault(new Locale(name));
        javax.swing.JComponent.setDefaultLocale(new Locale(name));
        javax.swing.JOptionPane.setDefaultLocale(new Locale(name));
    }

    /**
     * @return a new calculated font size based on difference between default
     *         size and selected size
     *
     * @param oldSize the old font size
     */
    private int getCalcFontSize(int oldSize) {
        return oldSize + (getFontSize() - getDefaultFontSize());
    }

    /**
     * Check if preferences need to be saved.
     *
     * @return true if preferences need to be saved
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * Set dirty state.
     *
     * @param dirty true if preferences need to be saved
     */
    private void setDirty(boolean dirty) {
        boolean oldDirty = dirty;
        this.dirty = dirty;
        if (oldDirty != dirty) {
            propertyChangeSupport.firePropertyChange(PROP_DIRTY, oldDirty, dirty);
        }
    }

    /**
     * Check if application needs to restart to apply preferences.
     *
     * @return true if preferences are only applied on application start
     */
    public boolean isRestartRequired() {
        return restartRequired;
    }

    /**
     * Set restart required state.
     *
     * @param restartRequired true if application needs to restart to apply
     *                        preferences
     */
    private void setRestartRequired(boolean restartRequired) {
        boolean oldRestartRequired = restartRequired;
        this.restartRequired = restartRequired;
        if (oldRestartRequired != restartRequired) {
            propertyChangeSupport.firePropertyChange(PROP_RESTARTREQUIRED, oldRestartRequired, restartRequired);
        }
    }

    @Override
    public boolean isInitializedWithExceptions(Profile profile) {
        return initialized && !exceptions.isEmpty();
    }

    @Override
    public List<Exception> getInitializationExceptions(Profile profile) {
        return new ArrayList<>(exceptions);
    }
}
