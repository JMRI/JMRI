// Application.java
package jmri.plaf.macosx;

import com.apple.eawt.AppEvent.PreferencesEvent;
import com.apple.eawt.AppEvent.QuitEvent;
import com.apple.eawt.ApplicationEvent;
import com.apple.eawt.PreferencesHandler;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;
import jmri.util.SystemType;

/**
 * Wrapper for Apple provided extensions to Java that allow Java apps to feel
 * more "Mac-like" on Mac OS X.
 * <p>
 * <b>NOTE</b> All use of this class must be wrapped in a conditional test that
 * ensures that JMRI is not running on Mac OS X or in Try-Catch blocks. The
 * easiest test is:
 * <code><pre>
 * if (SystemType.isMacOSX()) {
 *     ...
 * }
 * </pre></code>
 * A Try-Catch block will need to catch {@link java.lang.NoClassDefFoundError}
 * Failure to use one of these methods will result in crashes.
 * <p>
 * This wrapper currently provides incomplete support for the Apple
 * {@link com.apple.eawt.Application} class, as it only provides support for
 * those integration aspects that were implemented in JMRI 2.12.
 *
 * @author rhwood
 * @see com.apple.eawt.Application
 */
public class Application {
    
    private static Application sharedApplication = null;
    private com.apple.eawt.Application application = null;
    private Class applicationClass = null;
    private ApplicationListener legacyListener = null;
    
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

    public void setPreferencesHandler(final EventHandler handler) {
        try {
            if (handler != null) {
                application.setPreferencesHandler(new PreferencesHandler() {

                    public void handlePreferences(PreferencesEvent pe) {
                        handler.eventHandled(pe);
                    }
                });
            } else {
                application.setPreferencesHandler(null);
            }
        } catch (java.lang.NoClassDefFoundError ex) {
            legacyListener();
            legacyListener.setPreferencesHandler(handler);
        }
    }
    
    public void setQuitHandler(final EventHandler handler) {
        try {
            if (handler != null) {
                application.setQuitHandler(new QuitHandler() {

                    public void handleQuitRequestWith(QuitEvent qe, QuitResponse qr) {
                        if (handler.eventHandled(qe)) {
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
            legacyListener();
            legacyListener.setQuitHandler(handler);
        }
    }

    // Support Java 1.5
    private void legacyListener() {
        if (legacyListener == null) {
            legacyListener = new ApplicationListener();
            application.addApplicationListener(legacyListener);
        }
    }

    // Support Java 1.5
    private class ApplicationListener implements com.apple.eawt.ApplicationListener {
        private EventHandler quitHandler = null;
        private EventHandler preferencesHandler = null;

        protected void setPreferencesHandler(EventHandler handler) {
            preferencesHandler = handler;
        }

        protected void setQuitHandler(EventHandler handler) {
            quitHandler = handler;
        }

        public void handleAbout(ApplicationEvent ae) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void handleOpenApplication(ApplicationEvent ae) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void handleOpenFile(ApplicationEvent ae) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void handlePreferences(ApplicationEvent ae) {
            if (preferencesHandler != null) {
                preferencesHandler.eventHandled(ae);
            }
        }

        public void handlePrintFile(ApplicationEvent ae) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void handleQuit(ApplicationEvent ae) {
            if (quitHandler != null) {
                quitHandler.eventHandled(ae);
            }
        }

        public void handleReOpenApplication(ApplicationEvent ae) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }
}
