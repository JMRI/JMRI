package jmri.plaf.macosx;

import java.awt.Desktop;
import java.io.InputStreamReader;
import javax.annotation.Nonnull;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.script.SimpleScriptContext;
import jmri.script.JmriScriptEngineManager;
import jmri.util.SystemType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger log = LoggerFactory.getLogger(Application.class);
    
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

    protected ScriptContext getContext(@Nonnull Object handler) {
        Bindings bindings = new SimpleBindings();
        bindings.put("handler", handler); // NOI18N
        ScriptContext context = new SimpleScriptContext();
        context.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
        return context;
    }
    
    protected void setScriptedHandler(@Nonnull Object handler, @Nonnull String scriptName) {
        try {
            InputStreamReader reader = new InputStreamReader(Jdk9Application.class.getResourceAsStream(scriptName));
            ScriptEngine engine = JmriScriptEngineManager.getDefault().getEngineByMimeType("js"); // NOI18N
            engine.eval(reader, this.getContext(handler));
        } catch (ScriptException ex) {
            log.error("Unable to execute script {}", scriptName, ex);
        }
    }
}
