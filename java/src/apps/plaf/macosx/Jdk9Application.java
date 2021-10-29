package apps.plaf.macosx;

import java.awt.Desktop;
import java.awt.desktop.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Wrapper for Apple provided extensions to Java that allow Java apps to feel
 * more "Mac-like" on Mac OS X for JDK 9.
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
 * @author Randall Wood     (c) 2016
 * @author Daniel Bergqvist (c) 2021
 * @see Application
 */
class Jdk9Application extends Application {

    Jdk9Application() {
    }

    private void setHandler(String methodName, String handlerType, Object handler) {
        try {
            Class<?> parameterType = Class.forName(handlerType);
            Class<?>[] parameterTypes = {parameterType};
            Method method = java.awt.Desktop.class.getDeclaredMethod(methodName, parameterTypes);
            Object[] parameters = {handler};
            method.invoke(Desktop.getDesktop(), parameters);
        } catch (NoClassDefFoundError | ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            log.debug("Exception calling {} with {}", methodName, handlerType, ex);
        }
    }

    @Override
    public void setAboutHandler(final AboutHandler handler) {
        if (handler != null) {
            java.awt.Desktop.getDesktop().setAboutHandler(handler::handleAbout);
        } else {
            this.setHandler("setAboutHandler", "java.awt.desktop.AboutHandler", null); // NOI18N
        }
    }

    @Override
    public void setPreferencesHandler(final PreferencesHandler handler) {
        if (handler != null) {
            java.awt.Desktop.getDesktop().setPreferencesHandler(handler::handlePreferences);
        } else {
            this.setHandler("setPreferencesHandler", "java.awt.desktop.PreferencesHandler", null); // NOI18N
        }
    }

    @Override
    public void setQuitHandler(final QuitHandler handler) {
        if (handler != null) {
            java.awt.Desktop.getDesktop().setQuitHandler(
                    (QuitEvent qe, QuitResponse response) -> {
                        if (handler.handleQuitRequest(qe)) {
                            response.performQuit();
                        } else {
                            response.cancelQuit();
                        }
                    });
        } else {
            this.setHandler("setQuitHandler", "java.awt.desktop.QuitHandler", null); // NOI18N
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Jdk9Application.class);

}
