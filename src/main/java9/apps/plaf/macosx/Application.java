package apps.plaf.macosx;

import java.awt.Desktop;

import jmri.util.SystemType;

import org.apiguardian.api.API;

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
 * @author Randall Wood (c) 2011, 2016, 2020
 */
@API(status=API.Status.MAINTAINED)
public final class Application {

    private static volatile Application sharedApplication = null;

    public static Application getApplication() {
        if (!SystemType.isMacOSX()) {
            return null;
        }
        if (sharedApplication == null) {
            sharedApplication = new Application();
        }
        return sharedApplication;
    }

    // package private
    Application() {
        // nothing to do
    }

    public void setAboutHandler(final AboutHandler handler) {
        if (handler != null) {
            Desktop.getDesktop().setAboutHandler(ae -> handler.handleAbout(ae));
        } else {
            Desktop.getDesktop().setAboutHandler(null);
        }
    }

    public void setPreferencesHandler(final PreferencesHandler handler) {
        if (handler != null) {
            Desktop.getDesktop().setPreferencesHandler(pe -> handler.handlePreferences(pe));
        } else {
            Desktop.getDesktop().setPreferencesHandler(null);
        }
    }

    public void setQuitHandler(final QuitHandler handler) {
        if (handler != null) {
            Desktop.getDesktop().setQuitHandler((qe, qr) -> {
                if (handler.handleQuitRequest(qe)) {
                    qr.performQuit();
                } else {
                    qr.cancelQuit();
                }
            });
        } else {
            Desktop.getDesktop().setQuitHandler(null);
        }
    }
}
