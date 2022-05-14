package jmri.script;

import java.util.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.script.*;

/**
 * Selects a valid scripting engine.
 *
 * @author Daniel Bergqvist (C) 2022
 */
public class ScriptEngineSelector {

    public static final String JYTHON = "python";
    public static final String ECMA_SCRIPT = "ECMAScript";

    private static final Engine _defaultEngine;
    private static final List<Engine> _engines = new ArrayList<>();
    private static final Map<String, Engine> _engineMap = new HashMap<>();
    private static final Set<String> languageNames = new HashSet<>();

    private Engine _selectedEngine = _defaultEngine;


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
        _selectedEngine = _engineMap.get(languageName);
    }

    /**
     * Get a unmodifiable list of all the engines.
     * @return the list
     */
    @Nonnull
    public static List<Engine> getAllEngines() {
        return Collections.unmodifiableList(_engines);
    }


    public static class Engine {

        private final String _name;
        private final String _languageName;
        private final ScriptEngine _engine;

        private Engine(String name, String id) throws ScriptException {
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

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return _name;
        }
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ScriptEngineSelector.class);

    static {
        // get list of language names (to display) and IDs (for call)
        JmriScriptEngineManager.getDefault().getManager().getEngineFactories().stream().forEach((ScriptEngineFactory factory) -> {
            String version = factory.getEngineVersion();
            if (version != null) {
                String name = JmriScriptEngineManager.fileForLanguage(factory.getEngineName(), factory.getLanguageName());
                if (!languageNames.contains(name)) {
                    String languageName = factory.getLanguageName();
                    try {
                        languageNames.add(name);
                        Engine engine = new Engine(name, languageName);
                        _engines.add(engine);
                        _engineMap.put(languageName, engine);
                    } catch (ScriptException e) {
                        log.error("Cannot load script engine {}, {}", name, languageName);
                    }
                }
            }
        });

        if (_engineMap.containsKey("python")) {
            _defaultEngine = _engineMap.get("python");
        } else {
            if (!_engineMap.isEmpty()) {
                _defaultEngine = _engineMap.values().iterator().next();
            } else {
                _defaultEngine = null;
            }
        }
    }

}
