package jmri.managers;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.Manager;
import jmri.SignalGroup;
import jmri.SignalGroupManager;
import jmri.implementation.DefaultSignalGroup;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.FileUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of a SignalGroupManager.
 * <p>
 * This loads automatically the first time used.
 *
 * @author Bob Jacobsen Copyright (C) 2009, 2018
 */
public class DefaultSignalGroupManager extends AbstractManager<SignalGroup>
        implements SignalGroupManager {

    public DefaultSignalGroupManager(InternalSystemConnectionMemo memo) {
        super(memo);

        // load when created, which will generally
        // be the first time referenced
        //load();
    }

    @Override
    public int getXMLOrder() {
        return Manager.SIGNALGROUPS;
    }

    @Override
    public char typeLetter() {
        return 'G'; // according to JMRI: Names and Naming
    }

    /** {@inheritDoc} */
    @CheckForNull
    @Override
    public SignalGroup getSignalGroup(@Nonnull String name) {
        SignalGroup t = getByUserName(name);
        if (t != null) {
            return t;
        }

        return getBySystemName(name);
    }

    /** {@inheritDoc} */
    @CheckForNull
    @Override
    public SignalGroup getBySystemName(@Nonnull String key) {
        return _tsys.get(key);
    }

    /** {@inheritDoc} */
    @CheckForNull
    @Override
    public SignalGroup getByUserName(@Nonnull String key) {
        return _tuser.get(key);
    }

    /**
     * {@inheritDoc}
     *
     * Keep autostring in line with {@link #newSignalGroupWithUserName(String)},
     * {@link #getSystemPrefix()} and {@link #typeLetter()}
     */
    @Override
    @Nonnull
    public SignalGroup provideSignalGroup(@Nonnull String systemName, String userName) throws IllegalArgumentException {
        log.debug("provideGroup({})", systemName);
        SignalGroup r;
        if ( userName!=null ) {
            r = getByUserName(userName);
            if (r != null) {
                return r;
            }
        }
        r = getBySystemName(systemName);
        if (r != null) {
            return r;
        }
        // Group does not exist, create a new signal group
        r = new DefaultSignalGroup(systemName, userName);
        // save in the maps
        register(r);

        // Keep track of the last created auto system name
        updateAutoNumber(systemName);

        return r;
    }

    /**
     * {@inheritDoc}
     *
     * Keep autostring in line with {@link #provideSignalGroup(String, String)},
     * {@link #getSystemPrefix()} and {@link #typeLetter()}
     */
    @Deprecated // 4.25.2
    @Nonnull
    @Override
    public SignalGroup newSignaGroupWithUserName(@Nonnull String userName) {
        jmri.util.LoggingUtil.deprecationWarning(log, "newSignaGroupWithUserName");
        return newSignalGroupWithUserName(userName);
    }
    
    /**
     * {@inheritDoc}
     *
     * Keep autostring in line with {@link #provideSignalGroup(String, String)},
     * {@link #getSystemPrefix()} and {@link #typeLetter()}
     */
    @Nonnull
    @Override
    public SignalGroup newSignalGroupWithUserName(@Nonnull String userName) {
        return provideSignalGroup(getAutoSystemName(), userName);
    }

    List<String> getListOfNames() {
        List<String> retval = new ArrayList<>();
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
                    log.debug("found system: {}", file.getName());
                    retval.add(file.getName());
                }
            }
        }
        return retval;
    }

    @Override
    public void deleteSignalGroup(SignalGroup s) {
        deregister(s);
    }

    @Override
    @Nonnull
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameSignalGroups" : "BeanNameSignalGroup");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<SignalGroup> getNamedBeanClass() {
        return SignalGroup.class;
    }

    private final static Logger log = LoggerFactory.getLogger(DefaultSignalGroupManager.class);

}
