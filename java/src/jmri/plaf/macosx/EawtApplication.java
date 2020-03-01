package jmri.plaf.macosx;

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
 * @author Randall Wood (c) 2016, 2020
 * @see com.apple.eawt.Application
 */
class EawtApplication extends Application {

    EawtApplication() {
    }

    @Override
    public void setAboutHandler(final AboutHandler handler) {
        setScriptedHandler(handler, "AboutHandlerEawt.js"); // NOI18N
    }

    @Override
    public void setPreferencesHandler(final PreferencesHandler handler) {
        setScriptedHandler(handler, "PreferencesHandlerEawt.js"); // NOI18N
    }

    @Override
    public void setQuitHandler(final QuitHandler handler) {
        setScriptedHandler(handler, "QuitHandlerEawt.js"); // NOI18N
    }
}
