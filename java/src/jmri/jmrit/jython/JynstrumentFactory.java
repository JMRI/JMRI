package jmri.jmrit.jython;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import java.io.FileReader;
import javax.script.ScriptEngine;
import jmri.script.JmriScriptEngineManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JynstrumentFactory handles instantiation and connection of
 * {@link Jynstrument} instances.
 *
 * @see Jynstrument
 * @author Lionel Jeanson Copyright 2009
 * @since 2.7.8
 */
public class JynstrumentFactory {

    private static final String instanceName = "jynstrumentObjectInstance";

    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "Should crash if missing ScriptEngine dependencies are not present")
    public static Jynstrument createInstrument(String path, Object context) {
        String className = validate(path);
        if (className == null) {
            // Try containing directory
            File f = new File(path);
            String parentPath = f.getParent();
            className = validate(parentPath);
            if (className == null) {
                log.error("Invalid Jynstrument, neither {} or {} are usable", path, parentPath);
                return null;
            }
            path = parentPath;
        }
        String jyFile = path + File.separator + className + ".py";
        ScriptEngine engine = JmriScriptEngineManager.getDefault().getEngine(JmriScriptEngineManager.PYTHON);
        Jynstrument jyns;
        try {
            FileReader fr = new FileReader(jyFile);
            try {
                engine.eval(fr);
                engine.eval(instanceName + " = " + className + "()");
                jyns = (Jynstrument) engine.get(instanceName);
                engine.eval("del " + instanceName);
            } finally {
                fr.close();
            }
        } catch (java.io.IOException | javax.script.ScriptException ex) {
            log.error("Exception while creating Jynstrument", ex);
            return null;
        }
        jyns.setClassName(className);
        jyns.setContext(context);
        if (!jyns.validateContext()) {  // check validity of this Jynstrument for that extended context
            log.error("Invalid context for Jynstrument, host is {} and {} kind of host is expected", context.getClass(), jyns.getExpectedContextClassName());
            return null;
        }
        jyns.setJythonFile(jyFile);
        jyns.setFolder(path);
        jyns.setPopUpMenu(new JynstrumentPopupMenu(jyns));
        jyns.init();  // GO!
        return jyns;
    }

    // validate Jynstrument path, return className
    private static String validate(String path) {
        if (path == null) {
            log.error("Path is null");
            return null;
        }
        if (path.length() - 4 < 0) {
            log.error("File name too short (should at least end with .jyn) (got {})", path);
            return null;
        }
        if (path.endsWith(File.separator)) {
            path = path.substring(0, path.length()-File.separator.length());
        }
        File f = new File(path);

        // Path must be a folder named xyz.jin
        if (!f.isDirectory()) {
            log.debug("Not a directory, trying parent");
            return null;
        }
        if (! path.toLowerCase().endsWith(".jyn")) {
            log.debug("Not an instrument (folder name not ending with .jyn) (got {})", path);
            return null;
        }

        // must contain a xyz.py file and construct class name from filename (xyz actually) xyz class in xyz.py file in xyz.jin folder
        String[] children = f.list();
        String className = null;
        if (children == null) {
            log.error("Didn't find any files in {}", f);
            return className;
        }

        String assumedClassName = f.getName().substring(0, f.getName().length() - 4);
        // Try to find best candidate
        for (String c : children) {
            if ((c).compareToIgnoreCase(assumedClassName + ".py") == 0) {
                return assumedClassName; // got exact match for folder name
            }
        }
        // If not, use first python file we can find
        log.warn("Coulnd't find best candidate ({}), reverting to first one", assumedClassName + ".py");
        for (String c : children) {
            if (c.substring(c.length() - 3).compareToIgnoreCase(".py") == 0) {
                className = c.substring(0, c.length() - 3); // else take whatever comes
            }
        }
        log.warn("Using {}", className);
        return className;
    }

    private final static Logger log = LoggerFactory.getLogger(JynstrumentFactory.class);
}
