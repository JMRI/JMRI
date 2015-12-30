package apps.gui;

import apps.GuiLafConfigPane;
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
import javax.swing.plaf.FontUIResource;
import jmri.beans.Bean;
import jmri.profile.Profile;
import jmri.profile.ProfileUtils;
import jmri.spi.InitializationException;
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
    public final static String SHOW_TOOL_TIP_TIME = "showToolTipTime";

    // preferences with default values
    private Locale locale = Locale.getDefault();
    private int fontSize = 0;
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
            this.setFontSize(preferences.getInt(FONT_SIZE, this.getFontSize()));
            this.setLocale(Locale.forLanguageTag(preferences.get(LOCALE, this.getLocale().toLanguageTag())));
            this.setLookAndFeel(preferences.get(LOOK_AND_FEEL, this.getLookAndFeel()));
            this.setNonStandardMouseEvent(preferences.getBoolean(NONSTANDARD_MOUSE_EVENT, this.isNonStandardMouseEvent()));
            this.setToolTipDismissDelay(preferences.getInt(SHOW_TOOL_TIP_TIME, this.getToolTipDismissDelay()));
            Locale.setDefault(this.getLocale());
            GuiLafConfigPane.setFontSize(this.getFontSize()); // This is backwards - GuiLafConfigPane should be getting our fontSize when it needs it
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
        preferences.putInt(FONT_SIZE, this.getFontSize());
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
        propertyChangeSupport.firePropertyChange(LOCALE, oldLocale, locale);
    }

    /**
     * @return the fontSize
     */
    public int getFontSize() {
        return fontSize;
    }

    /**
     * @param fontSize the fontSize to set
     */
    public void setFontSize(int fontSize) {
        int oldFontSize = this.fontSize;
        this.fontSize = (fontSize == 0) ? 0 : ((fontSize < 9) ? 9 : ((fontSize > 18) ? 18 : fontSize));
        if (this.fontSize != oldFontSize) {
            propertyChangeSupport.firePropertyChange(FONT_SIZE, oldFontSize, this.fontSize);
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
        propertyChangeSupport.firePropertyChange(NONSTANDARD_MOUSE_EVENT, oldNonStandardMouseEvent, nonStandardMouseEvent);
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
        propertyChangeSupport.firePropertyChange(LOOK_AND_FEEL, oldLookAndFeel, lookAndFeel);
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

    private void applyFontSize() {
        if (this.getFontSize() != 0) {
//            UIManager.getDefaults().keySet().stream().forEach((key) -> {
//                Object value = UIManager.get(key);
//                if (value instanceof FontUIResource) {
//                    UIManager.put(key, UIManager.getFont(key).deriveFont(((Font)value).getStyle(), (float) this.getFontSize()));
//                }
//            });
            Enumeration<Object> keys = UIManager.getDefaults().keys();
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                Object value = UIManager.get(key);
                if (value instanceof FontUIResource) {
                    UIManager.put(key, UIManager.getFont(key).deriveFont(((Font) value).getStyle(), this.getFontSize()));
                }
            }
        }
    }
}
