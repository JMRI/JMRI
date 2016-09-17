package jmri.plaf.macosx;

import java.awt.Desktop;
import jmri.util.SystemType;

/**
 * Wrapper for Apple provided extensions to Java that allow Java apps to feel
 * more "Mac-like" on Mac OS X.
 * <p>
 * <b>NOTE</b> All use of this class must be wrapped in a conditional test that
 * ensures that JMRI is not running on Mac OS X or in Try-Catch blocks. The
 * easiest test is:
 * <pre><code>
 * if (SystemType.isMacOSX()) {
 *     ...
 * }
 * </code></pre> A Try-Catch block will need to catch
 * {@link java.lang.NoClassDefFoundError} Failure to use one of these methods
 * will result in crashes.
 * <p>
 * This wrapper currently provides incomplete support for the Apple
 * {@link com.apple.eawt.Application} class, as it only provides support for
 * those integration aspects that were implemented in JMRI 3.1.
 *
 * @author Randall Wood (c) 2011, 2016
 * @see EawtApplication
 * @see Jdk9Application
 */
abstract public class Application {

    private static volatile Application sharedApplication = null;

    public static Application getApplication() {
        if (!SystemType.isMacOSX()) {
            return null;
        }
        if (sharedApplication == null) {
            try {
                // test that Desktop supports AboutHandlers
                if (Desktop.getDesktop().isSupported(Desktop.Action.valueOf("APP_ABOUT"))) { // NOI18N
                    sharedApplication = new Jdk9Application();
                }
            } catch (IllegalArgumentException ex) {
                // if Desktop.Action does not include AboutHandlers, its not
                // recognized to be (un)supported, so assume the Eawt is available 
                sharedApplication = new EawtApplication();
            }
        }
        return sharedApplication;
    }

    Application() {
        // do nothing but require that subclass constructors are package private
    }

    abstract public void setAboutHandler(final AboutHandler handler);

    abstract public void setPreferencesHandler(final PreferencesHandler handler);

    abstract public void setQuitHandler(final QuitHandler handler);
}
