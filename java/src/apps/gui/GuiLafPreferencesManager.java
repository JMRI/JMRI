package apps.gui;

import java.awt.Font;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import jmri.beans.Bean;
import jmri.profile.Profile;
import jmri.profile.ProfileUtils;
import jmri.util.prefs.InitializationException;
import jmri.spi.PreferencesProvider;
import jmri.util.swing.SwingSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Randall Wood (C) 2015
 */
public class GuiLafPreferencesManager extends Bean implements PreferencesProvider {

    public static final String FONT_SIZE = "fontSize";
    public static final String LOCALE = "locale";
    public static final String LOOK_AND_FEEL = "lookAndFeel";
    public static final String NONSTANDARD_MOUSE_EVENT = "nonstandardMouseEvent";
    public final static String SHOW_TOOL_TIP_TIME = "showToolTipDismissDelay";
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

    // preferences with default values
    private Locale locale = Locale.getDefault();
    private int fontSize = 0;
    private int defaultFontSize = 0;
    private boolean nonStandardMouseEvent = false;
    private String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();
    private int toolTipDismissDelay = ToolTipManager.sharedInstance().getDismissDelay();

    /*
     * Unlike most PreferencesProviders, the GUI Look & Feel preferences should
     * be per-application instead of per-profile.
     */
    private boolean initialized = false;
    private final static Logger log = LoggerFactory.getLogger(GuiLafPreferencesManager.class);

    @Override
    public void initialize(Profile profile) throws InitializationException {
        if (!this.initialized) {
            Preferences preferences = ProfileUtils.getPreferences(profile, this.getClass(), true);
            this.setLocale(Locale.forLanguageTag(preferences.get(LOCALE, this.getLocale().toLanguageTag())));
            this.setLookAndFeel(preferences.get(LOOK_AND_FEEL, this.getLookAndFeel()));
            this.setDefaultFontSize(); // before we change anything
            this.setFontSize(preferences.getInt(FONT_SIZE, this.getDefaultFontSize()));
            if (this.getFontSize() == 0) {
                this.setFontSize(this.getDefaultFontSize());
            }
            this.setNonStandardMouseEvent(preferences.getBoolean(NONSTANDARD_MOUSE_EVENT, this.isNonStandardMouseEvent()));
            this.setToolTipDismissDelay(preferences.getInt(SHOW_TOOL_TIP_TIME, this.getToolTipDismissDelay()));
            Locale.setDefault(this.getLocale());
            this.applyLookAndFeel();
            this.applyFontSize();
            SwingSettings.setNonStandardMouseEvent(this.isNonStandardMouseEvent());
            this.initialized = true;
        }
    }

    @Override
    public boolean isInitialized(Profile profile) {
        return this.initialized;
    }

    @Override
    public Iterable<Class<? extends PreferencesProvider>> getRequires() {
        return new HashSet<>();
    }

    @Override
    public Iterable<Class<?>> getProvides() {
        Set<Class<?>> provides = new HashSet<>();
        provides.add(this.getClass());
        return provides;
    }

    @Override
    public void savePreferences(Profile profile) {
        Preferences preferences = ProfileUtils.getPreferences(profile, this.getClass(), true);
        preferences.put(LOCALE, this.getLocale().toLanguageTag());
        preferences.put(LOOK_AND_FEEL, this.getLookAndFeel());
        int temp = this.getFontSize();
        if (temp == this.getDefaultFontSize()) {
            temp = 0;
        }
        if (temp != preferences.getInt(FONT_SIZE, -1)) {
            preferences.putInt(FONT_SIZE, temp);
        }
        preferences.putBoolean(NONSTANDARD_MOUSE_EVENT, this.isNonStandardMouseEvent());
        preferences.putInt(SHOW_TOOL_TIP_TIME, this.getToolTipDismissDelay());
        try {
            preferences.sync();
        } catch (BackingStoreException ex) {
            log.error("Unable to save preferences.", ex);
        }
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
        Locale oldLocale = this.locale;
        this.locale = locale;
        firePropertyChange(LOCALE, oldLocale, locale);
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
        int oldFontSize = this.fontSize;
        this.fontSize = (newFontSize == 0) ? 0 : ((newFontSize < MIN_FONT_SIZE) ? MIN_FONT_SIZE : ((newFontSize > MAX_FONT_SIZE) ? MAX_FONT_SIZE : newFontSize));
        if (this.fontSize != oldFontSize) {
            firePropertyChange(FONT_SIZE, oldFontSize, this.fontSize);
        }
    }

   /**
     * @return the current Look & Feel default font size
     */
    public int getDefaultFontSize() {
        return defaultFontSize;
    }

   /**
     * Called to load the current Look & Feel default font size, based on looking up the "List.font" size
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
                log.debug("Key:" + key.toString() + " Font: " + f.getName() + " size: " + f.getSize());
                defaultFontSize = f.getSize();
                return;
            }
        }
        defaultFontSize = 11;	// couldn't find the default return a reasonable font size
    }

    private void listLAFfonts() {
        log.debug("******** LAF=" + UIManager.getLookAndFeel().getClass().getName());
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource || value instanceof java.awt.Font || key.toString().endsWith(".font")) {
                Font f = UIManager.getFont(key);
                log.debug("Class=" + value.getClass().getName() + ";Key:" + key.toString() + " Font: " + f.getName() + " size: " + f.getSize());
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
        this.toolTipDismissDelay = time;
        ToolTipManager.sharedInstance().setDismissDelay(time);
    }

    /**
     *
     * @return the int
     */
    public int getToolTipDismissDelay() {
        return this.toolTipDismissDelay;
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
        boolean oldNonStandardMouseEvent = this.nonStandardMouseEvent;
        this.nonStandardMouseEvent = nonStandardMouseEvent;
        firePropertyChange(NONSTANDARD_MOUSE_EVENT, oldNonStandardMouseEvent, nonStandardMouseEvent);
    }

    /**
     * @return the lookAndFeel
     */
    public String getLookAndFeel() {
        return lookAndFeel;
    }

    /**
     * @param lookAndFeel the lookAndFeel to set
     */
    public void setLookAndFeel(String lookAndFeel) {
        String oldLookAndFeel = this.lookAndFeel;
        this.lookAndFeel = lookAndFeel;
        firePropertyChange(LOOK_AND_FEEL, oldLookAndFeel, lookAndFeel);
    }

    /**
     * Apply the existing look and feel.
     */
    public void applyLookAndFeel() {
        String lafClassName = null;
        for (LookAndFeelInfo LAF : UIManager.getInstalledLookAndFeels()) {
            if (LAF.getName().equals(this.lookAndFeel)) {
                lafClassName = LAF.getClassName();
            }
        }
        log.debug("Look and feel selection \"{}\" ({})", this.lookAndFeel, lafClassName);
        if (lafClassName != null) {
            if (!lafClassName.equals(UIManager.getLookAndFeel().getClass().getName())) {
                log.debug("Apply look and feel \"{}\" ({})", this.lookAndFeel, lafClassName);
                try {
                    UIManager.setLookAndFeel(lafClassName);
                } catch (ClassNotFoundException ex) {
                    log.error("Could not find look and feel \"{}\".", this.lookAndFeel);
                } catch (IllegalAccessException | InstantiationException ex) {
                    log.error("Could not load look and feel \"{}\".", this.lookAndFeel);
                } catch (UnsupportedLookAndFeelException ex) {
                    log.error("Look and feel \"{}\" is not supported on this platform.", this.lookAndFeel);
                }
            } else {
                log.debug("Not updating look and feel {} matching existing look and feel" + lafClassName);
            }
        }
    }

    /**
     * Applies a new calculated font size to all found fonts.
     * <br><br>
     * Calls {@link #getCalcFontSize(int) getCalcFontSize} to calculate new size for each.
     */
    private void applyFontSize() {
        if (log.isDebugEnabled()) {
            listLAFfonts();
        }
        if (this.getFontSize() != this.getDefaultFontSize()) {
//            UIManager.getDefaults().keySet().stream().forEach((key) -> {
            Enumeration<Object> keys = UIManager.getDefaults().keys();
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                Object value = UIManager.get(key);
                if (value instanceof javax.swing.plaf.FontUIResource || value instanceof java.awt.Font || key.toString().endsWith(".font")) {
                    UIManager.put(key, UIManager.getFont(key).deriveFont(((Font) value).getStyle(), getCalcFontSize(((Font) value).getSize())));
                }
            }
            if (log.isDebugEnabled()) {
                listLAFfonts();
            }
        }
    }

    /**
     * @return a new calculated font size based on difference between default
     * size and selected size
     *
     * @param oldSize the old font size
     */
    private int getCalcFontSize(int oldSize) {
        return oldSize + (this.getFontSize() - this.getDefaultFontSize());
    }
}
