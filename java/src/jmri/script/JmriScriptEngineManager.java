package jmri.script;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Properties;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import jmri.AudioManager;
import jmri.BlockManager;
import jmri.CommandStation;
import jmri.InstanceManager;
import jmri.LightManager;
import jmri.MemoryManager;
import jmri.NamedBean;
import jmri.PowerManager;
import jmri.ProgrammerManager;
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
import org.apache.commons.io.FilenameUtils;
import org.python.core.PySystemState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rhwood
 */
public final class JmriScriptEngineManager {

    private final ScriptEngineManager manager = new ScriptEngineManager();
    private final HashMap<String, String> names = new HashMap<>();
    private final HashMap<String, ScriptEngine> engines = new HashMap<>();

    private static JmriScriptEngineManager instance = null;
    private static final Logger log = LoggerFactory.getLogger(JmriScriptEngineManager.class);
    private static final String defaultContextFile = "program:jython/jmri_defaults.py"; // should be replaced with default context

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
        });
        this.manager.put("turnouts", InstanceManager.getDefault(TurnoutManager.class));
        this.manager.put("sensors", InstanceManager.getDefault(SensorManager.class));
        this.manager.put("signals", InstanceManager.getDefault(SignalHeadManager.class));
        this.manager.put("masts", InstanceManager.getDefault(SignalMastManager.class));
        this.manager.put("lights", InstanceManager.getDefault(LightManager.class));
        this.manager.put("dcc", InstanceManager.getDefault(CommandStation.class));
        this.manager.put("reporters", InstanceManager.getDefault(ReporterManager.class));
        this.manager.put("memories", InstanceManager.getDefault(MemoryManager.class));
        this.manager.put("routes", InstanceManager.getDefault(RouteManager.class));
        this.manager.put("blocks", InstanceManager.getDefault(BlockManager.class));
        this.manager.put("powermanager", InstanceManager.getDefault(PowerManager.class));
        this.manager.put("programmers", InstanceManager.getDefault(ProgrammerManager.class));
        this.manager.put("shutdown", InstanceManager.getDefault(ShutDownManager.class));
        this.manager.put("audio", InstanceManager.getDefault(AudioManager.class));
        this.manager.put("layoutblocks", InstanceManager.getDefault(LayoutBlockManager.class));
        this.manager.put("warrants", InstanceManager.getDefault(WarrantManager.class));
        this.manager.put("CLOSED", Turnout.CLOSED);
        this.manager.put("THROWN", Turnout.THROWN);
        this.manager.put("CABLOCKOUT", Turnout.CABLOCKOUT);
        this.manager.put("PUSHBUTTONLOCKOUT", Turnout.PUSHBUTTONLOCKOUT);
        this.manager.put("UNLOCKED", Turnout.UNLOCKED);
        this.manager.put("LOCKED", Turnout.LOCKED);
        this.manager.put("ACTIVE", Sensor.ACTIVE);
        this.manager.put("INACTIVE", Sensor.INACTIVE);
        this.manager.put("UNKNOWN", NamedBean.UNKNOWN);
        this.manager.put("INCONSISTENT", NamedBean.INCONSISTENT);
        this.manager.put("DARK", SignalHead.DARK);
        this.manager.put("RED", SignalHead.RED);
        this.manager.put("YELLOW", SignalHead.YELLOW);
        this.manager.put("GREEN", SignalHead.GREEN);
        this.manager.put("LUNAR", SignalHead.LUNAR);
        this.manager.put("FLASHRED", SignalHead.FLASHRED);
        this.manager.put("FLASHYELLOW", SignalHead.FLASHYELLOW);
        this.manager.put("FLASHGREEN", SignalHead.FLASHGREEN);
        this.manager.put("FLASHLUNAR", SignalHead.FLASHLUNAR);
    }

    public static JmriScriptEngineManager getDefault() {
        if (JmriScriptEngineManager.instance == null) {
            JmriScriptEngineManager.instance = new JmriScriptEngineManager();
        }
        return JmriScriptEngineManager.instance;
    }

    public ScriptEngineManager getManager() {
        return this.manager;
    }

    public ScriptEngine getEngineByExtension(String extension) {
        String name = this.names.get(extension);
        return this.getEngineByName(name);
    }

    public ScriptEngine getEngineByMimeType(String mimeType) {
        String name = this.names.get(mimeType);
        return this.getEngineByName(name);
    }

    public ScriptEngine getEngineByName(String name) {
        if (!this.engines.containsKey(name)) {
            if ("python".equals(name)) {
                // Setup the default python engine to use the JMRI python properties
                this.initializePython();
            } else {
                this.engines.put(name, this.getManager().getEngineByName(name));
            }
        }
        return this.engines.get(name);
    }

    public Object eval(String script, ScriptEngine engine) throws ScriptException {
        return engine.eval(script);
    }

    public Object eval(Reader reader, ScriptEngine engine) throws ScriptException {
        return engine.eval(reader);
    }

    public Object eval(File file) throws ScriptException, FileNotFoundException {
        return this.getEngineByExtension(FilenameUtils.getExtension(file.getName())).eval(new FileReader(file));
    }
    
    public Object eval(File file, Bindings n) throws ScriptException, FileNotFoundException {
        return this.getEngineByExtension(FilenameUtils.getExtension(file.getName())).eval(new FileReader(file), n);
    }
    
    public Object eval(File file, ScriptContext context) throws ScriptException, FileNotFoundException {
        return this.getEngineByExtension(FilenameUtils.getExtension(file.getName())).eval(new FileReader(file), context);
    }

    public void initializePython() {
        if (!this.engines.containsKey("python")) {
            // Get properties for interpreter
            // Search in user files, the settings directory, and in the program path
            InputStream is = FileUtil.findInputStream("python.properties", new String[]{
                FileUtil.getUserFilesPath(),
                FileUtil.getPreferencesPath(),
                FileUtil.getProgramPath()
            });
            if (is != null) {
                Properties properties;
                try {
                    properties = new Properties(System.getProperties());
                    properties.load(is);
                } catch (IOException ex) {
                    log.error("Found, but unable to read python.properties: {}", ex.getMessage());
                    properties = null;
                }
                PySystemState.initialize(null, properties);
            }

            // must create one.
            /*
             TEMPORARY - will eventually be replaced with global objects in the default ScriptContext
             for all languages.
             */
            try {
                log.debug("create interpreter");
                ScriptEngine python = this.manager.getEngineByName("python");
                // have jython execute the default setup
                log.debug("load defaults from {}", defaultContextFile);
                python.eval(new FileReader(FileUtil.getExternalFilename(defaultContextFile)));
                this.engines.put("python", python);
            } catch (FileNotFoundException ex) {
                log.error("Python is not using the default JMRI context, since {} could not be found to provide it.", defaultContextFile);
            } catch (ScriptException e) {
                log.error("Exception creating jython system objects", e);
            }
        }
    }
}
