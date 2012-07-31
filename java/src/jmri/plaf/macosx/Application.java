// Application.java
package jmri.plaf.macosx;

import com.apple.eawt.AppEvent.AboutEvent;
import com.apple.eawt.AppEvent.PreferencesEvent;
import com.apple.eawt.AppEvent.QuitEvent;
import com.apple.eawt.QuitResponse;
import jmri.util.SystemType;

/**
 * Wrapper for Apple provided extensions to Java that allow Java apps to feel
 * more "Mac-like" on Mac OS X. <p> <b>NOTE</b> All use of this class must be
 * wrapped in a conditional test that ensures that JMRI is not running on Mac OS
 * X or in Try-Catch blocks. The easiest test is:
 * <code><pre>
 * if (SystemType.isMacOSX()) {
 *     ...
 * }
 * </pre></code> A Try-Catch block will need to catch
 * {@link java.lang.NoClassDefFoundError} Failure to use one of these methods
 * will result in crashes. <p> This wrapper currently provides incomplete
 * support for the Apple {@link com.apple.eawt.Application} class, as it only
 * provides support for those integration aspects that were implemented in JMRI
 * 3.1.
 *
 * @author rhwood
 * @see com.apple.eawt.Application
 */
public class Application {

    private static Application sharedApplication = null;
    private com.apple.eawt.Application application = null;

    public static Application getApplication() {
        if (!SystemType.isMacOSX()) {
            return null;
        }
        if (sharedApplication == null) {
            sharedApplication = new Application();
        }
        return sharedApplication;
    }

    private Application() {
        if (application == null) {
            application = com.apple.eawt.Application.getApplication();
        }
    }

    public void setAboutHandler(final AboutHandler handler) {
        try {
            if (handler != null) {
                application.setAboutHandler(new com.apple.eawt.AboutHandler() {
                    @Override
                    public void handleAbout(AboutEvent ae) {
                        handler.handleAbout(ae);
                    }
                });
            } else {
                application.setAboutHandler(null);
            }
        } catch (java.lang.NoClassDefFoundError ex) {
            // this simply means that the OS X version is too old to implement this
            // so we ignore it
        }
    }

    public void setPreferencesHandler(final PreferencesHandler handler) {
        try {
            if (handler != null) {
                application.setPreferencesHandler(new com.apple.eawt.PreferencesHandler() {
                    @Override
                    public void handlePreferences(PreferencesEvent pe) {
                        handler.handlePreferences(pe);
                    }
                });
            } else {
                application.setPreferencesHandler(null);
            }
        } catch (java.lang.NoClassDefFoundError ex) {
            // this simply means that the OS X version is too old to implement this
            // so we ignore it
        }
    }
/*
    public static void setWindowCanFullScreen(Window window, boolean state) {
        if (SystemType.isMacOSX()) {
            try {
                FullScreenUtilities.setWindowCanFullScreen(window, state);
            } catch (java.lang.NoClassDefFoundError ex) {
                // this simply means that the OS X version is too old to implement this
                // so we ignore it
            }
        }
    }

    public static void addFullScreenListenerTo(Window window, FullScreenListener listener) {
        if (SystemType.isMacOSX()) {
            try {
                FullScreenUtilities.addFullScreenListenerTo(window, listener);
            } catch (java.lang.NoClassDefFoundError ex) {
                // this simply means that the OS X version is too old to implement this
                // so we ignore it
            }
        }
    }

    public static void removeFullScreenListenerFrom(Window window, FullScreenListener listener) {
        if (SystemType.isMacOSX()) {
            try {
                FullScreenUtilities.removeFullScreenListenerFrom(window, listener);
            } catch (java.lang.NoClassDefFoundError ex) {
                // this simply means that the OS X version is too old to implement this
                // so we ignore it
            }
        }
    }
*/
    public void setQuitHandler(final QuitHandler handler) {
        try {
            if (handler != null) {
                application.setQuitHandler(new com.apple.eawt.QuitHandler() {
                    @Override
                    public void handleQuitRequestWith(QuitEvent qe, QuitResponse qr) {
                        if (handler.handleQuitRequest(qe)) {
                            qr.performQuit();
                        } else {
                            qr.cancelQuit();
                        }
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
