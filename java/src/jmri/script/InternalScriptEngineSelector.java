package jmri.script;

import java.util.*;

import javax.annotation.Nonnull;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

import jmri.InstanceManagerAutoDefault;
import jmri.script.ScriptEngineSelector.Engine;

/**
 * Internal class for the ScriptEngineSelector
 * @author Daniel Bergqvist (C) 2022
 */
public class InternalScriptEngineSelector implements InstanceManagerAutoDefault {

    private final Engine _defaultEngine;
    private final List<Engine> _engines = new ArrayList<>();
    private final Map<String, Engine> _engineMap = new HashMap<>();
    private final Set<String> languageNames = new HashSet<>();


    public InternalScriptEngineSelector() {
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

    Engine getDefaultEngine() {
        return _defaultEngine;
    }

    Engine getEngineFromLanguage(String language) {
        return _engineMap.get(language);
    }

    @Nonnull
    public List<Engine> getAllEngines() {
        return Collections.unmodifiableList(_engines);
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(InternalScriptEngineSelector.class);

}
