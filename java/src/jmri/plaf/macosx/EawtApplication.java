package jmri.plaf.macosx;

import com.apple.eawt.AppEvent.AboutEvent;
import com.apple.eawt.AppEvent.PreferencesEvent;
import com.apple.eawt.AppEvent.QuitEvent;
import com.apple.eawt.QuitResponse;

/**
 * Wrapper for Apple provided extensions to Java that allow Java apps to feel
 * more "Mac-like" on Mac OS X using JDK 8.
 * <p>
 * <b>NOTE</b> All use of this class must be wrapped in a conditional test that
 * ensures that JMRI is not running on Mac OS X or in Try-Catch blocks. The
 * easiest test is:  <pre><code>
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
 * @author Randall Wood (c) 2016
 * @see com.apple.eawt.Application
 */
class EawtApplication extends Application {

    private com.apple.eawt.Application application = null;

    EawtApplication() {
        application = com.apple.eawt.Application.getApplication();
    }

    @Override
    public void setAboutHandler(final AboutHandler handler) {
        try {
            if (handler != null) {
                application.setAboutHandler((AboutEvent ae) -> {
                    handler.handleAbout(ae);
                });
            } else {
                application.setAboutHandler(null);
            }
        } catch (java.lang.NoClassDefFoundError ex) {
            // this simply means that the OS X version is too old to implement this
            // so we ignore it
        }
    }

    @Override
    public void setPreferencesHandler(final PreferencesHandler handler) {
        try {
            if (handler != null) {
                application.setPreferencesHandler((PreferencesEvent pe) -> {
                    handler.handlePreferences(pe);
                });
            } else {
                application.setPreferencesHandler(null);
            }
        } catch (java.lang.NoClassDefFoundError ex) {
            // this simply means that the OS X version is too old to implement this
            // so we ignore it
        }
    }

    @Override
    public void setQuitHandler(final QuitHandler handler) {
        try {
            if (handler != null) {
                application.setQuitHandler((QuitEvent qe, QuitResponse qr) -> {
                    if (handler.handleQuitRequest(qe)) {
                        qr.performQuit();
                    } else {
                        qr.cancelQuit();
                    }
                });
            } else {
                application.setQuitHandler(null);
            }
        } catch (java.lang.NoClassDefFoundError ex) {
            // this simply means that the OS X version is too old to implement this
            // so we ignore it
        }
    }
}
