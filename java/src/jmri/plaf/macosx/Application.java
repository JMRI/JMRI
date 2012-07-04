// Application.java
package jmri.plaf.macosx;

import com.apple.eawt.AppEvent.AboutEvent;
import com.apple.eawt.AppEvent.PreferencesEvent;
import com.apple.eawt.AppEvent.QuitEvent;
import com.apple.eawt.ApplicationEvent;
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
            legacyListener();
            legacyListener.setAboutHandler(handler);
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
            legacyListener();
            legacyListener.setPreferencesHandler(handler);
        }
    }
    
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
        private QuitHandler quitHandler = null;
        private AboutHandler aboutHandler = null;
        private PreferencesHandler preferencesHandler = null;

        protected void setAboutHandler(AboutHandler handler) {
            aboutHandler = handler;
            if (handler != null) {
                application.addAboutMenuItem();
                application.setEnabledAboutMenu(true);
            } else {
                application.setEnabledAboutMenu(false);
                application.removeAboutMenuItem();
            }
        }

        protected void setPreferencesHandler(PreferencesHandler handler) {
            preferencesHandler = handler;
            if (handler != null) {
                application.addPreferencesMenuItem();
                application.setEnabledPreferencesMenu(true);
            } else {
                application.setEnabledPreferencesMenu(false);
                application.removePreferencesMenuItem();
            }
        }

        protected void setQuitHandler(QuitHandler handler) {
            quitHandler = handler;
        }

        @Override
        public void handleAbout(ApplicationEvent ae) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void handleOpenApplication(ApplicationEvent ae) {
            // Do not take any special activity if a user opens the application
        }

        @Override
        public void handleOpenFile(ApplicationEvent ae) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void handlePreferences(ApplicationEvent ae) {
            if (preferencesHandler != null) {
                preferencesHandler.handlePreferences(ae);
            }
        }

        @Override
        public void handlePrintFile(ApplicationEvent ae) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void handleQuit(ApplicationEvent ae) {
            if (quitHandler != null) {
                ae.setHandled(quitHandler.handleQuitRequest(ae));
            }
        }

        @Override
        public void handleReOpenApplication(ApplicationEvent ae) {
            // Do not take any special activity if a user opens the application
        }

    }
}
