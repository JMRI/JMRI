package apps.systemconsole;

import apps.SystemConsole;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import jmri.beans.Bean;
import jmri.profile.Profile;
import jmri.profile.ProfileUtils;
import jmri.spi.PreferencesManager;
import jmri.util.ThreadingUtil;
import jmri.util.prefs.InitializationException;
import jmri.util.swing.FontComboUtil;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage preferences for the {@link apps.SystemConsole}.
 *
 * @author Randall Wood
 */
@ServiceProvider(service = PreferencesManager.class)
public class SystemConsolePreferencesManager extends Bean implements PreferencesManager {

    public static final String SCHEME = "scheme";
    public static final String FONT_SIZE = "fontSize";
    public static final String FONT_STYLE = "fontStyle";
    public static final String FONT_FAMILY = "fontFamily";
    public static final String WRAP_STYLE = "wrapStyle";

    // default settings
    private int scheme = 0; // Green on Black
    private int fontSize = 12;
    private int fontStyle = Font.PLAIN;
    private String fontFamily = "Monospaced";  //NOI18N
    private int wrapStyle = SystemConsole.WRAP_STYLE_WORD;

    /*
     * Unlike most PreferencesProviders, the SystemConsole preferences should be
     * per-application instead of per-profile.
     */
    private boolean initialized = false;
    private final List<InitializationException> exceptions = new ArrayList<>();
    private static final Logger log = LoggerFactory.getLogger(SystemConsolePreferencesManager.class);

    @Override
    public void initialize(Profile profile) throws InitializationException {
        if (!this.initialized) {
            Preferences preferences = ProfileUtils.getPreferences(profile, this.getClass(), true);
            this.setFontFamily(preferences.get(FONT_FAMILY, this.getFontFamily()));
            this.setFontSize(preferences.getInt(FONT_SIZE, this.getFontSize()));
            this.setFontStyle(preferences.getInt(FONT_STYLE, this.getFontStyle()));
            this.setScheme(preferences.getInt(SCHEME, this.getScheme()));
            this.setWrapStyle(preferences.getInt(WRAP_STYLE, this.getWrapStyle()));
            this.initialized = true;
        }
    }

    @Override
    public void savePreferences(Profile profile) {
        Preferences preferences = ProfileUtils.getPreferences(profile, this.getClass(), true);
        preferences.put(FONT_FAMILY, this.getFontFamily());
        preferences.putInt(FONT_SIZE, this.getFontSize());
        preferences.putInt(FONT_STYLE, this.getFontStyle());
        preferences.putInt(SCHEME, this.getScheme());
        preferences.putInt(WRAP_STYLE, this.getWrapStyle());
        try {
            preferences.sync();
        } catch (BackingStoreException ex) {
            log.error("Unable to save preferences.", ex);
        }
    }

    @Override
    public boolean isInitialized(Profile profile) {
        return this.initialized && this.exceptions.isEmpty();
    }

    @Override
    public Iterable<Class<? extends PreferencesManager>> getRequires() {
        return new HashSet<>();
    }

    @Override
    public Iterable<Class<?>> getProvides() {
        Set<Class<?>> provides = new HashSet<>();
        provides.add(this.getClass());
        return provides;
    }

    /**
     * @return the scheme
     */
    public int getScheme() {
        return scheme;
    }

    /**
     * @param scheme the scheme to set
     */
    public void setScheme(int scheme) {
        int oldScheme = this.scheme;
        this.scheme = scheme;
        this.firePropertyChange(SCHEME, oldScheme, scheme);
        SystemConsole.getInstance().setScheme(scheme);
    }

    /**
     * @return the fontSize
     */
    public int getFontSize() {
        return fontSize;
    }

    /**
     * Sets the fontSize.
     * <p>
     * If the parameter is less than 6, the fontSize is set to 6. If the
     * parameter is greater than 24, the fontSize is set to 24.
     *
     * @param fontSize the fontSize to set
     */
    public void setFontSize(int fontSize) {
        int oldFontSize = this.fontSize;
        this.fontSize = fontSize < 6 ? 6 : fontSize > 24 ? 24 : fontSize;
        if (this.fontSize != oldFontSize) {
            this.firePropertyChange(FONT_SIZE, oldFontSize, this.fontSize);
            SystemConsole.getInstance().setFontSize(this.fontSize);
        }
    }

    /**
     * @return the fontStyle
     */
    public int getFontStyle() {
        return fontStyle;
    }

    /**
     * @param fontStyle one of {@link Font#BOLD}, {@link Font#ITALIC}, or
     *                  {@link Font#PLAIN}.
     */
    public void setFontStyle(int fontStyle) {
        if (fontStyle == Font.BOLD || fontStyle == Font.ITALIC || fontStyle == Font.PLAIN || fontStyle == (Font.BOLD | Font.ITALIC)) {
            int oldFontStyle = this.fontStyle;
            this.fontStyle = fontStyle;
            if (this.fontStyle != oldFontStyle) {
                this.firePropertyChange(FONT_STYLE, oldFontStyle, fontStyle);
                SystemConsole.getInstance().setFontStyle(this.fontStyle);
            }
        }
    }

    /**
     * @return the fontFamily
     */
    public String getFontFamily() {
        return fontFamily;
    }

    /**
     * @param fontFamily the fontFamily to set
     */
    public void setFontFamily(String fontFamily) {
        if (FontComboUtil.isReady()) {
            if (FontComboUtil.getFonts(FontComboUtil.MONOSPACED).contains(fontFamily)) {
                String oldFontFamily = this.fontFamily;
                this.fontFamily = fontFamily;
                this.firePropertyChange(FONT_FAMILY, oldFontFamily, fontFamily);
                SystemConsole.getInstance().setFontFamily(this.getFontFamily());
            } else {
                log.warn("Incompatible console font \"{}\" - using \"{}\"", fontFamily, this.getFontFamily());
            }
        } else {
            ThreadingUtil.runOnGUIDelayed(() -> {
                this.setFontFamily(fontFamily);
            }, 1000); // one second
        }
    }

    /**
     * @return the wrapStyle
     */
    public int getWrapStyle() {
        return wrapStyle;
    }

    /**
     * @param wrapStyle One of
     *                  {@link apps.SystemConsole#WRAP_STYLE_LINE}, {@link apps.SystemConsole#WRAP_STYLE_NONE},
     *                  or {@link apps.SystemConsole#WRAP_STYLE_WORD}.
     */
    public void setWrapStyle(int wrapStyle) {
        if (wrapStyle == SystemConsole.WRAP_STYLE_LINE
                || wrapStyle == SystemConsole.WRAP_STYLE_NONE
                || wrapStyle == SystemConsole.WRAP_STYLE_WORD) {
            int oldWrapStyle = this.wrapStyle;
            this.wrapStyle = wrapStyle;
            this.firePropertyChange(WRAP_STYLE, oldWrapStyle, wrapStyle);
            SystemConsole.getInstance().setWrapStyle(this.getWrapStyle());
        }
    }

    @Override
    public boolean isInitializedWithExceptions(Profile profile) {
        return this.initialized && !this.exceptions.isEmpty();
    }

    @Override
    public List<Exception> getInitializationExceptions(Profile profile) {
        return new ArrayList<>(this.exceptions);
    }

}
