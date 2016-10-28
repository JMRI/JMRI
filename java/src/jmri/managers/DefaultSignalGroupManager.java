package jmri.managers;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import jmri.Manager;
import jmri.SignalGroup;
import jmri.SignalGroupManager;
import jmri.implementation.DefaultSignalGroup;
import jmri.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of a SignalGroupManager.
 * <P>
 * This loads automatically the first time used.
 *
 * @author Bob Jacobsen Copyright (C) 2009
 */
public class DefaultSignalGroupManager extends AbstractManager
        implements SignalGroupManager, java.beans.PropertyChangeListener {

    public DefaultSignalGroupManager() {
        super();

        // load when created, which will generally
        // be the first time referenced
        //load();
    }

    public int getXMLOrder() {
        return Manager.SIGNALGROUPS;
    }

    public String getSystemPrefix() {
        return "I";
    }

    public char typeLetter() {
        return 'F';
    }

    public SignalGroup getSignalGroup(String name) {
        SignalGroup t = getByUserName(name);
        if (t != null) {
            return t;
        }

        return getBySystemName(name);
    }

    public SignalGroup getBySystemName(String key) {
        return (SignalGroup) _tsys.get(key);
    }

    public SignalGroup getByUserName(String key) {
        return (SignalGroup) _tuser.get(key);
    }

    public SignalGroup newSignalGroup(String sys) {
        SignalGroup g;
        g = new DefaultSignalGroup(sys);
        register(g);
        return g;

    }

    public SignalGroup provideSignalGroup(String systemName, String userName) {
        SignalGroup r;
        r = getByUserName(systemName);
        if (r != null) {
            return r;
        }
        r = getBySystemName(systemName);
        if (r != null) {
            return r;
        }
        // Route does not exist, create a new group
        r = new DefaultSignalGroup(systemName, userName);
        // save in the maps
        register(r);
        return r;
    }

    List<String> getListOfNames() {
        List<String> retval = new ArrayList<String>();
        // first locate the signal system directory
        // and get names of systems
        File signalDir;
        File[] files = new File[0];
        try {
            signalDir = new File(FileUtil.findURL("xml/signals", FileUtil.Location.INSTALLED).toURI());
            files = signalDir.listFiles();
        } catch (URISyntaxException | NullPointerException ex) {
            log.error("No signals are defined.", ex);
        }
        if (files == null) { // not a directory
            return retval; // empty, but not null
        }
        for (File file : files) {
            if (file.isDirectory()) {
                // check that there's an aspects.xml file
                File aspects = new File(file.getPath() + File.separator + "aspects.xml");
                if (aspects.exists()) {
                    log.debug("found system: " + file.getName());
                    retval.add(file.getName());
                }
            }
        }
        return retval;
    }

    static DefaultSignalGroupManager _instance = null;

    static public DefaultSignalGroupManager instance() {
        if (_instance == null) {
            _instance = new DefaultSignalGroupManager();
        }
        return (_instance);
    }

    public void deleteSignalGroup(SignalGroup s) {
        deregister(s);
    }

    public String getBeanTypeHandled() {
        return Bundle.getMessage("BeanNameSignalGroup");
    }

    private final static Logger log = LoggerFactory.getLogger(DefaultSignalGroupManager.class.getName());
}
