package jmri.script;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Properties;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.script.SimpleScriptContext;
import jmri.AddressedProgrammerManager;
import jmri.AudioManager;
import jmri.BlockManager;
import jmri.CommandStation;
import jmri.GlobalProgrammerManager;
import jmri.InstanceManager;
import jmri.Light;
import jmri.LightManager;
import jmri.MemoryManager;
import jmri.NamedBean;
import jmri.PowerManager;
import jmri.ReporterManager;
import jmri.RouteManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.ShutDownManager;
import jmri.SignalHead;
import jmri.SignalHeadManager;
import jmri.SignalMastManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.jmrit.logix.WarrantManager;
import jmri.util.FileUtil;
import jmri.util.FileUtilSupport;
import org.apache.commons.io.FilenameUtils;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide a manager for {@link javax.script.ScriptEngine}s.
 *
 * The following methods are the only mechanisms for evaluating a Python script
 * that respect the <code>jython.exec</code> property in the
 * <em>python.properties</em> file:
 * <ul>
 * <li>{@link #eval(java.io.File)}</li>
 * <li>{@link #eval(java.io.File, javax.script.Bindings)}</li>
 * <li>{@link #eval(java.io.File, javax.script.ScriptContext)}</li>
 * <li>{@link #eval(java.lang.String, javax.script.ScriptEngine)}</li>
 * <li>{@link #runScript(java.io.File)}</li>
 * </ul>
 * Evaluating a script using <code>getEngine*(java.lang.String).eval(...)</code>
 * methods will not respect the <code>jython.exec</code> property, although all
 * methods will respect all other properties of that file.
 *
 * @author Randall Wood
 */
public final class JmriScriptEngineManager {

    private final ScriptEngineManager manager = new ScriptEngineManager();
    private final HashMap<String, String> names = new HashMap<>();
    private final HashMap<String, ScriptEngineFactory> factories = new HashMap<>();
    private final HashMap<String, ScriptEngine> engines = new HashMap<>();
    private final ScriptContext context;

    private static final Logger log = LoggerFactory.getLogger(JmriScriptEngineManager.class);
    private static final String JYTHON_DEFAULTS = "jmri_defaults.py"; // should be replaced with default context

    public static final String PYTHON = "jython";
    private PythonInterpreter jython = null;

    /**
     * Create a JmriScriptEngineManager. In most cases, it is preferable to use
     * {@link #getDefault()} to get existing {@link javax.script.ScriptEngine}
     * instances.
     */
    public JmriScriptEngineManager() {
        this.manager.getEngineFactories().stream().forEach((factory) -> {
            log.info("{} {} is provided by {} {}",
                    factory.getLanguageName(),
                    factory.getLanguageVersion(),
                    factory.getEngineName(),
                    factory.getEngineVersion());
            factory.getExtensions().stream().forEach((extension) -> {
                names.put(extension, factory.getEngineName());
                log.debug("\tExtension: {}", extension);
            });
            factory.getExtensions().stream().forEach((mimeType) -> {
                names.put(mimeType, factory.getEngineName());
                log.debug("\tMime type: {}", mimeType);
            });
            factory.getNames().stream().forEach((name) -> {
                names.put(name, factory.getEngineName());
                log.debug("\tNames: {}", name);
            });
            this.names.put(factory.getLanguageName(), factory.getEngineName());
            this.factories.put(factory.getEngineName(), factory);
        });
        
        // this should agree with jmri_bindings.py and help/en/html/tools/scripting/Start.shtml
        Bindings bindings = new SimpleBindings();
        bindings.put("sensors", InstanceManager.getNullableDefault(SensorManager.class));
        bindings.put("turnouts", InstanceManager.getNullableDefault(TurnoutManager.class));
        bindings.put("lights", InstanceManager.getNullableDefault(LightManager.class));
        bindings.put("signals", InstanceManager.getNullableDefault(SignalHeadManager.class));
        bindings.put("masts", InstanceManager.getNullableDefault(SignalMastManager.class));
        bindings.put("routes", InstanceManager.getNullableDefault(RouteManager.class));
        bindings.put("blocks", InstanceManager.getNullableDefault(BlockManager.class));
        bindings.put("reporters", InstanceManager.getNullableDefault(ReporterManager.class));
        bindings.put("memories", InstanceManager.getNullableDefault(MemoryManager.class));
        bindings.put("powermanager", InstanceManager.getNullableDefault(PowerManager.class));
        bindings.put("addressedProgrammers", InstanceManager.getNullableDefault(AddressedProgrammerManager.class));
        bindings.put("globalProgrammers", InstanceManager.getNullableDefault(GlobalProgrammerManager.class));
        bindings.put("dcc", InstanceManager.getNullableDefault(CommandStation.class));
        bindings.put("audio", InstanceManager.getNullableDefault(AudioManager.class));
        bindings.put("shutdown", InstanceManager.getNullableDefault(ShutDownManager.class));
        bindings.put("layoutblocks", InstanceManager.getNullableDefault(LayoutBlockManager.class));
        bindings.put("warrants", InstanceManager.getNullableDefault(WarrantManager.class));
        bindings.put("CLOSED", Turnout.CLOSED);
        bindings.put("THROWN", Turnout.THROWN);
        bindings.put("CABLOCKOUT", Turnout.CABLOCKOUT);
        bindings.put("PUSHBUTTONLOCKOUT", Turnout.PUSHBUTTONLOCKOUT);
        bindings.put("UNLOCKED", Turnout.UNLOCKED);
        bindings.put("LOCKED", Turnout.LOCKED);
        bindings.put("ACTIVE", Sensor.ACTIVE);
        bindings.put("INACTIVE", Sensor.INACTIVE);
        bindings.put("ON", Light.ON);
        bindings.put("OFF", Light.OFF);
        bindings.put("UNKNOWN", NamedBean.UNKNOWN);
        bindings.put("INCONSISTENT", NamedBean.INCONSISTENT);
        bindings.put("DARK", SignalHead.DARK);
        bindings.put("RED", SignalHead.RED);
        bindings.put("YELLOW", SignalHead.YELLOW);
        bindings.put("GREEN", SignalHead.GREEN);
        bindings.put("LUNAR", SignalHead.LUNAR);
        bindings.put("FLASHRED", SignalHead.FLASHRED);
        bindings.put("FLASHYELLOW", SignalHead.FLASHYELLOW);
        bindings.put("FLASHGREEN", SignalHead.FLASHGREEN);
        bindings.put("FLASHLUNAR", SignalHead.FLASHLUNAR);
        bindings.put("FileUtil", FileUtilSupport.getDefault());
        this.context = new SimpleScriptContext();
        this.context.setBindings(bindings, ScriptContext.GLOBAL_SCOPE);
    }

    /**
     * Get the default instance of a JmriScriptEngineManager. Using the default
     * instance ensures that a script retains the context of the prior script.
     *
     * @return the default JmriScriptEngineManager
     */
    public static JmriScriptEngineManager getDefault() {
        return InstanceManager.getOptionalDefault(JmriScriptEngineManager.class).orElseGet(() -> {
            return InstanceManager.setDefault(JmriScriptEngineManager.class, new JmriScriptEngineManager());
        });
    }

    /**
     * Get the Java ScriptEngineManager that this object contains.
     *
     * @return the ScriptEngineManager
     */
    public ScriptEngineManager getManager() {
        return this.manager;
    }

    /**
     * Given a file extension, get the ScriptEngine registered to handle that
     * extension.
     *
     * @param extension a file extension
     * @return a ScriptEngine or null
     * @throws ScriptException if unable to get a matching ScriptEngine
     */
    public ScriptEngine getEngineByExtension(String extension) throws ScriptException {
        String name = this.names.get(extension);
        if (name == null) {
            log.error("Could not find script engine for extension \"{}\", expected one of {}", extension, String.join(",", names.keySet()));
            throw new ScriptException("Could not find script engine for extension \""+extension+"\" expected "+String.join(",", names.keySet()));
        }
        return this.getEngine(name);
    }

    /**
     * Given a mime type, get the ScriptEngine registered to handle that mime
     * type.
     *
     * @param mimeType a mimeType for a script
     * @return a ScriptEngine or null
     * @throws ScriptException if unable to get a matching ScriptEngine
     */
    public ScriptEngine getEngineByMimeType(String mimeType) throws ScriptException {
        String name = this.names.get(mimeType);
        if (name == null) {
            log.error("Could not find script engine for mime type \"{}\", expected one of {}", mimeType, String.join(",", names.keySet()));
            throw new ScriptException("Could not find script engine for mime type \""+mimeType+"\" expected "+String.join(",", names.keySet()));
        }
        return this.getEngine(name);
    }

    /**
     * Given a short name, get the ScriptEngine registered by that name.
     *
     * @param shortName the short name for the ScriptEngine
     * @return a ScriptEngine or null
     * @throws ScriptException if unable to get a matching ScriptEngine
     */
    public ScriptEngine getEngineByName(String shortName) throws ScriptException {
        String name = this.names.get(shortName);
        if (name == null) {
            log.error("Could not find script engine for short name \"{}\", expected one of {}", shortName, String.join(",", names.keySet()));
            throw new ScriptException("Could not find script engine for short name \""+shortName+"\" expected "+String.join(",", names.keySet()));
        }
        return this.getEngine(name);
    }

    /**
     * Get a ScriptEngine by its name.
     *
     * @param engineName the complete name for the ScriptEngine
     * @return a ScriptEngine or null
     */
    public ScriptEngine getEngine(String engineName) {
        if (!this.engines.containsKey(engineName)) {
            if (PYTHON.equals(engineName)) {
                // Setup the default python engine to use the JMRI python properties
                this.initializePython();
            } else {
                log.debug("Create engine for {}", engineName);
                ScriptEngine engine = this.factories.get(engineName).getScriptEngine();
                engine.setContext(this.context);
                this.engines.put(engineName, engine);
            }
        }
        return this.engines.get(engineName);
    }

    /**
     * Evaluate a script using the given ScriptEngine.
     *
     * @param script The script.
     * @param engine The script engine.
     * @return The results of evaluating the script.
     * @throws javax.script.ScriptException if there is an error in the script.
     */
    public Object eval(String script, ScriptEngine engine) throws ScriptException {
        if (PYTHON.equals(engine.getFactory().getEngineName()) && this.jython != null) {
            this.jython.exec(script);
            return null;
        }
        return engine.eval(script);
    }

    /**
     * Evaluate a script using the given ScriptEngine.
     *
     * @param reader The script.
     * @param engine The script engine.
     * @return The results of evaluating the script.
     * @throws javax.script.ScriptException if there is an error in the script.
     */
    public Object eval(Reader reader, ScriptEngine engine) throws ScriptException {
        return engine.eval(reader);
    }

    /**
     * Evaluate a script using the given ScriptEngine and Bindings.
     *
     * @param reader   The script.
     * @param engine   The script engine.
     * @param bindings Bindings passed to the script.
     * @return The results of evaluating the script.
     * @throws javax.script.ScriptException if there is an error in the script.
     */
    public Object eval(Reader reader, ScriptEngine engine, Bindings bindings) throws ScriptException {
        return engine.eval(reader, bindings);
    }

    /**
     * Evaluate a script using the given ScriptEngine and Bindings.
     *
     * @param reader  The script.
     * @param engine  The script engine.
     * @param context Context for the script.
     * @return The results of evaluating the script.
     * @throws javax.script.ScriptException if there is an error in the script.
     */
    public Object eval(Reader reader, ScriptEngine engine, ScriptContext context) throws ScriptException {
        return engine.eval(reader, context);
    }

    /**
     * Evaluate a script contained in a file. Uses the extension of the file to
     * determine which ScriptEngine to use.
     *
     * @param file the script file to evaluate.
     * @return the results of the evaluation.
     * @throws javax.script.ScriptException  if there is an error evaluating the
     *                                       script.
     * @throws java.io.FileNotFoundException if the script file cannot be found.
     * @throws java.io.IOException           if the script file cannot be read.
     */
    public Object eval(File file) throws ScriptException, FileNotFoundException, IOException {
        ScriptEngine engine = this.getEngineByExtension(FilenameUtils.getExtension(file.getName()));
        if (PYTHON.equals(engine.getFactory().getEngineName()) && this.jython != null) {
            try (FileInputStream fi = new FileInputStream(file)) {
                this.jython.execfile(fi);
            }
            return null;
        }
        try (FileReader fr = new FileReader(file)) {
            return engine.eval(fr);
        }
    }

    /**
     * Evaluate a script contained in a file given a set of
     * {@link javax.script.Bindings} to add to the script's context. Uses the
     * extension of the file to determine which ScriptEngine to use.
     *
     * @param file     the script file to evaluate.
     * @param bindings script bindings to evaluate against.
     * @return the results of the evaluation.
     * @throws javax.script.ScriptException  if there is an error evaluating the
     *                                       script.
     * @throws java.io.FileNotFoundException if the script file cannot be found.
     * @throws java.io.IOException           if the script file cannot be read.
     */
    public Object eval(File file, Bindings bindings) throws ScriptException, FileNotFoundException, IOException {
        ScriptEngine engine = this.getEngineByExtension(FilenameUtils.getExtension(file.getName()));
        if (PYTHON.equals(engine.getFactory().getEngineName()) && this.jython != null) {
            try (FileInputStream fi = new FileInputStream(file)) {
                this.jython.execfile(fi);
            }
            return null;
        }
        try (FileReader fr = new FileReader(file)) {
            return engine.eval(fr, bindings);
        }
    }

    /**
     * Evaluate a script contained in a file given a special context for the
     * script. Uses the extension of the file to determine which ScriptEngine to
     * use.
     *
     * @param file    the script file to evaluate.
     * @param context script context to evaluate within.
     * @return the results of the evaluation.
     * @throws javax.script.ScriptException  if there is an error evaluating the
     *                                       script.
     * @throws java.io.FileNotFoundException if the script file cannot be found.
     * @throws java.io.IOException           if the script file cannot be read.
     */
    public Object eval(File file, ScriptContext context) throws ScriptException, FileNotFoundException, IOException {
        ScriptEngine engine = this.getEngineByExtension(FilenameUtils.getExtension(file.getName()));
        if (PYTHON.equals(engine.getFactory().getEngineName()) && this.jython != null) {
            try (FileInputStream fi = new FileInputStream(file)) {
                this.jython.execfile(fi);
            }
            return null;
        }
        try (FileReader fr = new FileReader(file)) {
            return engine.eval(fr, context);
        }
    }

    /**
     * Run a script, suppressing common errors. Note that the file needs to have
     * a registered extension, or a NullPointerException will be thrown.
     *
     * @param file the script to run.
     */
    public void runScript(File file) {
        try {
            this.eval(file);
        } catch (FileNotFoundException ex) {
            log.error("File {} not found.", file);
        } catch (IOException ex) {
            log.error("Exception working with file {}", file);
        } catch (ScriptException ex) {
            log.error("Error in script {}.", file, ex);
        }

    }

    /**
     * Initialize all ScriptEngines. This can be used to prevent the on-demand
     * initialization of a ScriptEngine from causing a pause in JMRI.
     */
    public void initializeAllEngines() {
        this.factories.keySet().stream().forEach((name) -> {
            this.getEngine(name);
        });
    }

    /**
     * Get the default {@link javax.script.ScriptContext} for all
     * {@link javax.script.ScriptEngine}s.
     *
     * @return the default ScriptContext;
     */
    public ScriptContext getDefaultContext() {
        return this.context;
    }

    /**
     * Given a file extension, get the ScriptEngineFactory registered to handle
     * that extension.
     *
     * @param extension a file extension
     * @return a ScriptEngineFactory or null
     * @throws ScriptException if unable to get a matching ScriptEngineFactory
     */
    public ScriptEngineFactory getFactoryByExtension(String extension) throws ScriptException {
        String name = this.names.get(extension);
        if (name == null) {
            log.error("Could not find script engine factory for extension \"{}\", expected one of {}", extension, String.join(",", names.keySet()));
            throw new ScriptException("Could not find script engine for extension \""+extension+"\" expected "+String.join(",", names.keySet()));
        }
        return this.getFactory(name);
    }

    /**
     * Given a mime type, get the ScriptEngineFactory registered to handle that
     * mime type.
     *
     * @param mimeType the script mimeType
     * @return a ScriptEngineFactory or null
     * @throws ScriptException if unable to get a matching ScriptEngineFactory
     */
    public ScriptEngineFactory getFactoryByMimeType(String mimeType) throws ScriptException {
        String name = this.names.get(mimeType);
        if (name == null) {
            log.error("Could not find script engine factory for mime type \"{}\", expected one of {}", mimeType, String.join(",", names.keySet()));
            throw new ScriptException("Could not find script engine for mime type \""+mimeType+"\" expected "+String.join(",", names.keySet()));
        }
        return this.getFactory(name);
    }

    /**
     * Given a short name, get the ScriptEngineFactory registered by that name.
     *
     * @param shortName the short name for the factory
     * @return a ScriptEngineFactory or null
     * @throws ScriptException if unable to get a matching ScriptEngineFactory
     */
    public ScriptEngineFactory getFactoryByName(String shortName) throws ScriptException {
        String name = this.names.get(shortName);
        if (name == null) {
            log.error("Could not find script engine factory for name \"{}\", expected one of {}", shortName, String.join(",", names.keySet()));
            throw new ScriptException("Could not find script engine for short name \""+shortName+"\" expected "+String.join(",", names.keySet()));
        }
        return this.getFactory(name);
    }

    /**
     * Get a ScriptEngineFactory by its name.
     *
     * @param factoryName the complete name for a factory
     * @return a ScriptEngineFactory or null
     */
    public ScriptEngineFactory getFactory(String factoryName) {
        return this.factories.get(factoryName);
    }

    /**
     * The Python ScriptEngine can be configured using a custom
     * python.properties file and will run jmri_defaults.py if found in the
     * user's configuration profile or settings directory. See python.properties
     * in the JMRI installation directory for details of how to configure the
     * Python ScriptEngine.
     */
    public void initializePython() {
        if (!this.engines.containsKey(PYTHON)) {
            // Get properties for interpreter
            // Search in user files, the settings directory, and in the program path
            InputStream is = FileUtil.findInputStream("python.properties", new String[]{
                FileUtil.getUserFilesPath(),
                FileUtil.getPreferencesPath(),
                FileUtil.getProgramPath()
            });
            boolean execJython = false;
            if (is != null) {
                Properties properties;
                try {
                    properties = new Properties(System.getProperties());
                    properties.setProperty("python.console.encoding", "UTF-8"); // NOI18N
                    properties.setProperty("python.cachedir", FileUtil.getAbsoluteFilename(properties.getProperty("python.cachedir", "settings:jython/cache"))); // NOI18N
                    properties.load(is);
                    String path = properties.getProperty("python.path", "");
                    if (path.length() != 0) {
                        path = path.concat(File.pathSeparator);
                    }
                    properties.setProperty("python.path", path.concat(FileUtil.getScriptsPath().concat(File.pathSeparator).concat(FileUtil.getAbsoluteFilename("program:jython"))));
                    execJython = Boolean.valueOf(properties.getProperty("jython.exec", Boolean.toString(false)));
                } catch (IOException ex) {
                    log.error("Found, but unable to read python.properties: {}", ex.getMessage());
                    properties = null;
                }
                PySystemState.initialize(null, properties);
                log.debug("Jython path is {}", PySystemState.getBaseProperties().getProperty("python.path"));
            }

            // Create the interpreter
            try {
                log.debug("create interpreter");
                ScriptEngine python = this.manager.getEngineByName(PYTHON);
                python.setContext(this.context);
                is = FileUtil.findInputStream(JYTHON_DEFAULTS, new String[]{
                    FileUtil.getUserFilesPath(),
                    FileUtil.getPreferencesPath()
                });
                if (execJython) {
                    this.jython = new PythonInterpreter();
                }
                if (is != null) {
                    python.eval(new InputStreamReader(is));
                    if (this.jython != null) {
                        this.jython.execfile(is);
                    }
                }
                this.engines.put(PYTHON, python);
            } catch (ScriptException e) {
                log.error("Exception creating jython system objects", e);
            }
        }
    }
}
