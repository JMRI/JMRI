package apps.plaf.macosx;

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
 *
 * @author Randall Wood (c) 2011, 2016
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
                log.error("failed to start desktop support");
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

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Application.class);
}
