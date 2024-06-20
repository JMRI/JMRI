package jmri.script;

import java.util.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.script.*;

import jmri.InstanceManager;

/**
 * Selects a valid scripting engine.
 *
 * @author Daniel Bergqvist (C) 2022
 */
public class ScriptEngineSelector {

    public static final String JYTHON = "python";
    public static final String ECMA_SCRIPT = "ECMAScript";

    private Engine _selectedEngine = InstanceManager.getDefault(
            InternalScriptEngineSelector.class).getDefaultEngine();


    /**
     * Get the selected engine.
     * @return the engine
     */
    @CheckForNull
    public Engine getSelectedEngine() {
        return _selectedEngine;
    }

    /**
     * Sets the selected engine.
     * @param engine the engine
     */
    public void setSelectedEngine(@Nonnull Engine engine) {
        _selectedEngine = engine;
    }

    /**
     * Sets the selected engine from the language name.
     * @param languageName the engine
     */
    public void setSelectedEngine(@Nonnull String languageName) {
        Engine engine = InstanceManager.getDefault(
                InternalScriptEngineSelector.class).getEngineFromLanguage(languageName);
        if (engine != null) {
            setSelectedEngine(engine);
        } else {
            log.warn("Cannot select engine for the language {}", languageName);
        }
    }

    /**
     * Get a unmodifiable list of all the engines.
     * @return the list
     */
    @Nonnull
    public static List<Engine> getAllEngines() {
        return Collections.unmodifiableList(InstanceManager.getDefault(
                InternalScriptEngineSelector.class).getAllEngines());
    }


    public static class Engine {

        private final String _name;
        private final String _languageName;
        private final ScriptEngine _engine;

        Engine(String name, String id) throws ScriptException {
            _name = name;
            _languageName = id;
            _engine = JmriScriptEngineManager.getDefault().getEngineByName(_languageName);

            log.debug("Load script engine: {}, {}, {}", _name, _languageName, _engine);
        }

        /**
         * Get the name.
         * @return the name
         */
        @Nonnull
        public String getName() {
            return _name;
        }

        /**
         * Get the name.
         * @return the name
         */
        @Nonnull
        public String getLanguageName() {
            return _languageName;
        }

        /**
         * Get the script engine.
         * @return the script engine
         */
        @Nonnull
        public ScriptEngine getScriptEngine() {
            return _engine;
        }

        /**
         * Is this engine for Jython/Python?
         * @return true if Jython/Python, false otherwise
         */
        public boolean isJython() {
            return "python".equalsIgnoreCase(_languageName)
                    || "jython".equalsIgnoreCase(_languageName);
        }

        /**
         * Is this engine for JavaScript/ECMAscript?
         * @return true if JavaScript/ECMAscript, false otherwise
         */
        public boolean isJavaScript() {
            return "javascript".equalsIgnoreCase(_languageName)
                    || "ecmascript".equalsIgnoreCase(_languageName);
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return _name;
        }
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ScriptEngineSelector.class);

}
