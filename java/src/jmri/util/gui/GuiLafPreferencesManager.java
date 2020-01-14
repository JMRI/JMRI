package jmri.util.gui;

import java.awt.Font;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JComponent;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.FontUIResource;

import jmri.InstanceManagerAutoDefault;
import jmri.beans.Bean;
import jmri.profile.Profile;
import jmri.spi.PreferencesManager;
import jmri.util.prefs.InitializationException;
import jmri.util.prefs.JmriPreferencesProvider;
import jmri.util.swing.SwingSettings;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Randall Wood (C) 2015, 2019, 2020
 */
@ServiceProvider(service = PreferencesManager.class)
public class GuiLafPreferencesManager extends Bean implements PreferencesManager, InstanceManagerAutoDefault {

    public static final String FONT_NAME = "fontName";
    public static final String FONT_SIZE = "fontSize";
    public static final String LOCALE = "locale";
    public static final String LOOK_AND_FEEL = "lookAndFeel";
    public static final String NONSTANDARD_MOUSE_EVENT = "nonstandardMouseEvent";
    public static final String GRAPHIC_TABLE_STATE = "graphicTableState";
    /**
     * @deprecated since 4.19.3; use {@link #GRAPHIC_TABLE_STATE} instead
     */
    @Deprecated
    public static final String GRAPHICTABLESTATE = GRAPHIC_TABLE_STATE;
    /**
     * @deprecated since 4.19.3 without replacement
     */
    @Deprecated
    public static final String VERTICAL_TOOLBAR = "verticalToolBar";
    public static final String SHOW_TOOL_TIP_TIME = "showToolTipDismissDelay";
    public static final String EDITOR_USE_OLD_LOC_SIZE = "editorUseOldLocSize";
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
    private Locale currentLocale = Locale.getDefault();
    private Font currentFont = null;
    private int fontSize = 0;
    private boolean nonStandardMouseEvent = false;
    private boolean graphicTableState = false;
    private boolean editorUseOldLocSize = false;
    private String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();
    private int toolTipDismissDelay = ToolTipManager.sharedInstance().getDismissDelay();
    private boolean dirty = false;
    private boolean restartRequired = false;

    /*
     * Unlike most PreferencesProviders, the GUI Look & Feel preferences should
     * be per-application instead of per-profile, so initialization state is not
     * maintained per-profile although the preferences are stored as part of the
     * first loaded profile.
     */
    private boolean initialized = false;
    private final List<InitializationException> exceptions = new ArrayList<>();
    private static final Logger log = LoggerFactory.getLogger(GuiLafPreferencesManager.class);

    @Override
    @SuppressWarnings("deprecation")
    public void initialize(Profile profile) throws InitializationException {
        if (!initialized) {
            Preferences preferences = JmriPreferencesProvider.getPreferences(profile, getClass(), true);
            // getting the preference returns null if it had not previously been set
            boolean migrate = preferences.get(FONT_SIZE, null) == null;
            if (migrate) {
                // using deprecated, not for removal, call to enable migration
                // of preferences keys from apps.gui.* to jmri.util.gui.*
                getPreferences(JmriPreferencesProvider.getPreferences(profile, "apps-gui", true));
                setDirty(false);
            }
            getPreferences(preferences);
            setDirty(false);
            setRestartRequired(false);
            if (migrate) {
                log.info("Migrating preferences from apps.gui to jmri.util.gui...");
                savePreferences(profile, migrate);
            }
            initialized = true;
        }
    }

    private void getPreferences(Preferences preferences) {
        setLocale(Locale.forLanguageTag(preferences.get(LOCALE, getLocale().toLanguageTag())));
        setLookAndFeel(preferences.get(LOOK_AND_FEEL, getLookAndFeel()));

        setFontSize(preferences.getInt(FONT_SIZE, getFontSize()));
        if (getFontSize() == 0) {
            setFontSize(getDefaultFontSize());
        }

        setFont(preferences.get(FONT_NAME, getDefaultFont().getFontName()));
        if (getFont() == null) {
            setFont(getDefaultFont());
        }

        setNonStandardMouseEvent(preferences.getBoolean(NONSTANDARD_MOUSE_EVENT, isNonStandardMouseEvent()));
        setGraphicTableState(preferences.getBoolean(GRAPHIC_TABLE_STATE, isGraphicTableState()));
        setEditorUseOldLocSize(preferences.getBoolean(EDITOR_USE_OLD_LOC_SIZE, isEditorUseOldLocSize()));
        setToolTipDismissDelay(preferences.getInt(SHOW_TOOL_TIP_TIME, getToolTipDismissDelay()));

        log.debug("About to set default Locale");
        Locale.setDefault(getLocale());
        JComponent.setDefaultLocale(getLocale());

        applyLookAndFeel();
        applyFontSize();
        SwingSettings.setNonStandardMouseEvent(isNonStandardMouseEvent());
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
        savePreferences(profile, false);
    }

    private void savePreferences(Profile profile, boolean migrate) {
        Preferences preferences = JmriPreferencesProvider.getPreferences(profile, getClass(), true);
        preferences.put(LOCALE, getLocale().toLanguageTag());
        preferences.put(LOOK_AND_FEEL, getLookAndFeel());

        if (currentFont == null) {
            currentFont = getDefaultFont();
        }

        String currentFontName = currentFont.getFontName();
        if (currentFontName != null) {
            String prefFontName = preferences.get(FONT_NAME, currentFontName);
            if ((prefFontName == null) || (!prefFontName.equals(currentFontName))) {
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
        preferences.putBoolean(GRAPHIC_TABLE_STATE, isGraphicTableState());
        preferences.putBoolean(EDITOR_USE_OLD_LOC_SIZE, isEditorUseOldLocSize());
        preferences.putInt(SHOW_TOOL_TIP_TIME, getToolTipDismissDelay());
        try {
            preferences.sync();
            if (migrate) {
                preferences.flush();
            }
        } catch (BackingStoreException ex) {
            log.error("Unable to save preferences.", ex);
        }
        setDirty(false);
    }

    /**
     * @return the locale
     */
    @Nonnull
    public Locale getLocale() {
        return currentLocale;
    }

    /**
     * @param locale the locale to set
     */
    public void setLocale(@Nonnull Locale locale) {
        Locale oldLocale = currentLocale;
        currentLocale = locale;
        if (!oldLocale.equals(locale)) {
            setDirty(true);
            setRestartRequired(true);
            firePropertyChange(LOCALE, oldLocale, locale);
        }
    }

    /**
     * @return the currently selected font; will be null if no font is selected
     */
    public @CheckForNull Font getFont() {
        return currentFont;
    }

    /**
     * Sets a new font
     *
     * @param newFont the new font to set
     */
    public void setFont(@Nonnull Font newFont) {
        Font oldFont = currentFont;
        currentFont = newFont;
        if (currentFont != oldFont) {
            setDirty(true);
            setRestartRequired(true);
            firePropertyChange(FONT_NAME, oldFont, currentFont);
        }
    }

    /**
     * Sets a new font by name
     *
     * @param name the name of the new font to set
     */
    public void setFont(@Nonnull String name) {
        Font base = getFont();
        if (base == null) {
            base = getDefaultFont();
        }
        setFont(new Font(name, base.getStyle(), fontSize));
    }

    /**
     * Sets a new font by name
     *
     * @param name the name of the new font to set
     * @deprecated since 4.19.3; use {@link #setFont(String)} instead
     */
    @Deprecated
    public void setFontByName(@Nonnull String name) {
        setFont(name);
    }

    /**
     * Get the default font, using the font used for lists in the current
     * {@literal Look & Feel}, or by calling
     * {@link java.awt.Font#decode(String)} with a null value if the list font
     * is not specified.
     * 
     * @return the default font
     */
    public @Nonnull Font getDefaultFont() {
        Font defaultFont = UIManager.getFont("List.font");
        if (defaultFont == null) {
            defaultFont = Font.decode(null);
        }
        return defaultFont;
    }

    /**
     * Loads the current {@literal Look & Feel} default font, based on looking
     * up the {@code List.font}, or calling {@link java.awt.Font#decode(String)}
     * with a null value.
     * 
     * @see #getDefaultFont()
     * @deprecated since 4.19.3 without direct replacement
     */
    @Deprecated
    public void setDefaultFont() {
        // nothing to do
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
            setDirty(true);
            setRestartRequired(true);
            firePropertyChange(FONT_SIZE, oldFontSize, fontSize);
        }
    }

    /**
     * @return the size of the font returned by {@link #getDefaultFont()}
     */
    public int getDefaultFontSize() {
        return getDefaultFont().getSize();
    }

    /**
     * Load the current {@literal Look & Feel} default font size, based on the
     * default font.
     * 
     * @see #getDefaultFontSize()
     * @deprecated since 4.19.3 without direct replacement
     */
    @Deprecated
    public void setDefaultFontSize() {
        // nothing to do
    }

    /**
     * Logs LAF fonts at the TRACE level.
     */
    private void logAllFonts() {
        // avoid any activity if logging at level is disabled to avoid
        // the unnecessary overhead of getting the fonts
        if (log.isTraceEnabled()) {
            log.trace("******** LAF={}", UIManager.getLookAndFeel().getClass().getName());
            UIManager.getDefaults().forEach((key, value) -> {
                if (value instanceof FontUIResource ||
                        value instanceof Font ||
                        key.toString().endsWith(".font")) {
                    Font f = UIManager.getFont(key);
                    log.trace("Class={}; Key: {} Font: {} size: {}", value.getClass().getName(), key, f.getName(),
                            f.getSize());
                }
            });
        }
    }

    /**
     * Sets the time a tooltip is displayed before it goes away. Note that this
     * preference takes effect immediately.
     *
     * @param time the delay in seconds.
     */
    public void setToolTipDismissDelay(int time) {
        int old = toolTipDismissDelay;
        toolTipDismissDelay = time;
        ToolTipManager.sharedInstance().setDismissDelay(time);
        if (old != time) {
            setDirty(true);
            firePropertyChange(SHOW_TOOL_TIP_TIME, old, time);
        }
    }

    /**
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
     * @param flag the nonStandardMouseEvent to set
     */
    public void setNonStandardMouseEvent(boolean flag) {
        boolean old = nonStandardMouseEvent;
        nonStandardMouseEvent = flag;
        if (old != flag) {
            setDirty(true);
            setRestartRequired(true);
            firePropertyChange(NONSTANDARD_MOUSE_EVENT, old, flag);
        }
    }

    /**
     * @return the graphicTableState
     */
    public boolean isGraphicTableState() {
        return graphicTableState;
    }

    /**
     * @param flag the graphicTableState to set
     */
    public void setGraphicTableState(boolean flag) {
        boolean old = graphicTableState;
        graphicTableState = flag;
        if (old != flag) {
            setDirty(true);
            setRestartRequired(true);
            firePropertyChange(GRAPHIC_TABLE_STATE, old, flag);
        }
    }

    /**
     * @return the editorUseOldLocSize value
     */
    public boolean isEditorUseOldLocSize() {
        return editorUseOldLocSize;
    }

    /**
     * @param flag the editorUseOldLocSize value to set
     */
    public void setEditorUseOldLocSize(boolean flag) {
        boolean old = editorUseOldLocSize;
        editorUseOldLocSize = flag;
        if (old != flag) {
            setDirty(true);
            setRestartRequired(false);
            firePropertyChange(EDITOR_USE_OLD_LOC_SIZE, old, flag);
        }
    }

    /**
     * Get the name of the class implementing the preferred look and feel. Note
     * may not be the in-use look and feel if the preferred look and feel is not
     * available on the current platform; and will be overwritten if preferences
     * are saved on a platform where the preferred look and feel is not
     * available.
     * 
     * @return the look and feel class name
     */
    public @Nonnull String getLookAndFeel() {
        return lookAndFeel;
    }

    /**
     * Set the name of the class implementing the preferred look and feel. Note
     * change only takes effect after the application is restarted, because Java
     * has some issues setting the look and feel correctly on already open
     * windows.
     * 
     * @param name the look and feel class name
     */
    public void setLookAndFeel(@Nonnull String name) {
        String old = lookAndFeel;
        lookAndFeel = name;
        if (!old.equals(name)) {
            setDirty(true);
            setRestartRequired(true);
            firePropertyChange(LOOK_AND_FEEL, old, name);
        }
    }

    /**
     * Apply the existing look and feel.
     */
    public void applyLookAndFeel() {
        String lafClassName = UIManager.getLookAndFeel().getClass().getName();
        for (LookAndFeelInfo LAF : UIManager.getInstalledLookAndFeels()) {
            // accept either name or class name of look and feel
            if (LAF.getClassName().equals(lookAndFeel) || LAF.getName().equals(lookAndFeel)) {
                lafClassName = LAF.getClassName();
                // use first match, not last match (unlikely to be
                // multiple, but you never know)
                break;
            }
        }
        log.debug("Look and feel selection \"{}\" ({})", lookAndFeel, lafClassName);
        if (!lafClassName.equals(UIManager.getLookAndFeel().getClass().getName())) {
            log.debug("Apply look and feel \"{}\" ({})", lookAndFeel, lafClassName);
            try {
                UIManager.setLookAndFeel(lafClassName);
            } catch (ClassNotFoundException ex) {
                log.error("Could not find look and feel \"{}\".", lookAndFeel);
            } catch (
                    IllegalAccessException |
                    InstantiationException ex) {
                log.error("Could not load look and feel \"{}\".", lookAndFeel);
            } catch (UnsupportedLookAndFeelException ex) {
                log.error("Look and feel \"{}\" is not supported on platform.", lookAndFeel);
            }
        } else {
            log.debug("Not updating look and feel {} matching existing look and feel", lafClassName);
        }
    }

    /**
     * Applies a new calculated font size to all found fonts.
     * <p>
     * Calls {@link #getCalcFontSize(int) getCalcFontSize} to calculate new size
     * for each.
     */
    private void applyFontSize() {
        logAllFonts();
        if (getFontSize() != getDefaultFontSize()) {
            UIManager.getDefaults().forEach((key, value) -> {
                if (value instanceof FontUIResource ||
                        value instanceof Font ||
                        key.toString().endsWith(".font")) {
                    UIManager.put(key, UIManager.getFont(key).deriveFont(((Font) value).getStyle(),
                            getCalcFontSize(((Font) value).getSize())));
                }
            });
            logAllFonts();
        }
    }

    /**
     * Stand-alone service routine to set the default Locale.
     * <p>
     * Intended to be invoked early, as soon as a profile is available, to
     * ensure the correct language is set as startup proceeds. Must be followed
     * eventually by a complete {@link #setLocale}.
     * 
     * @param profile The profile to get the locale from
     */
    public static void setLocaleMinimally(Profile profile) {
        // use current default if no setting specified
        String name = JmriPreferencesProvider.getPreferences(profile, GuiLafPreferencesManager.class, true).get(LOCALE,
                Locale.getDefault().toLanguageTag());
        log.debug("setLocaleMinimally found language {}, setting", name);
        Locale locale = Locale.forLanguageTag(name);
        Locale.setDefault(locale);
        JComponent.setDefaultLocale(locale);
    }

    /**
     * @return a new calculated font size based on difference between default
     *         size and selected size
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
    private void setDirty(boolean flag) {
        boolean old = dirty;
        dirty = flag;
        propertyChangeSupport.firePropertyChange(PROP_DIRTY, old, flag);
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
     * @param flag true if application needs to restart to apply preferences
     */
    private void setRestartRequired(boolean flag) {
        boolean old = restartRequired;
        restartRequired = flag;
        propertyChangeSupport.firePropertyChange(PROP_RESTARTREQUIRED, old, flag);
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
