package jmri.script.swing;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.swing.JComboBox;

import jmri.script.JmriScriptEngineManager;

/**
 * A JComboBox for selecting a valid scripting engine.
 *
 * Persistence of settings, if desired, should be handled in the using class.
 *
 * @author Bob Jacobsen Copyright (C) 2004, 2021, 2022
 */
public class ScriptEngineSelector extends JComboBox<String> {

    /**
     * Obtain one of these; direct calls to ctor are not permitted
     */
    @Nonnull
    public static ScriptEngineSelector getScriptEngineSelector() {

        List<String> languageNames = new ArrayList<>();
        List<String> languageIDs = new ArrayList<>();

        // get list of language names (to display) and IDs (for call)
        JmriScriptEngineManager.getDefault().getManager().getEngineFactories().stream().forEach((ScriptEngineFactory factory) -> {
            String version = factory.getEngineVersion();
            if (version != null) {
                String name = JmriScriptEngineManager.fileForLanguage(factory.getEngineName(), factory.getLanguageName());
                if (!languageNames.contains(name)) {
                    languageNames.add(name);
                    languageIDs.add(factory.getLanguageName());
                }
            }
        });

        String[] nameArray = languageNames.toArray(new String[languageNames.size()]);

        ScriptEngineSelector retval = new ScriptEngineSelector(nameArray);
        retval.languageNames = languageNames;
        retval.languageIDs = languageIDs;

        return retval;
    }

    /**
     * Get the currently selected engine
     */
    @CheckForNull
    public ScriptEngine getEngine() {
        String language = languageIDs.get(getSelectedIndex());
        try {
            return JmriScriptEngineManager.getDefault().getEngineByName(language);
        } catch (ScriptException ex) {
            log.warn("could not allocate script engine", ex);
            return null;
        }
    }

    private ScriptEngineSelector() {
        super();
    }

    private ScriptEngineSelector(String[] arr) {
        super(arr);
    }

    List<String> languageNames = new ArrayList<>();
    List<String> languageIDs = new ArrayList<>();


    // initialize logging
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ScriptEngineSelector.class);
}
