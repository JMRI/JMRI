package jmri.plaf.macosx;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger log = LoggerFactory.getLogger(EawtApplication.class);

    EawtApplication() {
    }

    private void setNullHandler(String methodName, String handlerType) {
        try {
            Class<?> parameterType = Class.forName(handlerType);
            Class<?>[] parameterTypes = {parameterType};
            Method method = com.apple.eawt.Application.class.getDeclaredMethod(methodName, parameterTypes);
            method.invoke(com.apple.eawt.Application.getApplication(), (Object) null);
        } catch (NoClassDefFoundError | ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            log.error("Exception calling {} with {}", methodName, handlerType, ex);
        }
    }

    @Override
    public void setAboutHandler(final AboutHandler handler) {
        if (handler != null) {
            setScriptedHandler(handler, "AboutHandlerEawt.js"); // NOI18N
        } else {
            this.setNullHandler("setAboutHandler", "com.apple.eawt.AppEvent.AboutEvent"); // NOI18N
        }
    }

    @Override
    public void setPreferencesHandler(final PreferencesHandler handler) {
        if (handler != null) {
            setScriptedHandler(handler, "PreferencesHandlerEawt.js"); // NOI18N
        } else {
            this.setNullHandler("setPreferenceHandler", "com.apple.eawt.AppEvent.PreferencesEvent"); // NOI18N
        }
    }

    @Override
    public void setQuitHandler(final QuitHandler handler) {
        if (handler != null) {
            setScriptedHandler(handler, "QuitHandlerEawt.js"); // NOI18N
        } else {
            this.setNullHandler("setQuitHandler", "com.apple.eawt.AppEvent.QuitEvent"); // NOI18N
        }
    }
}
